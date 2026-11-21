package com.example.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppLanguage
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.CardOrder
import com.example.data.model.CardPackage
import com.example.data.model.CurrencyRate
import com.example.data.model.DashboardSlider
import com.example.data.model.HKPayConstants
import com.example.data.model.IssuedCard
import com.example.data.model.KycSubmission
import com.example.data.model.NotificationPreferences
import com.example.data.model.ReferralRecord
import com.example.data.model.ReferralReward
import com.example.data.model.SecureCardDisplayData
import com.example.data.model.SupportTicket
import com.example.data.model.ThemeMode
import com.example.data.model.TransactionRecord
import com.example.data.model.UserProfile
import com.example.data.repository.AuthRepository
import com.example.data.repository.HKPayRepository
import com.example.data.repository.ThemePreferencesRepository
import com.example.notifications.NotificationHelper
import com.example.util.SecurityUtils
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
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
    val remoteSliders: List<DashboardSlider> = emptyList(),
    val transactions: List<TransactionRecord> = emptyList(),
    val notifications: List<AppNotification> = emptyList(),
    val supportTickets: List<SupportTicket> = emptyList(),
    val referrals: List<ReferralRecord> = emptyList(),
    val referralRewards: List<ReferralReward> = emptyList(),
    val revealedCardData: Map<String, SecureCardDisplayData> = emptyMap(),
    val revealingCardId: String? = null,
    val freezingCardId: String? = null
) {
    val activeSliders: List<DashboardSlider>
        get() = when {
            remoteSliders.isNotEmpty() -> remoteSliders
            appSettings.sliders.isNotEmpty() -> appSettings.sliders
            else -> HKPayConstants.DEFAULT_DASHBOARD_SLIDERS
        }

    fun isCardFrozen(card: IssuedCard): Boolean {
        return card.isFrozen ||
            card.cardId in userProfile.frozenCardIds ||
            card.cardStatus.equals("Frozen", ignoreCase = true) ||
            card.cardStatus.equals("Inactive", ignoreCase = true)
    }

    val availableReferralCredit: Double
        get() = referralRewards.filter { it.status == "Available" }.sumOf { it.amount }

    val totalReferralEarned: Double
        get() = referralRewards.sumOf { it.amount }

    val unreadNotificationsCount: Int
        get() = notifications.count { !it.isRead }

    val activeCardsCount: Int
        get() = issuedCards.count { it.cardStatus.equals("Active", ignoreCase = true) && !isCardFrozen(it) }

    val totalLinkedCardBalance: Double
        get() = issuedCards.filter { it.providerBalanceSupported }.sumOf { it.balance }

    val hasAnyBalanceSupportedCard: Boolean
        get() = issuedCards.any { it.providerBalanceSupported }

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

    val isKycVerified: Boolean
        get() = effectiveKycStatus.equals("Approved", ignoreCase = true)
}

sealed interface UiEvent {
    data class ShowMessage(val message: String, val isError: Boolean = false) : UiEvent
    data class OrderCreated(val orderId: String) : UiEvent
    data object PaymentSubmitted : UiEvent
    data object KycSubmitted : UiEvent
    data class TicketCreated(val ticketId: String) : UiEvent
    data object ProfileUpdated : UiEvent
    data object PinUpdated : UiEvent
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HKPayRepository()
    private val authRepository = AuthRepository()
    private val themeRepository = ThemePreferencesRepository(application.applicationContext)

