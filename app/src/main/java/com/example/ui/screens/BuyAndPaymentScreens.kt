package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.CardOrder
import com.example.data.model.CardPackage
import com.example.data.model.PaymentMethodConfig
import com.example.data.model.ThemeMode
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPhysicalCardVisual
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.SecurityNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.BiometricHelper
import com.example.util.FormatUtils

@Composable
fun BuyCardScreen(
    cardPackage: CardPackage?,
    selectedDesign: String = "",
    state: MainDashboardState,
    themeMode: ThemeMode,
    biometricEnabled: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onConfirmOrder: (
        pkg: CardPackage,
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
    ) -> Unit
) {
    val extra = LocalHKPayColors.current
    val context = LocalContext.current
    val profile = state.userProfile
    val designObj = remember(selectedDesign) {
        HKPayCardDesigns.firstOrNull { it.name.equals(selectedDesign, ignoreCase = true) }
    }
    var applyReferralCredit by remember(state.availableReferralCredit) {
        mutableStateOf(state.availableReferralCredit > 0.0)
    }

    var customerName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var phoneNumber by remember(profile.phone) { mutableStateOf(profile.phone) }
    var country by remember(profile.country) { mutableStateOf(profile.country.ifBlank { "Bangladesh" }) }
    var district by remember(profile.district) { mutableStateOf(profile.district) }
    var cityUpazila by remember(profile.cityUpazila) { mutableStateOf(profile.cityUpazila) }
    var address by remember(profile.streetAddress) { mutableStateOf(profile.streetAddress) }
    var postalCode by remember(profile.postalCode) { mutableStateOf(profile.postalCode) }
    var orderNotes by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val orderingAllowed = state.appSettings.cardOrderingEnabled

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "Buy Physical Card",
                onBack = onBack,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (cardPackage == null) {
                Box(modifier = Modifier.padding(16.dp)) {
                    EmptyStateCard(
                        icon = HKPayIcons.Payment,
                        title = "Package Unavailable",
                        subtitle = "The selected card package could not be loaded.",
                        actionLabel = "Back to Store",
                        onAction = onBack
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (!orderingAllowed) {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = extra.warning.copy(alpha = 0.14f),
                            border = BorderStroke(1.dp, extra.warning.copy(alpha = 0.5f))
                        ) {
                            Text(
                                text = "Card ordering is currently paused by administrator.",
                                style = MaterialTheme.typography.bodySmall,
                                color = extra.warning,
                                modifier = Modifier.fillMaxWidth().padding(12.dp)
                            )
                        }
                    }

                    HKPayPhysicalCardVisual(
                        cardName = buildString {
                            append(cardPackage.name)
                            if (selectedDesign.isNotBlank()) append(" • $selectedDesign")
                        },
                        cardholderName = customerName.ifBlank { "CARDHOLDER NAME" },
                        cardType = cardPackage.cardType,
                        network = cardPackage.network,
                        maskedOrFullNumber = "••••  ••••  ••••  ••••",
                        expiryDate = "MM/YY",
                        statusBadgeText = "Order Preview",
                        gradientStart = designObj?.let { Color(it.gradientStartHex) },
                        gradientEnd = designObj?.let { Color(it.gradientEndHex) },
                        accentColor = designObj?.let { Color(it.accentHex) }
                    )

                    val appliedDiscount = if (applyReferralCredit) {
                        state.availableReferralCredit.coerceAtMost(cardPackage.computedTotal)
                    } else 0.0
                    val finalTotal = (cardPackage.computedTotal - appliedDiscount).coerceAtLeast(0.0)

                    GlassCard {
                        Text(text = "Price Breakdown", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(10.dp))
                        PriceLine("Card Package", cardPackage.name)
                        if (selectedDesign.isNotBlank()) {
                            PriceLine("Selected Design", selectedDesign)
                        }
                        if (cardPackage.usdPackage > 0.0) {
                            PriceLine("USD Package Value", FormatUtils.formatMoney(cardPackage.usdPackage, "USD"))
                        }
                        PriceLine("Card Price", FormatUtils.formatMoney(cardPackage.cardPrice, cardPackage.currency))
                        PriceLine("Issuing Fee", FormatUtils.formatMoney(cardPackage.issuingFee, cardPackage.currency))
                        PriceLine("Delivery Fee", FormatUtils.formatMoney(cardPackage.deliveryFee, cardPackage.currency))
                        if (state.availableReferralCredit > 0.0) {
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Apply Referral Credit",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = extra.success
                                    )
                                    Text(
                                        text = "Available: ${FormatUtils.formatMoney(state.availableReferralCredit, "USD")}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = applyReferralCredit,
                                    onCheckedChange = { applyReferralCredit = it }
                                )
                            }
                            if (appliedDiscount > 0.0) {
                                PriceLine("Referral Discount", "-${FormatUtils.formatMoney(appliedDiscount, cardPackage.currency)}")
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Final Total",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = FormatUtils.formatMoney(finalTotal, cardPackage.currency),
                                style = MaterialTheme.typography.titleLarge,
                                color = extra.cyanGlow
                            )
                        }
                    }

                    GlassCard {
                        Text(text = "Delivery & Recipient Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(12.dp))

                        if (!validationError.isNullOrBlank()) {
                            Text(
                                text = validationError.orEmpty(),
                                color = extra.danger,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }

                        OutlinedTextField(
                            value = customerName,
                            onValueChange = { customerName = it },
                            label = { Text("Customer Full Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("buy_name_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            label = { Text("Phone Number") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("buy_phone_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = country,
                                onValueChange = { country = it },
                                label = { Text("Country") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("buy_country_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = district,
                                onValueChange = { district = it },
                                label = { Text("District / State") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("buy_district_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedTextField(
                                value = cityUpazila,
                                onValueChange = { cityUpazila = it },
                                label = { Text("City / Upazila") },
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("buy_city_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            OutlinedTextField(
                                value = postalCode,
                                onValueChange = { postalCode = it },
                                label = { Text("Postal Code") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f).testTag("buy_postal_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = address,
                            onValueChange = { address = it },
                            label = { Text("Full Street Address") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("buy_address_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = orderNotes,
                            onValueChange = { orderNotes = it },
                            label = { Text("Order Notes (Optional)") },
                            minLines = 2,
                            modifier = Modifier.fillMaxWidth().testTag("buy_notes_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Final Payable Amount", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(
                                    text = FormatUtils.formatMoney(finalTotal, cardPackage.currency),
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = extra.cyanGlow
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        HKPayPrimaryButton(
                            text = if (orderingAllowed) "Confirm Order & Proceed to Payment" else "Ordering Paused",
                            enabled = orderingAllowed,
                            onClick = {
                                when {
                                    customerName.isBlank() -> validationError = "Customer name is required."
                                    phoneNumber.isBlank() -> validationError = "Phone number is required."
                                    country.isBlank() || district.isBlank() || cityUpazila.isBlank() ->
                                        validationError = "Country, district, and city/upazila are required."
                                    address.isBlank() -> validationError = "Delivery street address is required."
                                    postalCode.isBlank() -> validationError = "Postal code is required."
                                    else -> {
                                        validationError = null
                                        val submitAction = {
                                            onConfirmOrder(
                                                cardPackage,
                                                selectedDesign,
                                                applyReferralCredit,
                                                customerName,
                                                phoneNumber,
                                                country,
                                                district,
                                                cityUpazila,
                                                address,
                                                postalCode,
                                                orderNotes
                                            )
                                        }
                                        val activity = context as? FragmentActivity
                                        if (biometricEnabled && activity != null) {
                                            BiometricHelper.authenticate(
                                                activity = activity,
                                                title = "Confirm Card Order",
                                                subtitle = "Verify biometrics to authorize this physical card order",
                                                negativeButtonText = "Cancel",
                                                onSuccess = submitAction,
                                                onError = { msg -> validationError = msg }
                                            )
                                        } else {
                                            submitAction()
                                        }
                                    }
                                }
                            },
                            isLoading = state.isSubmitting,
                            testTag = "buy_confirm_order_button"
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun PaymentScreen(
    order: CardOrder?,
    state: MainDashboardState,
    themeMode: ThemeMode,
    biometricEnabled: Boolean,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onSubmitPayment: (
        order: CardOrder,
        method: String,
        senderNumber: String,
        referenceCode: String,
        proofUri: Uri?
    ) -> Unit,
    onCopied: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    val configuredMethods = remember(state.appSettings.paymentMethods) {
        val enabled = state.appSettings.paymentMethods.filter { it.enabled }
        if (enabled.isNotEmpty()) {
            enabled
        } else {
            listOf(
                PaymentMethodConfig("bkash", "bKash", "Mobile Wallet", instructions = "Configured via Firebase /app_settings/paymentMethods"),
                PaymentMethodConfig("nagad", "Nagad", "Mobile Wallet", instructions = "Configured via Firebase /app_settings/paymentMethods"),
                PaymentMethodConfig("rocket", "Rocket", "Mobile Wallet", instructions = "Configured via Firebase /app_settings/paymentMethods"),
                PaymentMethodConfig("bank", "Bank Transfer", "Bank", instructions = "Configured via Firebase /app_settings/paymentMethods"),
                PaymentMethodConfig("gateway", "Provider Payment Gateway", "Gateway", instructions = "Configured via Firebase /app_settings/paymentMethods")
            )
        }
    }

    var selectedMethod by remember(configuredMethods) {
        mutableStateOf(configuredMethods.firstOrNull())
    }
    var senderNumber by remember { mutableStateOf("") }
    var referenceCode by remember { mutableStateOf("") }
    var proofUri by remember { mutableStateOf<Uri?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    val proofPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> if (uri != null) proofUri = uri }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "Complete Payment",
                onBack = onBack,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            if (order == null) {
                Box(Modifier.padding(16.dp)) {
                    EmptyStateCard(
                        icon = HKPayIcons.Payment,
                        title = "Order Not Found",
                        subtitle = "Unable to locate this order.",
                        actionLabel = "Go Back",
                        onAction = onBack
                    )
                }
            } else {
                Column(
                    modifier = Modifier
                        .widthIn(max = 720.dp)
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecurityNoticeBanner(text = stringResource(id = R.string.security_payment_notice))

                    GlassCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(order.packageName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(
                                    text = "Order ${order.orderId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = order.paymentStatus)
                        }
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = FormatUtils.formatMoney(order.totalPrice, order.currency),
                            style = MaterialTheme.typography.displaySmall,
                            color = extra.cyanGlow
                        )
                    }

                    GlassCard {
                        Text(text = "Select Payment Method", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(10.dp))
                        configuredMethods.forEach { method ->
                            val isSelected = selectedMethod?.id == method.id
                            Surface(
                                onClick = { selectedMethod = method },
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelected) extra.electricBlue.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) extra.cyanGlow else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .testTag("payment_method_${method.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(selected = isSelected, onClick = { selectedMethod = method })
                                    Spacer(Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(method.name, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                                        if (method.accountNumber.isNotBlank()) {
                                            Text(
                                                text = "${method.accountTitle.ifBlank { "Account" }}: ${method.accountNumber}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = extra.cyanGlow
                                            )
                                        }
                                        if (method.instructions.isNotBlank()) {
                                            Text(
                                                text = method.instructions,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                    if (method.accountNumber.isNotBlank()) {
                                        IconButton(onClick = {
                                            clipboard.setText(AnnotatedString(method.accountNumber))
                                            onCopied("${method.name} number copied")
                                        }) {
                                            Icon(HKPayIcons.ContentCopy, contentDescription = "Copy account number")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    val activeMethod = selectedMethod
                    if (activeMethod != null && activeMethod.gatewayUrl.startsWith("https://")) {
                        GlassCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(HKPayIcons.AccountBalance, contentDescription = null, tint = extra.cyanGlow)
                                Spacer(Modifier.width(10.dp))
                                Text("Provider-Approved Payment Gateway", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    runCatching {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(activeMethod.gatewayUrl)))
                                    }
                                },
                                modifier = Modifier.fillMaxWidth().height(48.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(HKPayIcons.OpenInNew, contentDescription = null)
                                Spacer(Modifier.width(8.dp))
                                Text("Open Secure Checkout")
                            }
                        }
                    }

                    GlassCard {
                        Text(text = "Submit Payment Verification Details", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
                        Spacer(Modifier.height(10.dp))

                        if (!formError.isNullOrBlank()) {
                            Text(
                                text = formError.orEmpty(),
                                color = extra.danger,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        OutlinedTextField(
                            value = senderNumber,
                            onValueChange = { senderNumber = it },
                            label = { Text("Sender Phone / Account Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("payment_sender_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        OutlinedTextField(
                            value = referenceCode,
                            onValueChange = { referenceCode = it },
                            label = { Text("Transaction ID / Reference Number") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("payment_reference_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        Spacer(Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                proofPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("payment_upload_proof_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = if (proofUri != null) Icons.Rounded.CheckCircle else HKPayIcons.UploadFile,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(if (proofUri != null) "Payment Slip Selected (Tap to Change)" else "Upload Payment Receipt (Optional)")
                        }

                        if (proofUri != null) {
                            Spacer(Modifier.height(10.dp))
                            AsyncImage(
                                model = proofUri,
                                contentDescription = "Selected payment slip",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HKPayPrimaryButton(
                            text = "Submit Payment for Verification",
                            onClick = {
                                when {
                                    activeMethod == null -> formError = "Please select a payment method."
                                    senderNumber.isBlank() -> formError = "Sender account or phone number is required."
                                    referenceCode.isBlank() -> formError = "Transaction reference ID is required."
                                    else -> {
                                        formError = null
                                        val payAction = {
                                            onSubmitPayment(
                                                order,
                                                activeMethod.name,
                                                senderNumber,
                                                referenceCode,
                                                proofUri
                                            )
                                        }
                                        val activity = context as? FragmentActivity
                                        if (biometricEnabled && activity != null) {
                                            BiometricHelper.authenticate(
                                                activity = activity,
                                                title = "Authorize Payment Submission",
                                                subtitle = "Verify fingerprint or face ID to submit payment proof",
                                                negativeButtonText = "Cancel",
                                                onSuccess = payAction,
                                                onError = { msg -> formError = msg }
                                            )
                                        } else {
                                            payAction()
                                        }
                                    }
                                }
                            },
                            isLoading = state.isSubmitting,
                            testTag = "payment_submit_button"
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}
