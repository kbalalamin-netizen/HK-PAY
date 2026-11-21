package com.example.data.model

enum class ThemeMode(val label: String) {
    DARK("Dark"),
    LIGHT("Light"),
    NAVY("Navy");

    fun next(): ThemeMode = when (this) {
        DARK -> NAVY
        NAVY -> LIGHT
        LIGHT -> DARK
    }
}

enum class AppLanguage(
    val code: String,
    val flagEmoji: String,
    val displayName: String
) {
    ENGLISH("en", "🇺🇸", "English"),
    BENGALI("bn", "🇧🇩", "বাংলা"),
    ARABIC("ar", "🇸🇦", "العربية"),
    HINDI("hi", "🇮🇳", "हिन्दी"),
    MALAY("ms", "🇲🇾", "Melayu");

    val shortTag: String
        get() = "$flagEmoji $code"

    companion object {
        fun fromCode(code: String): AppLanguage {
            return entries.firstOrNull {
                it.code.equals(code.trim(), ignoreCase = true) ||
                    it.name.equals(code.trim(), ignoreCase = true)
            } ?: ENGLISH
        }
    }
}

data class DashboardSlider(
    val id: String = "",
    val title: String = "",
    val subtitle: String = "",
    val badge: String = "HK PAY",
    val ctaLabel: String = "Explore",
    val actionRoute: String = "store",
    val imageUrl: String = "",
    val gradientStartHex: Long = 0xFF0A2463,
    val gradientEndHex: Long = 0xFF04102F,
    val order: Int = 0,
    val active: Boolean = true
)

object HKPayConstants {
    const val FIREBASE_DATABASE_URL = "https://money-6e7f2-default-rtdb.firebaseio.com"

    val DEFAULT_DASHBOARD_SLIDERS = listOf(
        DashboardSlider(
            id = "hkpay_global_cards",
            title = "HK PAY Global Physical Cards",
            subtitle = "Order multi-currency Visa & Mastercard physical cards delivered worldwide with real-time tracking.",
            badge = "PHYSICAL CARDS",
            ctaLabel = "Card Store",
            actionRoute = "store",
            gradientStartHex = 0xFF0A2868,
            gradientEndHex = 0xFF030B29,
            order = 1,
            active = true
        ),
        DashboardSlider(
            id = "hkpay_card_security",
            title = "Instant Card Freeze, PIN Lock & 2FA",
            subtitle = "Protect every transaction with 4-digit App Lock, Biometric unlock, TOTP 2FA, and one-tap card freeze.",
            badge = "SECURITY SHIELD",
            ctaLabel = "My Cards",
            actionRoute = "cards",
            gradientStartHex = 0xFF0E3A5F,
            gradientEndHex = 0xFF041024,
            order = 2,
            active = true
        ),
        DashboardSlider(
            id = "hkpay_live_kyc",
            title = "Direct NID Scan & Face Liveness",
            subtitle = "Verify your identity in seconds with guided camera document capture and directional face liveness.",
            badge = "VERIFIED KYC",
            ctaLabel = "Verify KYC",
            actionRoute = "kyc",
            gradientStartHex = 0xFF14275E,
            gradientEndHex = 0xFF050A1F,
            order = 3,
            active = true
        )
    )

    val ORDER_STATUS_STEPS = listOf(
        "Order Placed",
        "Payment Pending",
        "Payment Confirmed",
        "KYC Pending",
        "KYC Approved",
        "Card Processing",
        "Card Issued",
        "Shipped",
        "In Transit",
        "Delivered",
        "Card Activation Pending",
        "Active"
    )

    val ALL_ORDER_STATUSES = ORDER_STATUS_STEPS + listOf("Cancelled", "Refunded")

    val PAYMENT_STATUSES = listOf("Unpaid", "Pending", "Paid", "Rejected", "Refunded")

    val KYC_STATUSES = listOf("Not Submitted", "Pending", "Approved", "Rejected")

    val NOTIFICATION_TYPES = listOf(
        "Order Created",
        "Order Status",
        "Payment Confirmed",
        "KYC Update",
        "Card Processing",
        "Card Shipped",
        "Card Delivered",
        "Card Activated",
        "Referral Bonus",
        "Refund",
        "Security Alert",
        "System Notice"
    )
}

data class NotificationPreferences(
    val orderUpdates: Boolean = true,
    val securityAlerts: Boolean = true,
    val rateAlerts: Boolean = true,
    val marketingNotices: Boolean = false
)

