package com.example.data.repository

import android.net.Uri
import com.example.data.model.AppNotification
import com.example.data.model.AppSettings
import com.example.data.model.CardOrder
import com.example.data.model.CardPackage
import com.example.data.model.CurrencyRate
import com.example.data.model.HKPayConstants
import com.example.data.model.IssuedCard
import com.example.data.model.KycSubmission
import com.example.data.model.NotificationPreferences
import com.example.data.model.OrderStatusEvent
import com.example.data.model.PaymentMethodConfig
import com.example.data.model.SecureCardDisplayData
import com.example.data.model.SupportTicket
import com.example.data.model.TicketReply
import com.example.data.model.TransactionRecord
import com.example.data.model.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit

class HKPayRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(HKPayConstants.FIREBASE_DATABASE_URL),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()
) {

    fun observeUserProfile(uid: String): Flow<UserProfile> {
        return database.reference.child("users").child(uid)
            .observeSnapshots()
            .map { snap -> parseUserProfile(uid, snap) }
            .catch { emit(UserProfile(uid = uid, publicUserId = AuthRepository.generatePublicUserId(uid))) }
    }

    suspend fun ensureUserProfileExists(uid: String, email: String, fullName: String) {
        val ref = database.reference.child("users").child(uid)
        val snap = runCatching { ref.get().awaitResult() }.getOrNull()
        if (snap == null || !snap.exists()) {
            val now = System.currentTimeMillis()
            val payload = mapOf(
                "uid" to uid,
                "publicUserId" to AuthRepository.generatePublicUserId(uid),
                "fullName" to fullName.ifBlank { email.substringBefore("@") },
                "email" to email,
                "phone" to "",
                "country" to "Bangladesh",
                "photoUrl" to "",
                "joinDate" to now,
                "kycStatus" to "Not Submitted",
                "accountStatus" to "Active",
                "notificationPrefs" to mapOf(
                    "orderUpdates" to true,
                    "securityAlerts" to true,
                    "rateAlerts" to true,
                    "marketingNotices" to false
                )
            )
            runCatching { ref.setValue(payload).awaitResult() }
        }
    }

    fun observeCardPackages(): Flow<List<CardPackage>> {
        return database.reference.child("card_packages")
            .observeSnapshots()
            .map { snap ->
                snap.children.mapNotNull { child -> parseCardPackage(child) }
                    .sortedWith(compareByDescending<CardPackage> { it.available }.thenBy { it.computedTotal })
            }
            .catch { emit(emptyList()) }
    }

    fun observeOrders(uid: String): Flow<List<CardOrder>> {
        return database.reference.child("orders").child(uid)
            .observeSnapshots()
            .map { snap ->
                snap.children.mapNotNull { child -> parseCardOrder(uid, child) }
                    .sortedByDescending { it.createdAt }
            }
            .catch { emit(emptyList()) }
    }

    fun observeIssuedCards(uid: String): Flow<List<IssuedCard>> {
        return database.reference.child("cards").child(uid)
            .observeSnapshots()
            .map { snap ->
                snap.children.mapNotNull { child -> parseIssuedCard(uid, child) }
                    .sortedByDescending { it.issuedAt }
            }
            .catch { emit(emptyList()) }
    }

    fun observeKycSubmission(uid: String): Flow<KycSubmission> {
        return database.reference.child("kyc_submissions").child(uid)
            .observeSnapshots()
            .map { snap -> parseKycSubmission(uid, snap) }
            .catch { emit(KycSubmission(userId = uid)) }
    }

    fun observeCurrencyRates(): Flow<List<CurrencyRate>> {
        return database.reference.child("currency_rates")
            .observeSnapshots()
            .map { snap ->
                snap.children.mapNotNull { child -> parseCurrencyRate(child) }
                    .filter { it.active }
            }
            .catch { emit(emptyList()) }
    }

    fun observeAppSettings(): Flow<AppSettings> {
        return database.reference.child("app_settings")
            .observeSnapshots()
            .map { snap -> parseAppSettings(snap) }
            .catch { emit(AppSettings()) }
    }

    fun observeTransactions(uid: String): Flow<List<TransactionRecord>> {
        return database.reference.child("transactions").child(uid)
            .observeSnapshots()
            .map { snap ->
                snap.children.mapNotNull { child -> parseTransaction(uid, child) }
                    .sortedByDescending { it.createdAt }
            }
            .catch { emit(emptyList()) }
    }

    fun observeNotifications(uid: String): Flow<List<AppNotification>> {
        return database.reference.child("notifications").child(uid)
            .observeSnapshots()
            .map { snap ->
                snap.children.mapNotNull { child -> parseNotification(uid, child) }
                    .sortedByDescending { it.createdAt }
            }
            .catch { emit(emptyList()) }
    }

    fun observeSupportTickets(uid: String): Flow<List<SupportTicket>> {
        return database.reference.child("support_tickets").child(uid)
            .observeSnapshots()
            .map { snap ->
                snap.children.mapNotNull { child -> parseSupportTicket(uid, child) }
                    .sortedByDescending { it.updatedAt }
            }
            .catch { emit(emptyList()) }
    }

    suspend fun updateProfile(
        uid: String,
        fullName: String,
        phone: String,
        country: String
    ): Result<Unit> = runCatching {
        val updates = mapOf(
            "fullName" to fullName.trim(),
            "phone" to phone.trim(),
            "country" to country.trim(),
            "updatedAt" to System.currentTimeMillis()
        )
        database.reference.child("users").child(uid).updateChildren(updates).awaitResult()
        Unit
    }

    suspend fun updateNotificationPreferences(
        uid: String,
        prefs: NotificationPreferences
    ): Result<Unit> = runCatching {
        val map = mapOf(
            "orderUpdates" to prefs.orderUpdates,
            "securityAlerts" to prefs.securityAlerts,
            "rateAlerts" to prefs.rateAlerts,
            "marketingNotices" to prefs.marketingNotices
        )
        database.reference.child("users").child(uid).child("notificationPrefs").setValue(map).awaitResult()
        Unit
    }

    suspend fun syncFcmToken(uid: String, token: String) {
        if (uid.isBlank() || token.isBlank()) return
        runCatching {
            database.reference.child("users").child(uid).child("fcmToken").setValue(token).awaitResult()
        }
    }

    suspend fun uploadProfilePhoto(uid: String, imageUri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("profile_photos/$uid/avatar_${System.currentTimeMillis()}.jpg")
        ref.putFile(imageUri).awaitResult()
        val downloadUrl = ref.downloadUrl.awaitResult().toString()
        database.reference.child("users").child(uid).child("photoUrl").setValue(downloadUrl).awaitResult()
        downloadUrl
    }

    suspend fun createCardOrder(
        uid: String,
        cardPackage: CardPackage,
        customerName: String,
        phoneNumber: String,
        country: String,
        district: String,
        cityUpazila: String,
        address: String,
        postalCode: String,
        orderNotes: String
    ): Result<String> = runCatching {
        val now = System.currentTimeMillis()
        val orderId = "ORD-${now.toString().takeLast(6)}-${UUID.randomUUID().toString().take(4).uppercase(Locale.US)}"
        val total = cardPackage.computedTotal

        val initialTimeline = mapOf(
            "0" to mapOf(
                "status" to "Order Placed",
                "note" to "Physical card order created for ${cardPackage.name}.",
                "timestamp" to now
            )
        )

        val orderMap = mapOf(
            "orderId" to orderId,
            "userId" to uid,
            "packageId" to cardPackage.id,
            "packageName" to cardPackage.name,
            "cardType" to cardPackage.cardType,
            "network" to cardPackage.network,
            "usdPackage" to cardPackage.usdPackage,
            "cardPrice" to cardPackage.cardPrice,
            "issuingFee" to cardPackage.issuingFee,
            "deliveryFee" to cardPackage.deliveryFee,
            "totalPrice" to total,
            "currency" to cardPackage.currency,
            "customerName" to customerName.trim(),
            "phoneNumber" to phoneNumber.trim(),
            "country" to country.trim(),
            "district" to district.trim(),
            "cityUpazila" to cityUpazila.trim(),
            "address" to address.trim(),
            "postalCode" to postalCode.trim(),
            "orderNotes" to orderNotes.trim(),
            "paymentMethod" to "",
            "paymentReference" to "",
            "paymentSenderNumber" to "",
            "paymentProofUrl" to "",
            "paymentStatus" to "Unpaid",
            "orderStatus" to "Order Placed",
            "courierName" to "",
            "trackingNumber" to "",
            "estimatedDelivery" to "",
            "createdAt" to now,
            "updatedAt" to now,
            "timeline" to initialTimeline
        )

        database.reference.child("orders").child(uid).child(orderId).setValue(orderMap).awaitResult()

        val txId = "TXN-${now.toString().takeLast(6)}-${UUID.randomUUID().toString().take(4).uppercase(Locale.US)}"
        val txMap = mapOf(
            "txId" to txId,
            "userId" to uid,
            "orderId" to orderId,
            "cardId" to "",
            "txType" to "Card Order",
            "amount" to cardPackage.cardPrice,
            "fee" to (cardPackage.issuingFee + cardPackage.deliveryFee),
            "currency" to cardPackage.currency,
            "status" to "Pending",
            "description" to "${cardPackage.name} physical card order",
            "createdAt" to now
        )
        database.reference.child("transactions").child(uid).child(txId).setValue(txMap).awaitResult()

        val notifRef = database.reference.child("notifications").child(uid).push()
        val notifMap = mapOf(
            "id" to (notifRef.key ?: ""),
            "userId" to uid,
            "title" to "Order Created ($orderId)",
            "message" to "Your order for ${cardPackage.name} has been placed. Complete payment to begin verification.",
            "type" to "Order Created",
            "referenceId" to orderId,
            "isRead" to false,
            "createdAt" to now
        )
        notifRef.setValue(notifMap).awaitResult()

        orderId
    }

    suspend fun submitPaymentProof(
        uid: String,
        order: CardOrder,
        paymentMethod: String,
        senderNumber: String,
        referenceCode: String,
        proofUri: Uri?
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        var proofUrl = order.paymentProofUrl
        if (proofUri != null) {
            val ref = storage.reference.child("user_uploads/$uid/payments/${order.orderId}_$now.jpg")
            ref.putFile(proofUri).awaitResult()
            proofUrl = ref.downloadUrl.awaitResult().toString()
        }

        val updatedTimeline = order.timeline.toMutableList().apply {
            add(
                OrderStatusEvent(
                    status = "Payment Pending",
                    note = "Payment submitted via $paymentMethod (Ref: ${referenceCode.trim()}). Awaiting backend verification.",
                    timestamp = now
                )
            )
        }
        val timelineMap = updatedTimeline.mapIndexed { idx, event ->
            idx.toString() to mapOf(
                "status" to event.status,
                "note" to event.note,
                "timestamp" to event.timestamp
            )
        }.toMap()

        val updates = mapOf(
            "paymentMethod" to paymentMethod,
            "paymentSenderNumber" to senderNumber.trim(),
            "paymentReference" to referenceCode.trim(),
            "paymentProofUrl" to proofUrl,
            "paymentStatus" to "Pending",
            "orderStatus" to "Payment Pending",
            "updatedAt" to now,
            "timeline" to timelineMap
        )

        database.reference.child("orders").child(uid).child(order.orderId).updateChildren(updates).awaitResult()

        val txId = "TXN-${now.toString().takeLast(6)}-${UUID.randomUUID().toString().take(4).uppercase(Locale.US)}"
        val txMap = mapOf(
            "txId" to txId,
            "userId" to uid,
            "orderId" to order.orderId,
            "cardId" to "",
            "txType" to "Payment",
            "amount" to order.totalPrice,
            "fee" to 0.0,
            "currency" to order.currency,
            "status" to "Pending",
            "description" to "Payment proof submitted via $paymentMethod (${referenceCode.trim()})",
            "createdAt" to now
        )
        database.reference.child("transactions").child(uid).child(txId).setValue(txMap).awaitResult()
        Unit
    }

    suspend fun submitKyc(
        uid: String,
        fullName: String,
        dateOfBirth: String,
        country: String,
        documentType: String,
        documentNumber: String,
        frontUri: Uri?,
        backUri: Uri?,
        selfieUri: Uri?,
        existingFrontUrl: String,
        existingBackUrl: String,
        existingSelfieUrl: String
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val frontUrl = if (frontUri != null) {
            val ref = storage.reference.child("kyc_uploads/$uid/front_$now.jpg")
            ref.putFile(frontUri).awaitResult()
            ref.downloadUrl.awaitResult().toString()
        } else existingFrontUrl

        val backUrl = if (backUri != null) {
            val ref = storage.reference.child("kyc_uploads/$uid/back_$now.jpg")
            ref.putFile(backUri).awaitResult()
            ref.downloadUrl.awaitResult().toString()
        } else existingBackUrl

        val selfieUrl = if (selfieUri != null) {
            val ref = storage.reference.child("kyc_uploads/$uid/selfie_$now.jpg")
            ref.putFile(selfieUri).awaitResult()
            ref.downloadUrl.awaitResult().toString()
        } else existingSelfieUrl

        val kycMap = mapOf(
            "userId" to uid,
            "fullName" to fullName.trim(),
            "dateOfBirth" to dateOfBirth.trim(),
            "country" to country.trim(),
            "documentType" to documentType,
            "documentNumber" to documentNumber.trim(),
            "frontDocumentUrl" to frontUrl,
            "backDocumentUrl" to backUrl,
            "selfieUrl" to selfieUrl,
            "status" to "Pending",
            "rejectionReason" to "",
            "submittedAt" to now,
            "reviewedAt" to 0L
        )

        database.reference.child("kyc_submissions").child(uid).setValue(kycMap).awaitResult()

        val notifRef = database.reference.child("notifications").child(uid).push()
        val notifMap = mapOf(
            "id" to (notifRef.key ?: ""),
            "userId" to uid,
            "title" to "KYC Documents Submitted",
            "message" to "Your $documentType verification is under review. You will be notified once verified.",
            "type" to "KYC Update",
            "referenceId" to uid,
            "isRead" to false,
            "createdAt" to now
        )
        notifRef.setValue(notifMap).awaitResult()
        Unit
    }

    suspend fun markNotificationRead(uid: String, notifId: String) {
        if (uid.isBlank() || notifId.isBlank()) return
        runCatching {
            database.reference.child("notifications").child(uid).child(notifId).child("isRead").setValue(true).awaitResult()
        }
    }

    suspend fun createSupportTicket(
        uid: String,
        senderName: String,
        subject: String,
        category: String,
        message: String
    ): Result<String> = runCatching {
        val now = System.currentTimeMillis()
        val ticketId = "TKT-${now.toString().takeLast(6)}-${UUID.randomUUID().toString().take(4).uppercase(Locale.US)}"
        val replyId = "RPL-$now"

        val ticketMap = mapOf(
            "ticketId" to ticketId,
            "userId" to uid,
            "subject" to subject.trim(),
            "category" to category,
            "status" to "Open",
            "createdAt" to now,
            "updatedAt" to now,
            "replies" to mapOf(
                replyId to mapOf(
                    "replyId" to replyId,
                    "senderRole" to "user",
                    "senderName" to senderName.ifBlank { "Customer" },
                    "message" to message.trim(),
                    "timestamp" to now
                )
            )
        )

        database.reference.child("support_tickets").child(uid).child(ticketId).setValue(ticketMap).awaitResult()
        ticketId
    }

    suspend fun replyToTicket(
        uid: String,
        ticketId: String,
        senderName: String,
        message: String
    ): Result<Unit> = runCatching {
        val now = System.currentTimeMillis()
        val replyId = "RPL-$now"
        val replyMap = mapOf(
            "replyId" to replyId,
            "senderRole" to "user",
            "senderName" to senderName.ifBlank { "Customer" },
            "message" to message.trim(),
            "timestamp" to now
        )
        val ticketRef = database.reference.child("support_tickets").child(uid).child(ticketId)
        ticketRef.child("replies").child(replyId).setValue(replyMap).awaitResult()
        ticketRef.child("updatedAt").setValue(now).awaitResult()
        Unit
    }

    suspend fun fetchSecureCardData(card: IssuedCard): Result<SecureCardDisplayData> = runCatching {
        val endpoint = card.providerCardApiUrl.trim()
        require(endpoint.startsWith("https://")) {
            "Verified HTTPS card provider endpoint is not configured for this card."
        }
        val user = auth.currentUser ?: error("Authentication required.")
        val idToken = user.getIdToken(false).awaitResult().token ?: error("Unable to verify session token.")

        withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(endpoint)
                .addHeader("Authorization", "Bearer $idToken")
                .addHeader("Accept", "application/json")
                .get()
                .build()

            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    error("Issuer secure view unavailable (HTTP ${response.code}).")
                }
                val body = response.body?.string().orEmpty()
                val json = JSONObject(body)
                SecureCardDisplayData(
                    cardNumber = json.optString("cardNumber", json.optString("pan", "")),
                    expiryDate = json.optString("expiryDate", json.optString("expiry", "")),
                    cvv = json.optString("cvv", "")
                )
            }
        }
    }

    private fun parseUserProfile(uid: String, snap: DataSnapshot): UserProfile {
        val prefsSnap = snap.child("notificationPrefs")
        return UserProfile(
            uid = uid,
            publicUserId = snap.stringValue("publicUserId", default = AuthRepository.generatePublicUserId(uid)),
            fullName = snap.stringValue("fullName", "name"),
            email = snap.stringValue("email"),
            phone = snap.stringValue("phone", "phoneNumber"),
            country = snap.stringValue("country", default = "Bangladesh"),
            photoUrl = snap.stringValue("photoUrl", "avatarUrl"),
            joinDate = snap.longValue("joinDate", "createdAt"),
            kycStatus = snap.stringValue("kycStatus", default = "Not Submitted"),
            accountStatus = snap.stringValue("accountStatus", default = "Active"),
            fcmToken = snap.stringValue("fcmToken"),
            notificationPrefs = NotificationPreferences(
                orderUpdates = prefsSnap.boolValue("orderUpdates", default = true),
                securityAlerts = prefsSnap.boolValue("securityAlerts", default = true),
                rateAlerts = prefsSnap.boolValue("rateAlerts", default = true),
                marketingNotices = prefsSnap.boolValue("marketingNotices", default = false)
            )
        )
    }

    private fun parseCardPackage(snap: DataSnapshot): CardPackage? {
        val id = snap.key ?: return null
        val name = snap.stringValue("name", "cardName", "title")
        if (name.isBlank()) return null
        val highlights = snap.child("highlights").children.mapNotNull { it.value?.toString() }
        val cardPrice = snap.doubleValue("cardPrice", "price")
        val issuingFee = snap.doubleValue("issuingFee")
        val deliveryFee = snap.doubleValue("deliveryFee")
        val totalPrice = snap.doubleValue("totalPrice", default = cardPrice + issuingFee + deliveryFee)
        return CardPackage(
            id = id,
            name = name,
            cardType = snap.stringValue("cardType", "type", default = "Physical Prepaid Card"),
            network = snap.stringValue("network", "cardNetwork"),
            usdPackage = snap.doubleValue("usdPackage", "usdLimit"),
            cardPrice = cardPrice,
            issuingFee = issuingFee,
            deliveryFee = deliveryFee,
            totalPrice = totalPrice,
            currency = snap.stringValue("currency", default = "USD"),
            description = snap.stringValue("description"),
            kycRequired = snap.boolValue("kycRequired", default = true),
            available = snap.boolValue("available", "isAvailable", default = true),
            imageUrl = snap.stringValue("imageUrl", "cardImage"),
            highlights = highlights
        )
    }

    private fun parseCardOrder(uid: String, snap: DataSnapshot): CardOrder? {
        val orderId = snap.key ?: return null
        val timeline = snap.child("timeline").children.mapNotNull { eventSnap ->
            val status = eventSnap.stringValue("status")
            if (status.isBlank()) null
            else OrderStatusEvent(
                status = status,
                note = eventSnap.stringValue("note"),
                timestamp = eventSnap.longValue("timestamp")
            )
        }.sortedBy { it.timestamp }

        return CardOrder(
            orderId = snap.stringValue("orderId", default = orderId),
            userId = snap.stringValue("userId", default = uid),
            packageId = snap.stringValue("packageId"),
            packageName = snap.stringValue("packageName", "cardName"),
            cardType = snap.stringValue("cardType"),
            network = snap.stringValue("network"),
            usdPackage = snap.doubleValue("usdPackage"),
            cardPrice = snap.doubleValue("cardPrice"),
            issuingFee = snap.doubleValue("issuingFee"),
            deliveryFee = snap.doubleValue("deliveryFee"),
            totalPrice = snap.doubleValue("totalPrice"),
            currency = snap.stringValue("currency", default = "USD"),
            customerName = snap.stringValue("customerName"),
            phoneNumber = snap.stringValue("phoneNumber"),
            country = snap.stringValue("country"),
            district = snap.stringValue("district"),
            cityUpazila = snap.stringValue("cityUpazila"),
            address = snap.stringValue("address"),
            postalCode = snap.stringValue("postalCode"),
            orderNotes = snap.stringValue("orderNotes"),
            paymentMethod = snap.stringValue("paymentMethod"),
            paymentReference = snap.stringValue("paymentReference"),
            paymentSenderNumber = snap.stringValue("paymentSenderNumber"),
            paymentProofUrl = snap.stringValue("paymentProofUrl"),
            paymentStatus = snap.stringValue("paymentStatus", default = "Unpaid"),
            orderStatus = snap.stringValue("orderStatus", default = "Order Placed"),
            courierName = snap.stringValue("courierName"),
            trackingNumber = snap.stringValue("trackingNumber"),
            estimatedDelivery = snap.stringValue("estimatedDelivery"),
            createdAt = snap.longValue("createdAt"),
            updatedAt = snap.longValue("updatedAt"),
            timeline = timeline
        )
    }

    private fun parseIssuedCard(uid: String, snap: DataSnapshot): IssuedCard? {
        val cardId = snap.key ?: return null
        val status = snap.stringValue("cardStatus", "status")
        if (status.isBlank() && snap.stringValue("cardholderName").isBlank()) return null
        return IssuedCard(
            cardId = cardId,
            userId = snap.stringValue("userId", default = uid),
            orderId = snap.stringValue("orderId"),
            cardholderName = snap.stringValue("cardholderName"),
            cardType = snap.stringValue("cardType", default = "Physical Card"),
            network = snap.stringValue("network"),
            cardStatus = status.ifBlank { "Issued" },
            activationStatus = snap.stringValue("activationStatus", default = "Pending Activation"),
            issuingProvider = snap.stringValue("issuingProvider", default = "Authorized Issuer"),
            last4 = snap.stringValue("last4"),
            providerBalanceSupported = snap.boolValue("providerBalanceSupported"),
            balance = snap.doubleValue("balance"),
            balanceCurrency = snap.stringValue("balanceCurrency", default = "USD"),
            providerSecureViewSupported = snap.boolValue("providerSecureViewSupported"),
            hostedCardViewUrl = snap.stringValue("hostedCardViewUrl"),
            providerCardApiUrl = snap.stringValue("providerCardApiUrl"),
            issuedAt = snap.longValue("issuedAt", "createdAt")
        )
    }

    private fun parseKycSubmission(uid: String, snap: DataSnapshot): KycSubmission {
        if (!snap.exists()) return KycSubmission(userId = uid)
        return KycSubmission(
            userId = uid,
            fullName = snap.stringValue("fullName"),
            dateOfBirth = snap.stringValue("dateOfBirth"),
            country = snap.stringValue("country", default = "Bangladesh"),
            documentType = snap.stringValue("documentType", default = "NID"),
            documentNumber = snap.stringValue("documentNumber"),
            frontDocumentUrl = snap.stringValue("frontDocumentUrl"),
            backDocumentUrl = snap.stringValue("backDocumentUrl"),
            selfieUrl = snap.stringValue("selfieUrl"),
            status = snap.stringValue("status", default = "Pending"),
            rejectionReason = snap.stringValue("rejectionReason"),
            submittedAt = snap.longValue("submittedAt"),
            reviewedAt = snap.longValue("reviewedAt")
        )
    }

    private fun parseCurrencyRate(snap: DataSnapshot): CurrencyRate? {
        val id = snap.key ?: return null
        val base = snap.stringValue("baseCurrency", "currency", default = id.uppercase(Locale.US))
        val currentRate = snap.doubleValue("currentRate", "rate")
        val cardRate = snap.doubleValue("cardPurchaseRate", default = currentRate)
        if (currentRate <= 0.0 && cardRate <= 0.0) return null
        return CurrencyRate(
            id = id,
            baseCurrency = base,
            targetCurrency = snap.stringValue("targetCurrency", default = "BDT"),
            currentRate = currentRate,
            cardPurchaseRate = cardRate,
            serviceChargePercent = snap.doubleValue("serviceChargePercent", "serviceCharge"),
            serviceChargeFlat = snap.doubleValue("serviceChargeFlat"),
            lastUpdated = snap.longValue("lastUpdated", "updatedAt"),
            active = snap.boolValue("active", default = true)
        )
    }

    private fun parseAppSettings(snap: DataSnapshot): AppSettings {
        val methodsSnap = snap.child("paymentMethods")
        val methods = methodsSnap.children.mapNotNull { m ->
            val id = m.key ?: return@mapNotNull null
            val name = m.stringValue("name", default = id)
            PaymentMethodConfig(
                id = id,
                name = name,
                type = m.stringValue("type", default = name),
                accountNumber = m.stringValue("accountNumber"),
                accountTitle = m.stringValue("accountTitle"),
                instructions = m.stringValue("instructions"),
                gatewayUrl = m.stringValue("gatewayUrl"),
                enabled = m.boolValue("enabled", default = true)
            )
        }
        return AppSettings(
            paymentMethods = methods,
            kycSelfieEnabled = snap.boolValue("kycSelfieEnabled", default = false),
            supportEmail = snap.stringValue("supportEmail"),
            supportHours = snap.stringValue("supportHours"),
            systemNotice = snap.stringValue("systemNotice")
        )
    }

    private fun parseTransaction(uid: String, snap: DataSnapshot): TransactionRecord? {
        val id = snap.key ?: return null
        return TransactionRecord(
            txId = snap.stringValue("txId", default = id),
            userId = snap.stringValue("userId", default = uid),
            orderId = snap.stringValue("orderId"),
            cardId = snap.stringValue("cardId"),
            txType = snap.stringValue("txType", "type", default = "Card Order"),
            amount = snap.doubleValue("amount"),
            fee = snap.doubleValue("fee"),
            currency = snap.stringValue("currency", default = "USD"),
            status = snap.stringValue("status", default = "Pending"),
            description = snap.stringValue("description"),
            createdAt = snap.longValue("createdAt")
        )
    }

    private fun parseNotification(uid: String, snap: DataSnapshot): AppNotification? {
        val id = snap.key ?: return null
        val title = snap.stringValue("title")
        if (title.isBlank()) return null
        return AppNotification(
            id = snap.stringValue("id", default = id),
            userId = snap.stringValue("userId", default = uid),
            title = title,
            message = snap.stringValue("message", "body"),
            type = snap.stringValue("type", default = "System Notice"),
            referenceId = snap.stringValue("referenceId"),
            isRead = snap.boolValue("isRead"),
            createdAt = snap.longValue("createdAt")
        )
    }

    private fun parseSupportTicket(uid: String, snap: DataSnapshot): SupportTicket? {
        val id = snap.key ?: return null
        val subject = snap.stringValue("subject")
        if (subject.isBlank()) return null
        val replies = snap.child("replies").children.mapNotNull { r ->
            val msg = r.stringValue("message")
            if (msg.isBlank()) null
            else TicketReply(
                replyId = r.stringValue("replyId", default = r.key.orEmpty()),
                senderRole = r.stringValue("senderRole", default = "user"),
                senderName = r.stringValue("senderName"),
                message = msg,
                timestamp = r.longValue("timestamp")
            )
        }.sortedBy { it.timestamp }

        return SupportTicket(
            ticketId = snap.stringValue("ticketId", default = id),
            userId = snap.stringValue("userId", default = uid),
            subject = subject,
            category = snap.stringValue("category", default = "General"),
            status = snap.stringValue("status", default = "Open"),
            createdAt = snap.longValue("createdAt"),
            updatedAt = snap.longValue("updatedAt"),
            replies = replies
        )
    }
}
