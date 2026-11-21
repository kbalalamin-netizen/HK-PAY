package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.model.CurrencyRate
import com.example.data.model.ThemeMode
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.SecurityNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val KYC_LIVENESS_PROMPTS = listOf(
    "Look straight into the camera and blink once",
    "Hold steady in good light and give a natural smile",
    "Tilt slightly right and face forward again"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBeforeExternalPicker: () -> Unit = {},
    onBack: () -> Unit,
    onSubmitKyc: (
        fullName: String,
        dob: String,
        country: String,
        docType: String,
        docNumber: String,
        frontUri: Uri?,
        backUri: Uri?,
        selfieUri: Uri?,
        livenessChallenge: String
    ) -> Unit
) {
    val extra = LocalHKPayColors.current
    val kyc = state.kycSubmission
    val effectiveStatus = state.effectiveKycStatus

    var fullName by remember(kyc.fullName, state.userProfile.fullName) {
        mutableStateOf(kyc.fullName.ifBlank { state.userProfile.fullName })
    }
    var dob by remember(kyc.dateOfBirth, state.userProfile.dateOfBirth) {
        mutableStateOf(kyc.dateOfBirth.ifBlank { state.userProfile.dateOfBirth })
    }
    var country by remember(kyc.country, state.userProfile.country) {
        mutableStateOf(kyc.country.ifBlank { state.userProfile.country.ifBlank { "Bangladesh" } })
    }
    var docType by remember(kyc.documentType) { mutableStateOf(kyc.documentType.ifBlank { "NID" }) }
    var docNumber by remember(kyc.documentNumber, state.userProfile.nidNumber) {
        mutableStateOf(kyc.documentNumber.ifBlank { state.userProfile.nidNumber })
    }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var challengeIdx by remember { mutableIntStateOf(0) }
    var livenessSummary by remember { mutableStateOf(kyc.livenessChallenge) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showFrontScanner by remember { mutableStateOf(false) }
    var showBackScanner by remember { mutableStateOf(false) }
    var showFaceLiveness by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val activeChallenge = livenessSummary.ifBlank {
        kyc.livenessChallenge.ifBlank { KYC_LIVENESS_PROMPTS[challengeIdx % KYC_LIVENESS_PROMPTS.size] }
    }

    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) frontUri = uri
    }
    val backPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) backUri = uri
    }
    val selfiePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) selfieUri = uri
    }

    if (showFrontScanner) {
        LiveDocumentScanDialog(
            title = "Live Scan $docType Front",
            subtitle = "Position the front side inside the glowing frame.",
            onBeforeExternalPicker = onBeforeExternalPicker,
            onCaptured = { uri ->
                frontUri = uri
                showFrontScanner = false
                errorText = null
            },
            onDismiss = { showFrontScanner = false }
        )
    }

    if (showBackScanner) {
        LiveDocumentScanDialog(
            title = "Live Scan $docType Back",
            subtitle = "Position the back side inside the glowing frame.",
            onBeforeExternalPicker = onBeforeExternalPicker,
            onCaptured = { uri ->
                backUri = uri
                showBackScanner = false
                errorText = null
            },
            onDismiss = { showBackScanner = false }
        )
    }

    if (showFaceLiveness) {
        LiveFaceLivenessDialog(
            livenessChallenge = activeChallenge,
            onCompleted = { uri, challengeSummary ->
                selfieUri = uri
                livenessSummary = challengeSummary
                showFaceLiveness = false
                errorText = null
            },
            onDismiss = { showFaceLiveness = false }
        )
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        dob = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
                    }
                    showDatePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "KYC & Liveness",
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
            Column(
                modifier = Modifier
                    .widthIn(max = 720.dp)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SecurityNoticeBanner(
                    text = "KYC documents & live liveness captures are stored in private per-user Firebase Storage (/kyc_uploads) with strict access control."
                )

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Verification Status",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (kyc.submittedAt > 0L) {
                                Text(
                                    text = "Submitted ${FormatUtils.formatDateTime(kyc.submittedAt)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        StatusBadge(status = effectiveStatus)
                    }
                    if (effectiveStatus == "Rejected" && kyc.rejectionReason.isNotBlank()) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            text = "Reason: ${kyc.rejectionReason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = extra.danger
                        )
                    }
                }

                val canEdit = effectiveStatus != "Approved" && effectiveStatus != "Pending"
                GlassCard {
                    Text(
                        "Identity Information",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))

                    if (!errorText.isNullOrBlank()) {
                        Text(
                            text = errorText.orEmpty(),
                            color = extra.danger,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }

                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        enabled = canEdit,
                        label = { Text("Legal Full Name (as on Document)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("kyc_fullname_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        enabled = canEdit,
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        trailingIcon = {
                            IconButton(
                                onClick = { if (canEdit) showDatePicker = true },
                                modifier = Modifier.testTag("kyc_dob_picker_button")
                            ) {
                                Icon(HKPayIcons.CalendarMonth, contentDescription = "Pick date of birth")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("kyc_dob_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = country,
                        onValueChange = { country = it },
                        enabled = canEdit,
                        label = { Text("Country of Issue") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("kyc_country_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Document Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("NID", "Passport").forEach { type ->
                            FilterChip(
                                selected = docType == type,
                                onClick = { if (canEdit) docType = type },
                                label = { Text(type) },
                                leadingIcon = { Icon(HKPayIcons.Badge, contentDescription = null) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = docNumber,
                        onValueChange = { docNumber = it },
                        enabled = canEdit,
                        label = { Text("$docType Number") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("kyc_doc_number_input"),
                        shape = RoundedCornerShape(14.dp)
                    )

                    Spacer(Modifier.height(16.dp))
                    Text("Document & Liveness Captures", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(8.dp))

                    KycUploadTile(
                        label = "Document Front Side",
                        selectedUri = frontUri,
                        existingUrl = kyc.frontDocumentUrl,
                        enabled = canEdit,
                        icon = HKPayIcons.CameraAlt,
                        onPick = {
                            if (canEdit) showFrontScanner = true
                            else {
                                onBeforeExternalPicker()
                                frontPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                        testTag = "kyc_upload_front_button"
                    )
                    Spacer(Modifier.height(10.dp))
                    KycUploadTile(
                        label = "Document Back Side",
                        selectedUri = backUri,
                        existingUrl = kyc.backDocumentUrl,
                        enabled = canEdit,
                        icon = HKPayIcons.CameraAlt,
                        onPick = {
                            if (canEdit) showBackScanner = true
                            else {
                                onBeforeExternalPicker()
                                backPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            }
                        },
                        testTag = "kyc_upload_back_button"
                    )

                    if (state.appSettings.kycSelfieEnabled || state.appSettings.livenessCheckRequired || kyc.selfieUrl.isNotBlank() || selfieUri != null) {
                        Spacer(Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = extra.electricBlue.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(HKPayIcons.FaceCheck, contentDescription = null, tint = extra.cyanGlow, modifier = Modifier.size(18.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("Liveness Challenge", style = MaterialTheme.typography.labelLarge, color = extra.cyanGlow)
                                    }
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        text = activeChallenge,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (canEdit) {
                                    IconButton(onClick = { challengeIdx++ }) {
                                        Icon(HKPayIcons.Refresh, contentDescription = "Refresh challenge", tint = extra.cyanGlow)
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showFaceLiveness = true },
                                enabled = canEdit,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("kyc_live_selfie_camera_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(HKPayIcons.FaceCheck, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Live Liveness")
                            }
                            OutlinedButton(
                                onClick = {
                                    onBeforeExternalPicker()
                                    selfiePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                enabled = canEdit,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("kyc_upload_selfie_button"),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(HKPayIcons.UploadFile, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("Photo Picker")
                            }
                        }
                        val selfieModel: Any? = selfieUri ?: kyc.selfieUrl.takeIf { it.isNotBlank() }
                        if (selfieModel != null) {
                            Spacer(Modifier.height(8.dp))
                            AsyncImage(
                                model = selfieModel,
                                contentDescription = "Liveness Selfie",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))
                    HKPayPrimaryButton(
                        text = when (effectiveStatus) {
                            "Approved" -> "KYC Verified"
                            "Pending" -> "KYC Under Review"
                            else -> "Submit Verification"
                        },
                        enabled = canEdit,
                        isLoading = state.isSubmitting,
                        onClick = {
                            val hasFront = frontUri != null || kyc.frontDocumentUrl.isNotBlank()
                            val hasSelfie = selfieUri != null || kyc.selfieUrl.isNotBlank()
                            when {
                                fullName.isBlank() -> errorText = "Full name is required."
                                dob.isBlank() -> errorText = "Date of birth is required."
                                country.isBlank() -> errorText = "Country is required."
                                docNumber.isBlank() -> errorText = "$docType number is required."
                                !hasFront -> errorText = "Please upload the front image of your $docType."
                                state.appSettings.livenessCheckRequired && !hasSelfie ->
                                    errorText = "Please complete the live selfie liveness check."
                                else -> {
                                    errorText = null
                                    onSubmitKyc(
                                        fullName,
                                        dob,
                                        country,
                                        docType,
                                        docNumber,
                                        frontUri,
                                        backUri,
                                        selfieUri,
                                        activeChallenge
                                    )
                                }
                            }
                        },
                        testTag = "kyc_submit_button"
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun KycUploadTile(
    label: String,
    selectedUri: Uri?,
    existingUrl: String,
    enabled: Boolean,
    icon: ImageVector = HKPayIcons.UploadFile,
    onPick: () -> Unit,
    testTag: String
) {
    val model: Any? = selectedUri ?: existingUrl.takeIf { it.isNotBlank() }
    Column {
        OutlinedButton(
            onClick = onPick,
            enabled = enabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(testTag),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (model != null) Icons.Rounded.CheckCircle else icon,
                contentDescription = null
            )
            Spacer(Modifier.width(8.dp))
            Text(if (model != null) "$label Selected" else "Select $label")
        }
        if (model != null) {
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = model,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun CurrencyRatesScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    val extra = LocalHKPayColors.current
    var selectedRate by remember(state.currencyRates) {
        mutableStateOf(state.currencyRates.firstOrNull())
    }
    var amountInput by remember { mutableStateOf("100") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "Currency Exchange Rates",
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
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = 760.dp)
                    .fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (state.currencyRates.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = HKPayIcons.CurrencyExchange,
                            title = "Awaiting Live Exchange Rates",
                            subtitle = "Exchange rates (USD/BDT, EUR/BDT, GBP/BDT) sync automatically when configured in Firebase RTDB at /currency_rates."
                        )
                    }
                } else {
                    item {
                        val activeRate: CurrencyRate? = selectedRate ?: state.currencyRates.firstOrNull()
                        GlassCard {
                            Text(
                                "Card Purchase Calculator",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                state.currencyRates.forEach { rate ->
                                    FilterChip(
                                        selected = activeRate?.id == rate.id,
                                        onClick = { selectedRate = rate },
                                        label = { Text("${rate.baseCurrency}/${rate.targetCurrency}") }
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = amountInput,
                                onValueChange = { amountInput = it },
                                label = { Text("Amount (${activeRate?.baseCurrency ?: "USD"})") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("rates_amount_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            if (activeRate != null) {
                                val parsed = amountInput.toDoubleOrNull() ?: 0.0
                                val baseConverted = parsed * activeRate.cardPurchaseRate
                                val serviceFee = (baseConverted * (activeRate.serviceChargePercent / 100.0)) + activeRate.serviceChargeFlat
                                val finalAmount = baseConverted + serviceFee
                                Spacer(Modifier.height(12.dp))
                                PriceLine("Card Purchase Rate", FormatUtils.formatMoney(activeRate.cardPurchaseRate, activeRate.targetCurrency))
                                PriceLine(
                                    "Service Charge (${activeRate.serviceChargePercent}% + ${FormatUtils.formatMoney(activeRate.serviceChargeFlat, activeRate.targetCurrency)})",
                                    FormatUtils.formatMoney(serviceFee, activeRate.targetCurrency)
                                )
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        "Estimated Total",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = FormatUtils.formatMoney(finalAmount, activeRate.targetCurrency),
                                        style = MaterialTheme.typography.titleLarge,
                                        color = extra.cyanGlow
                                    )
                                }
                            }
                        }
                    }

                    items(state.currencyRates, key = { it.id }) { rate ->
                        GlassCard(contentPadding = PaddingValues(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${rate.baseCurrency} → ${rate.targetCurrency}",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = extra.cyanGlow
                                )
                                Text(
                                    text = "Updated: ${FormatUtils.formatDateTime(rate.lastUpdated)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(10.dp))
                            PriceLine("Current Rate", FormatUtils.formatMoney(rate.currentRate, rate.targetCurrency))
                            PriceLine("Card Purchase Rate", FormatUtils.formatMoney(rate.cardPurchaseRate, rate.targetCurrency))
                            PriceLine(
                                "Service Charge",
                                "${rate.serviceChargePercent}% + ${FormatUtils.formatMoney(rate.serviceChargeFlat, rate.targetCurrency)}"
                            )
                        }
                    }
                }
            }
        }
    }
}