data class UserProfile(
    val uid: String = "",
    val publicUserId: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val country: String = "Bangladesh",
    val dateOfBirth: String = "",
    val gender: String = "",
    val nidNumber: String = "",
    val occupation: String = "",
    val monthlyIncomeRange: String = "",
    val preferredCurrency: String = "USD",
    val streetAddress: String = "",
    val cityUpazila: String = "",
    val district: String = "",
    val postalCode: String = "",
    val livenessVerified: Boolean = false,
    val livenessChallenge: String = "",
    val livenessTimestamp: Long = 0L,
    val photoUrl: String = "",
    val joinDate: Long = 0L,
    val kycStatus: String = "Not Submitted",
    val accountStatus: String = "Active",
    val fcmToken: String = "",
    val biometricEnabled: Boolean = false,
    val pinEnabled: Boolean = false,
    val twoFactorEnabled: Boolean = false,
    val twoFactorSecret: String = "",
    val frozenCardIds: Set<String> = emptySet(),
    val referralCode: String = "",
    val referredBy: String = "",
    val referredByCode: String = "",
    val rewardCredits: Double = 0.0,
    val notificationPrefs: NotificationPreferences = NotificationPreferences()
) {
    val completionPercent: Int
        get() {
            val fields = listOf(
                fullName,
                phone,
                dateOfBirth,
                gender,
                nidNumber,
                occupation,
                streetAddress,
                cityUpazila,
                district,
                country
            )
            val filled = fields.count { it.isNotBlank() } + (if (photoUrl.isNotBlank()) 1 else 0) + (if (livenessVerified) 1 else 0)
            return ((filled * 100) / 12).coerceIn(0, 100)
        }
}

data class CardPackage(
    val id: String = "",
    val name: String = "",
    val cardType: String = "",
    val network: String = "",
    val usdPackage: Double = 0.0,
    val cardPrice: Double = 0.0,
    val issuingFee: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val totalPrice: Double = 0.0,
    val currency: String = "USD",
    val description: String = "",
    val kycRequired: Boolean = true,
    val available: Boolean = true,
    val imageUrl: String = "",
    val highlights: List<String> = emptyList()
) {
    val computedTotal: Double
        get() = if (totalPrice > 0.0) totalPrice else (cardPrice + issuingFee + deliveryFee)
}

data class OrderStatusEvent(
    val status: String = "",
    val note: String = "",
    val timestamp: Long = 0L
)

data class CardOrder(
    val orderId: String = "",
    val userId: String = "",
    val packageId: String = "",
    val packageName: String = "",
    val cardType: String = "",
    val network: String = "",
    val usdPackage: Double = 0.0,
    val cardPrice: Double = 0.0,
    val issuingFee: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val referralDiscountApplied: Double = 0.0,
    val totalPrice: Double = 0.0,
    val currency: String = "USD",
    val selectedDesign: String = "",
    val customerName: String = "",
    val phoneNumber: String = "",
    val country: String = "",
    val district: String = "",
    val cityUpazila: String = "",
    val address: String = "",
    val postalCode: String = "",
    val orderNotes: String = "",
    val paymentMethod: String = "",
    val paymentReference: String = "",
    val paymentSenderNumber: String = "",
    val paymentProofUrl: String = "",
    val paymentStatus: String = "Unpaid",
    val orderStatus: String = "Order Placed",
    val courierName: String = "",
    val trackingNumber: String = "",
    val estimatedDelivery: String = "",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val timeline: List<OrderStatusEvent> = emptyList()
)

enum class OrderTrackStage(val label: String, val stepIndex: Int) {
    PLACED("Placed", 0),
    PROCESSING("Processing", 1),
    SHIPPED("Shipped", 2),
    DELIVERED("Delivered", 3),
    CANCELLED("Cancelled", -1)
}

fun CardOrder.resolvedTrackStage(): OrderTrackStage {
    return when (orderStatus.trim().lowercase()) {
        "cancelled", "refunded" -> OrderTrackStage.CANCELLED
        "delivered", "card activation pending", "active" -> OrderTrackStage.DELIVERED
        "shipped", "in transit" -> OrderTrackStage.SHIPPED
        "processing",
        "card processing",
        "card issued",
        "kyc pending",
        "kyc approved",
        "payment confirmed" -> OrderTrackStage.PROCESSING
        else -> OrderTrackStage.PLACED
    }
}

fun CardOrder.trackingProgressFraction(): Float {
    return when (resolvedTrackStage()) {
        OrderTrackStage.PLACED -> 0.25f
        OrderTrackStage.PROCESSING -> 0.50f
        OrderTrackStage.SHIPPED -> 0.75f
        OrderTrackStage.DELIVERED -> 1.0f
        OrderTrackStage.CANCELLED -> 0f
    }
}

