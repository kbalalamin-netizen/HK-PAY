package com.example.data.model

enum class ThemeMode(val label: String) {
    DARK("Dark Navy"),
    LIGHT("Light Ice"),
    SYSTEM("System Default")
}

object HKPayConstants {
    const val FIREBASE_DATABASE_URL = "https://money-6e7f2-default-rtdb.firebaseio.com"

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
        "Payment Confirmed",
        "KYC Update",
        "Card Processing",
        "Card Shipped",
        "Card Delivered",
        "Card Activated",
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
    val photoUrl: String = "",
    val joinDate: Long = 0L,
    val kycStatus: String = "Not Submitted",
    val accountStatus: String = "Active",
    val fcmToken: String = "",
    val notificationPrefs: NotificationPreferences = NotificationPreferences()
)

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
    val totalPrice: Double = 0.0,
    val currency: String = "USD",
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
    val issuedAt: Long = 0L
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
    val kycSelfieEnabled: Boolean = false,
    val supportEmail: String = "",
    val supportHours: String = "",
    val systemNotice: String = ""
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
