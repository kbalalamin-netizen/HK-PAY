package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.CardOrder
import com.example.data.model.CardPackage
import com.example.data.model.CurrencyRate
import com.example.data.model.IssuedCard
import com.example.data.model.KycSubmission
import com.example.data.model.NotificationPreferences
import com.example.data.model.SecureCardDisplayData
import com.example.data.model.SupportTicket
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionRecord
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.HKPayRepository
import com.example.data.repository.ThemePreferencesRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainDashboardState(
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val userProfile: UserProfile = UserProfile(),
    val cardPackages: List<CardPackage> = emptyList(),
    val orders: List<CardOrder> = emptyList(),
    val issuedCards: List<IssuedCard> = emptyList(),
    val kycSubmission: KycSubmission = KycSubmission(),
    val currencyRates: List<CurrencyRate> = emptyList(),
    val appSettings: AppSettings = AppSettings(),
    val transactions: List<TransactionRecord> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val supportTickets: List<SupportTicket> = emptyList(),
    val revealedCardData: Map<String, SecureCardDisplayData> = emptyMap(),
    val revealingCardId: String? = null
) {
    val unreadNotificationsCount: Int
        get() = notifications.count { !it.isRead }

    val activeCardsCount: Int
        get() = issuedCards.count { it.cardStatus.equals("Active", ignoreCase = true) }

    val pendingOrdersCount: Int
        get() = orders.count {
            it.orderStatus !in listOf("Active", "Delivered", "Cancelled", "Refunded")
        }

    val effectiveKycStatus: String
        get() = when {
            userProfile.kycStatus.isNotBlank() && userProfile.kycStatus != "Not Submitted" -> userProfile.kycStatus
            kycSubmission.status.isNotBlank() && kycSubmission.status != "Not Submitted" -> kycSubmission.status
            else -> "Not Submitted"
        }
}