data class IssuedCard(
    val cardId: String = "",
    val userId: String = "",
    val orderId: String = "",
    val cardholderName: String = "",
    val cardType: String = "",
    val network: String = "",
    val cardStatus: String = "",
    val activationStatus: String = "",
    val issuingProvider: String = "",
    val last4: String = "",
    val providerBalanceSupported: Boolean = false,
    val balance: Double = 0.0,
    val balanceCurrency: String = "USD",
    val providerSecureViewSupported: Boolean = false,
    val hostedCardViewUrl: String = "",
    val providerCardApiUrl: String = "",
    val issuedAt: Long = 0L,
    val isFrozen: Boolean = false
)

data class SecureCardDisplayData(
    val cardNumber: String,
    val expiryDate: String,
    val cvv: String
)

data class KycSubmission(
    val userId: String = "",
    val fullName: String = "",
    val dateOfBirth: String = "",
    val country: String = "Bangladesh",
    val documentType: String = "NID",
    val documentNumber: String = "",
    val frontDocumentUrl: String = "",
    val backDocumentUrl: String = "",
    val selfieUrl: String = "",
    val livenessChallenge: String = "",
    val livenessTimestamp: Long = 0L,
    val livenessVerified: Boolean = false,
    val status: String = "Not Submitted",
    val rejectionReason: String = "",
    val submittedAt: Long = 0L,
    val reviewedAt: Long = 0L
)

data class CurrencyRate(
    val id: String = "",
    val baseCurrency: String = "",
    val targetCurrency: String = "BDT",
    val currentRate: Double = 0.0,
    val cardPurchaseRate: Double = 0.0,
    val serviceChargePercent: Double = 0.0,
    val serviceChargeFlat: Double = 0.0,
    val lastUpdated: Long = 0L,
    val active: Boolean = true
)

data class PaymentMethodConfig(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val accountNumber: String = "",
    val accountTitle: String = "",
    val instructions: String = "",
    val gatewayUrl: String = "",
    val enabled: Boolean = true
)

data class AppSettings(
    val paymentMethods: List<PaymentMethodConfig> = emptyList(),
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "",
    val registrationEnabled: Boolean = true,
    val cardOrderingEnabled: Boolean = true,
    val referralProgramEnabled: Boolean = true,
    val referralBonusAmount: Double = 5.0,
    val kycRequiredForOrder: Boolean = true,
    val kycSelfieEnabled: Boolean = true,
    val livenessCheckRequired: Boolean = true,
    val supportEmail: String = "",
    val supportPhone: String = "",
    val supportWhatsApp: String = "",
    val supportHours: String = "",
    val bannerTitle: String = "",
    val systemNotice: String = "",
    val minSupportedVersionCode: Int = 1,
    val sliders: List<DashboardSlider> = emptyList()
)

data class TransactionRecord(
    val txId: String = "",
    val userId: String = "",
    val orderId: String = "",
    val cardId: String = "",
    val txType: String = "",
    val amount: Double = 0.0,
    val fee: Double = 0.0,
    val currency: String = "USD",
    val status: String = "Pending",
    val description: String = "",
    val createdAt: Long = 0L
)

data class AppNotification(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "System Notice",
    val referenceId: String = "",
    val isRead: Boolean = false,
    val createdAt: Long = 0L
)

data class TicketReply(
    val replyId: String = "",
    val senderRole: String = "user",
    val senderName: String = "",
    val message: String = "",
    val timestamp: Long = 0L
)

data class SupportTicket(
    val ticketId: String = "",
    val userId: String = "",
    val subject: String = "",
    val category: String = "Card Order",
    val status: String = "Open",
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val replies: List<TicketReply> = emptyList()
)

data class ReferralRecord(
    val referredUserId: String = "",
    val referredName: String = "",
    val rewardAmount: Double = 5.0,
    val refereeBonus: Double = 5.0,
    val status: String = "Credited",
    val createdAt: Long = 0L
)

data class ReferralReward(
    val rewardId: String = "",
    val title: String = "",
    val amount: Double = 0.0,
    val currency: String = "USD",
    val type: String = "Referral Bonus",
    val status: String = "Available",
    val createdAt: Long = 0L
)

data class CardDesignOption(
    val id: String,
    val name: String,
    val finishLabel: String,
    val description: String,
    val gradientStartHex: Long,
    val gradientEndHex: Long,
    val accentHex: Long
)