    val themeMode: StateFlow<ThemeMode> = themeRepository.themeMode.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ThemeMode.NAVY
    )

    val appLanguage: StateFlow<AppLanguage> = themeRepository.appLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppLanguage.ENGLISH
    )

    val biometricEnabled: StateFlow<Boolean> = themeRepository.biometricEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val pinLockEnabled: StateFlow<Boolean> = themeRepository.pinLockEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val twoFactorEnabled: StateFlow<Boolean> = themeRepository.twoFactorEnabled.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val twoFactorSecret: StateFlow<String> = themeRepository.twoFactorSecret.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val permissionsWizardDone: StateFlow<Boolean> = themeRepository.permissionsWizardDone.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val tutorialCompleted: StateFlow<Boolean> = themeRepository.tutorialCompleted.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val balanceVisible: StateFlow<Boolean> = themeRepository.balanceVisible.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = true
    )

    private val _isBiometricUnlockedThisSession = MutableStateFlow(false)
    val isBiometricUnlockedThisSession: StateFlow<Boolean> = _isBiometricUnlockedThisSession.asStateFlow()

    private val _isCardManagementVerified = MutableStateFlow(false)
    val isCardManagementVerified: StateFlow<Boolean> = _isCardManagementVerified.asStateFlow()

    private val _pinUnlockError = MutableStateFlow<String?>(null)
    val pinUnlockError: StateFlow<String?> = _pinUnlockError.asStateFlow()

    private var suppressNextLockUntilMs: Long = 0L

    private val _state = MutableStateFlow(MainDashboardState())
    val state: StateFlow<MainDashboardState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<UiEvent>()
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    private var boundUid: String? = null
    private val observationJobs = mutableListOf<Job>()
    private var hasLoadedInitialOrders = false
    private val lastKnownOrderStatuses = mutableMapOf<String, String>()

    fun bindUser(uid: String) {
        if (boundUid == uid && observationJobs.isNotEmpty()) return
        boundUid = uid
        observationJobs.forEach { it.cancel() }
        observationJobs.clear()
        hasLoadedInitialOrders = false
        lastKnownOrderStatuses.clear()

        _state.update { it.copy(isLoading = true) }

        syncFcmTokenAndTopics(uid)

        observationJobs += viewModelScope.launch {
            repository.observeUserProfile(uid).collect { profile ->
                _state.update { it.copy(userProfile = profile, isLoading = false) }
                // Sync remote 2FA state into local store if enabled remotely and missing locally
                if (profile.twoFactorEnabled && profile.twoFactorSecret.isNotBlank() && twoFactorSecret.value.isBlank()) {
                    themeRepository.setTwoFactorConfig(true, profile.twoFactorSecret)
                }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeCardPackages().collect { packages ->
                _state.update { it.copy(cardPackages = packages) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeOrders(uid).collect { orders ->
                handlePersonalizedOrderStatusNotifications(uid, orders)
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
            repository.observeDashboardSliders().collect { sliders ->
                _state.update { it.copy(remoteSliders = sliders) }
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
        observationJobs += viewModelScope.launch {
            repository.observeReferrals(uid).collect { referrals ->
                _state.update { it.copy(referrals = referrals) }
            }
        }
        observationJobs += viewModelScope.launch {
            repository.observeReferralRewards(uid).collect { rewards ->
                _state.update { it.copy(referralRewards = rewards) }
            }
        }
    }

    private fun handlePersonalizedOrderStatusNotifications(uid: String, orders: List<CardOrder>) {
        if (!hasLoadedInitialOrders) {
            orders.forEach { lastKnownOrderStatuses[it.orderId] = it.orderStatus }
            hasLoadedInitialOrders = true
            return
        }
        val orderUpdatesEnabled = _state.value.userProfile.notificationPrefs.orderUpdates
        val customerName = _state.value.userProfile.fullName
        orders.forEach { order ->
            val previous = lastKnownOrderStatuses[order.orderId]
            if (previous != null && previous != order.orderStatus) {
                lastKnownOrderStatuses[order.orderId] = order.orderStatus
                if (orderUpdatesEnabled) {
                    val (title, body) = NotificationHelper.buildPersonalizedOrderMessage(
                        customerName = customerName.ifBlank { order.customerName },
                        packageName = order.packageName,
                        orderId = order.orderId,
                        orderStatus = order.orderStatus,
                        trackingNumber = order.trackingNumber,
                        previousStatus = previous
                    )
                    NotificationHelper.showNotification(
                        context = getApplication(),
                        title = title,
                        body = body,
                        type = "Order Status",
                        orderId = order.orderId
                    )
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.recordInAppOrderNotification(
                            uid = uid,
                            title = title,
                            message = body,
                            orderId = order.orderId
                        )
                    }
                }
            } else {
                lastKnownOrderStatuses[order.orderId] = order.orderStatus
            }
        }
    }

    fun clearSession() {
        observationJobs.forEach { it.cancel() }
        observationJobs.clear()
        boundUid = null
        hasLoadedInitialOrders = false
        lastKnownOrderStatuses.clear()
        _isBiometricUnlockedThisSession.value = false
        _isCardManagementVerified.value = false
        _pinUnlockError.value = null
        _state.value = MainDashboardState(isLoading = false)
    }

    fun suppressNextBackgroundLock(durationMs: Long = 15_000L) {
        suppressNextLockUntilMs = System.currentTimeMillis() + durationMs
    }

    fun lockAppIfEnabled() {
        if (System.currentTimeMillis() <= suppressNextLockUntilMs) {
            suppressNextLockUntilMs = 0L
            return
        }
        if (pinLockEnabled.value || biometricEnabled.value) {
            _isBiometricUnlockedThisSession.value = false
            _isCardManagementVerified.value = false
        }
    }

    fun unlockBiometrically() {
        _pinUnlockError.value = null
        _isBiometricUnlockedThisSession.value = true
    }

    fun markCardManagementVerified() {
        _isCardManagementVerified.value = true
    }

    fun unlockWithPin(pin: String) {
        if (!SecurityUtils.isValidPin(pin)) {
            _pinUnlockError.value = "Enter a 4-digit PIN"
            return
        }
        viewModelScope.launch {
            val ok = themeRepository.verifyPinCode(pin)
            if (ok) {
                _pinUnlockError.value = null
                _isBiometricUnlockedThisSession.value = true
            } else {
                _pinUnlockError.value = "Incorrect 4-digit PIN. Try again or use Biometric."
            }
        }
    }

    fun clearPinUnlockError() {
        _pinUnlockError.value = null
    }

    fun setupOrChangePin(currentPin: String?, newPin: String, confirmPin: String) {
        if (!SecurityUtils.isValidPin(newPin)) {
            viewModelScope.launch { _events.emit(UiEvent.ShowMessage("PIN must be exactly 4 digits.", true)) }
            return
        }
        if (newPin != confirmPin) {
            viewModelScope.launch { _events.emit(UiEvent.ShowMessage("New PINs do not match.", true)) }
            return
        }
        viewModelScope.launch {
            if (pinLockEnabled.value) {
                if (currentPin.isNullOrBlank() || !themeRepository.verifyPinCode(currentPin)) {
                    _events.emit(UiEvent.ShowMessage("Current 4-digit PIN is incorrect.", true))
                    return@launch
                }
            }
            val uid = boundUid.orEmpty()
            val email = _state.value.userProfile.email
            themeRepository.setPinCode(uid, email, newPin)
            _isBiometricUnlockedThisSession.value = true
            if (uid.isNotBlank()) {
                repository.updateSecuritySettings(uid = uid, pinEnabled = true)
            }
            _events.emit(UiEvent.PinUpdated)
            _events.emit(UiEvent.ShowMessage("4-digit App Lock PIN saved."))
        }
    }

    fun disablePinLock(currentPin: String) {
        viewModelScope.launch {
            if (!themeRepository.verifyPinCode(currentPin)) {
                _events.emit(UiEvent.ShowMessage("Incorrect current PIN.", true))
                return@launch
            }
            themeRepository.disablePinLock()
            val uid = boundUid.orEmpty()
            if (uid.isNotBlank()) {
                repository.updateSecuritySettings(uid = uid, pinEnabled = false)
            }
            _events.emit(UiEvent.ShowMessage("4-digit PIN App Lock disabled."))
        }
    }

    fun enableTwoFactor(secret: String, totpCode: String) {
        if (!SecurityUtils.verifyTotpCode(secret, totpCode)) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("Invalid 6-digit authenticator code. Please check the current code.", true))
            }
            return
        }
        val uid = boundUid.orEmpty()
        viewModelScope.launch {
            themeRepository.setTwoFactorConfig(enabled = true, secret = secret)
            if (uid.isNotBlank()) {
                repository.updateSecuritySettings(
                    uid = uid,
                    twoFactorEnabled = true,
                    twoFactorSecret = secret
                )
            }
            _events.emit(UiEvent.ShowMessage("2-Factor Authentication (2FA) enabled."))
        }
    }

    fun disableTwoFactor(totpCode: String) {
        val secret = twoFactorSecret.value.ifBlank { _state.value.userProfile.twoFactorSecret }
        if (secret.isNotBlank() && !SecurityUtils.verifyTotpCode(secret, totpCode)) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("Enter a valid 6-digit 2FA code to disable 2FA.", true))
            }
            return
        }
        val uid = boundUid.orEmpty()
        viewModelScope.launch {
            themeRepository.setTwoFactorConfig(enabled = false, secret = "")
            if (uid.isNotBlank()) {
                repository.updateSecuritySettings(
                    uid = uid,
                    twoFactorEnabled = false,
                    twoFactorSecret = ""
                )
            }
            _events.emit(UiEvent.ShowMessage("2-Factor Authentication disabled."))
        }
    }

    fun verifyTwoFactorCode(code: String): Boolean {
        val secret = twoFactorSecret.value.ifBlank { _state.value.userProfile.twoFactorSecret }
        if (secret.isBlank()) return true
        return SecurityUtils.verifyTotpCode(secret, code)
    }

    fun toggleCardFreeze(card: IssuedCard, freeze: Boolean) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            _state.update { it.copy(freezingCardId = card.cardId) }
            repository.toggleCardFreeze(uid, card.cardId, freeze)
                .onSuccess {
                    _state.update {
                        it.copy(
                            freezingCardId = null,
                            revealedCardData = if (freeze) it.revealedCardData - card.cardId else it.revealedCardData
                        )
                    }
                    _events.emit(
                        UiEvent.ShowMessage(
                            if (freeze) {
                                "Card •••• ${card.last4.ifBlank { "HK" }} frozen. Further transactions blocked."
                            } else {
                                "Card •••• ${card.last4.ifBlank { "HK" }} unfrozen and active."
                            }
                        )
                    )
                }
                .onFailure { err ->
                    _state.update { it.copy(freezingCardId = null) }
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Failed to update card freeze status.", true))
                }
        }
    }

    fun toggleBalanceVisibility() {
        val next = !balanceVisible.value
        viewModelScope.launch {
            themeRepository.setBalanceVisible(next)
        }
    }

    fun cycleThemeMode() {
        val next = themeMode.value.next()
        viewModelScope.launch {
            themeRepository.setThemeMode(next)
            _events.emit(UiEvent.ShowMessage("Theme switched to ${next.label}"))
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            themeRepository.setThemeMode(mode)
        }
    }

    fun setAppLanguage(language: AppLanguage) {
        viewModelScope.launch {
            themeRepository.setAppLanguage(language)
            _events.emit(UiEvent.ShowMessage("Language switched to ${language.shortTag} (${language.displayName})"))
        }
    }

    fun setPermissionsWizardDone(done: Boolean) {
        viewModelScope.launch {
            themeRepository.setPermissionsWizardDone(done)
        }
    }

    fun setBiometricEnabled(enabled: Boolean) {
        val uid = boundUid
        viewModelScope.launch {
            themeRepository.setBiometricEnabled(enabled)
            if (enabled) {
                _isBiometricUnlockedThisSession.value = true
            }
            if (uid != null) {
                repository.setBiometricPreference(uid, enabled)
            }
            _events.emit(
                UiEvent.ShowMessage(
                    if (enabled) "Biometric authentication enabled" else "Biometric authentication disabled"
                )
            )
        }
    }

    fun setTutorialCompleted(completed: Boolean) {
        viewModelScope.launch {
            themeRepository.setTutorialCompleted(completed)
        }
    }

    fun claimReferralCode(code: String) {
        val uid = boundUid ?: return
        val name = _state.value.userProfile.fullName
        val bonus = _state.value.appSettings.referralBonusAmount.takeIf { it > 0.0 } ?: AuthRepository.REFERRAL_BONUS_USD
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.claimReferralCode(uid, name, code, bonus)
                .onSuccess {
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage("Referral bonus unlocked! $${String.format("%.2f", bonus)} fee credit added."))
                }
                .onFailure { err ->
                    _state.update { it.copy(isSubmitting = false) }
                    _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "Unable to redeem referral code.", true))
                }
        }
    }

    private fun syncFcmTokenAndTopics(uid: String) {
        runCatching {
            val messaging = FirebaseMessaging.getInstance()
            messaging.subscribeToTopic("all_users")
            messaging.subscribeToTopic("user_$uid")
            messaging.subscribeToTopic("orders_$uid")
            messaging.subscribeToTopic("order_status_updates")
            messaging.token.addOnSuccessListener { token ->
                if (!token.isNullOrBlank()) {
                    viewModelScope.launch(Dispatchers.IO) {
                        repository.syncFcmToken(uid, token)
                    }
                }
            }
        }
    }

    fun createOrder(
        cardPackage: CardPackage,
        selectedDesign: String,
        applyReferralCredit: Boolean,
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
        val currentState = _state.value
        if (!currentState.appSettings.cardOrderingEnabled) {
            viewModelScope.launch {
                _events.emit(UiEvent.ShowMessage("Card ordering is currently paused by administrator.", true))
            }
            return
        }
        val availableRewards = currentState.referralRewards.filter { it.status == "Available" }
        val discount = if (applyReferralCredit) {
            availableRewards.sumOf { it.amount }.coerceAtMost(cardPackage.computedTotal)
        } else 0.0
        val rewardIds = if (discount > 0.0) availableRewards.map { it.rewardId } else emptyList()

        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.createCardOrder(
                uid = uid,
                cardPackage = cardPackage,
                selectedDesign = selectedDesign,
                referralDiscountApplied = discount,
                availableRewardIdsToMark = rewardIds,
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
        selfieUri: Uri?,
        livenessChallenge: String
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
                livenessChallenge = livenessChallenge,
                existingFrontUrl = existing.frontDocumentUrl,
                existingBackUrl = existing.backDocumentUrl,
                existingSelfieUrl = existing.selfieUrl,
                existingLivenessVerified = existing.livenessVerified
            ).onSuccess {
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.KycSubmitted)
            }.onFailure { err ->
                _state.update { it.copy(isSubmitting = false) }
                _events.emit(UiEvent.ShowMessage(err.localizedMessage ?: "KYC submission failed.", true))
            }
        }
    }

    fun updateProfile(
        fullName: String,
        phone: String,
        country: String,
        dateOfBirth: String,
        gender: String,
        nidNumber: String,
        occupation: String,
        monthlyIncomeRange: String,
        preferredCurrency: String,
        streetAddress: String,
        cityUpazila: String,
        district: String,
        postalCode: String
    ) {
        val uid = boundUid ?: return
        viewModelScope.launch {
            _state.update { it.copy(isSubmitting = true) }
            repository.updateProfile(
                uid = uid,
                fullName = fullName,
                phone = phone,
                country = country,
                dateOfBirth = dateOfBirth,
                gender = gender,
                nidNumber = nidNumber,
                occupation = occupation,
                monthlyIncomeRange = monthlyIncomeRange,
                preferredCurrency = preferredCurrency,
                streetAddress = streetAddress,
                cityUpazila = cityUpazila,
                district = district,
                postalCode = postalCode
            )
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
        if (_state.value.isCardFrozen(card)) {
            viewModelScope.launch {
                _events.emit(
                    UiEvent.ShowMessage(
                        "Card •••• ${card.last4.ifBlank { "HK" }} is frozen. Unfreeze the card to access card details.",
                        isError = true
                    )
                )
            }
            return
        }
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