sealed interface UiEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : UiEvent
    data class OrderCreated(val orderId: String) : UiEvent
    data object PaymentSubmitted : UiEvent
    data object KycSubmitted : UiEvent
    data class TicketCreated(val ticketId: String) : UiEvent
    data object ProfileUpdated : UiEvent
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HKPayRepository()
    private val authRepository = AuthRepository()
    private val themeRepository = ThemePreferencesRepository(application.applicationContext)

    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.DARK
    )

    private val _state = MutableStateFlow(MainDashboardState())
    val state: StateFlow<MainDashboardState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var boundUid: String? = null
    private val observationJobs = mutableListOf<Job>()

    fun bindUser(uid: String) {
        if (boundUid == uid && observationJobs.isNotEmpty()) return
        boundUid = uid
        observationJobs.forEach { it.cancel() }
        observationJobs.clear()

        _state.update { it.copy(isLoading = true) }

        syncFcmToken(uid)

        observationJobs += viewModelScope.launch {
            repository.observeUserProfile(uid).collect { profile ->
                _state.update { it.copy(userProfile = profile, isLoading = false) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeCardPackages().collect { packages ->
                _state.update { it.copy(cardPackages = packages) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeOrders(uid).collect { orders ->
                _state.update { it.copy(orders = orders) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeIssuedCards(uid).collect { cards ->
                _state.update { it.copy(issuedCards = cards) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeKycSubmission(uid).collect { kyc ->
                _state.update { it.copy(kycSubmission = kyc) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeCurrencyRates().collect { rates ->
                _state.update { it.copy(currencyRates = rates) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeAppSettings().collect { settings ->
                _state.update { it.copy(appSettings = settings) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeTransactions(uid).collect { txs ->
                _state.update { it.copy(transactions = txs) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeNotifications(uid).collect { notifs ->
                _state.update { it.copy(notifications = notifs) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeSupportTickets(uid).collect { tickets ->
                _state.update { it.copy(supportTickets = tickets) }
            }
        }
    }

    fun clearSession() {
        observationJobs.forEach { it.cancel() }
        observationJobs.clear()
        boundUid = null
        _state.value = MainDashboardState(isLoading = false)
    }

    private fun syncFcmToken(uid: String) {
        runCatching {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                if (!token.isNullOrBlank()) {
                    viewModelScope.launch {
                        repository.syncFcmToken(uid, token)
                    }
                }
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }

    fun createOrder(
        cardPackage: CardPackage,
        customerName: String,
        phoneNumber: String,
        country: String,
        district: String,
        cityUpazila: String,
        address: String,
        postalCode: String,
        orderNotes: String
    ) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.createCardOrder(
                uid = uid,
                cardPackage = cardPackage,
                customerName = customerName,
                phoneNumber = phoneNumber,
                country = country,
                district = district,
                cityUpazila = cityUpazila,
                address = address,
                postalCode = postalCode,
                orderNotes = orderNotes
            ).onSuccess { orderId ->
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.OrderCreated(orderId))
            }.onFailure { err ->
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Failed to create order.", true))
            }
        }
    }

    fun submitPayment(
        order: CardOrder,
        paymentMethod: String,
        senderNumber: String,
        referenceCode: String,
        proofUri: Uri?
    ) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.submitPaymentProof(
                uid = uid,
                order = order,
                paymentMethod = paymentMethod,
                senderNumber = senderNumber,
                referenceCode = referenceCode,
                proofUri = proofUri
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.PaymentSubmitted)
            }.onFailure { err ->
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Payment submission failed.", true))
            }
        }
    }

    fun submitKyc(
        fullName: String,
        dateOfBirth: String,
        country: String,
        documentType: String,
        documentNumber: String,
        frontUri: Uri?,
        backUri: Uri?,
        selfieUri: Uri?
    ) {
        val uid = boundUid ?: return
        val existing = _state.value.kycSubmission
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.submitKyc(
                uid = uid,
                fullName = fullName,
                dateOfBirth = dateOfBirth,
                country = country,
                documentType = documentType,
                documentNumber = documentNumber,
                frontUri = frontUri,
                backUri = backUri,
                selfieUri = selfieUri,
                existingFrontUrl = existing.frontDocumentUrl,
                existingBackUrl = existing.backDocumentUrl,
                existingSelfieUrl = existing.selfieUrl
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.KycSubmitted)
            }.onFailure { err ->
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "KYC submission failed.", true))
            }
        }
    }

    fun updateProfile(fullName: String, phone: String, country: String) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.updateProfile(uid, fullName, phone, country)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ProfileUpdated)
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Update failed.", true))
                }
        }
    }

    fun uploadProfilePhoto(uri: Uri) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.uploadProfilePhoto(uid, uri)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage("Profile photo updated."))
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Photo upload failed.", true))
                }
        }
    }

    fun updateNotificationPrefs(prefs: NotificationPreferences) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            repository.updateNotificationPreferences(uid, prefs)
                .onSuccess { _events.emit(UiEvent.ShowMessage("Notification settings saved.")) }
                .onFailure { err ->
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Unable to update settings.", true))
                }
        }
    }

    fun changePassword(currentPassword: String, newPassword: String) {
        if (newPassword.length < 6) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("New password must be at least 6 characters.", true))
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            authRepository.changePassword(currentPassword, newPassword)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage("Password changed successfully."))
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Password change failed.", true))
                }
        }
    }

    fun markNotificationRead(notifId: String) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            repository.markNotificationRead(uid, notifId)
        }
    }

    fun createSupportTicket(subject: String, category: String, message: String) {
        val uid = boundUid ?: return
        val name = _state.value.userProfile.fullName
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.createSupportTicket(uid, name, subject, category, message)
                .onSuccess { ticketId ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.TicketCreated(ticketId))
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Ticket creation failed.", true))
                }
        }
    }

    fun replyToTicket(ticketId: String, message: String) {
        val uid = boundUid ?: return
        val name = _state.value.userProfile.fullName
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.replyToTicket(uid, ticketId, name, message)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Reply failed.", true))
                }
        }
    }

    fun toggleCardSensitiveData(card: IssuedCard) {
        val current = _state.value.revealedCardData[card.cardId]
        if (current != null) {
            _state.update {
                it.copy(revealedCardData = it.revealedCardData - card.cardId)
            }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(revealingCardId = card.cardId) }
            repository.fetchSecureCardData(card)
                .onSuccess { secure ->
                    _state.update {
                        it.copy(
                            revealingCardId = null,
                            revealedCardData = it.revealedCardData + (card.cardId to secure)
                        )
                    }
                }
                .onFailure { err ->
                    _state.update { it.copy(revealingCardId = null) }
                    _events.emit(
                        UiEvent.ShowMessage(
                            err.localizedMessage ?: "Secure issuer API unavailable.",
                            isError = true
                        )
                    )
                }
        }
    }
}
