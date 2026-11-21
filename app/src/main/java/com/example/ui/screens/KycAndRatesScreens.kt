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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Badge
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.CameraFront
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CurrencyExchange
import androidx.compose.material.icons.rounded.UploadFile
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.CurrencyRate
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.SecurityNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KycScreen(
    state: MainDashboardState,
    onBack: () -> Unit,
    onSubmitKyc: (
        fullName: String,
        dob: String,
        country: String,
        docType: String,
        docNumber: String,
        frontUri: Uri?,
        backUri: Uri?,
        selfieUri: Uri?
    ) -> Unit
) {
    val extra = LocalHKPayColors.current
    val kyc = state.kycSubmission
    val effectiveStatus = state.effectiveKycStatus

    var fullName by remember(kyc.fullName, state.userProfile.fullName) {
        mutableStateOf(kyc.fullName.ifBlank { state.userProfile.fullName })
    }
    var dob by remember(kyc.dateOfBirth) { mutableStateOf(kyc.dateOfBirth) }
    var country by remember(kyc.country, state.userProfile.country) {
        mutableStateOf(kyc.country.ifBlank { state.userProfile.country.ifBlank { "Bangladesh" } })
    }
    var docType by remember(kyc.documentType) { mutableStateOf(kyc.documentType.ifBlank { "NID" }) }
    var docNumber by remember(kyc.documentNumber) { mutableStateOf(kyc.documentNumber) }
    var frontUri by remember { mutableStateOf<Uri?>(null) }
    var backUri by remember { mutableStateOf<Uri?>(null) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val frontPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) frontUri = uri
    }
    val backPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) backUri = uri
    }
    val selfiePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) selfieUri = uri
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
        topBar = { HKPayTopBar(title = "KYC Verification", onBack = onBack) }
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
                    text = "KYC documents are stored in private per-user Firebase Storage (/kyc_uploads) with strict access control."
                )

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Verification Status", style = MaterialTheme.typography.titleMedium)
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
                    Text("Identity Information", style = MaterialTheme.typography.titleMedium)
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
                                Icon(Icons.Rounded.CalendarMonth, contentDescription = "Pick date of birth")
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
                    Text("Document Type", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("NID", "Passport").forEach { type ->
                            FilterChip(
                                selected = docType == type,
                                onClick = { if (canEdit) docType = type },
                                label = { Text(type) },
                                leadingIcon = { Icon(Icons.Rounded.Badge, contentDescription = null) }
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
                    Text("Document Uploads", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(8.dp))

                    KycUploadTile(
                        label = "Document Front Side",
                        selectedUri = frontUri,
                        existingUrl = kyc.frontDocumentUrl,
                        enabled = canEdit,
                        onPick = {
                            frontPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        testTag = "kyc_upload_front_button"
                    )
                    Spacer(Modifier.height(10.dp))
                    KycUploadTile(
                        label = "Document Back Side",
                        selectedUri = backUri,
                        existingUrl = kyc.backDocumentUrl,
                        enabled = canEdit,
                        onPick = {
                            backPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        testTag = "kyc_upload_back_button"
                    )

                    if (state.appSettings.kycSelfieEnabled || kyc.selfieUrl.isNotBlank() || selfieUri != null) {
                        Spacer(Modifier.height(10.dp))
                        KycUploadTile(
                            label = "Selfie Verification",
                            selectedUri = selfieUri,
                            existingUrl = kyc.selfieUrl,
                            enabled = canEdit,
                            icon = Icons.Rounded.CameraFront,
                            onPick = {
                                selfiePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            testTag = "kyc_upload_selfie_button"
                        )
                    } else {
                        Spacer(Modifier.height(10.dp))
                        KycUploadTile(
                            label = "Selfie Verification (Optional)",
                            selectedUri = selfieUri,
                            existingUrl = kyc.selfieUrl,
                            enabled = canEdit,
                            icon = Icons.Rounded.CameraFront,
                            onPick = {
                                selfiePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                            testTag = "kyc_upload_selfie_button"
                        )
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
                            when {
                                fullName.isBlank() -> errorText = "Full name is required."
                                dob.isBlank() -> errorText = "Date of birth is required."
                                country.isBlank() -> errorText = "Country is required."
                                docNumber.isBlank() -> errorText = "$docType number is required."
                                !hasFront -> errorText = "Please upload the front image of your $docType."
                                else -> {
                                    errorText = null
                                    onSubmitKyc(fullName, dob, country, docType, docNumber, frontUri, backUri, selfieUri)
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
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Rounded.UploadFile,
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
    onBack: () -> Unit
) {
    val extra = LocalHKPayColors.current
    var selectedRate by remember(state.currencyRates) {
        mutableStateOf(state.currencyRates.firstOrNull())
    }
    var amountInput by remember { mutableStateOf("100") }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { HKPayTopBar(title = "Currency Exchange Rates", onBack = onBack) }
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
                            icon = Icons.Rounded.CurrencyExchange,
                            title = "Awaiting Live Exchange Rates",
                            subtitle = "Exchange rates (USD/BDT, EUR/BDT, GBP/BDT) sync automatically when configured in Firebase RTDB at /currency_rates."
                        )
                    }
                } else {
                    item {
                        val activeRate: CurrencyRate? = selectedRate ?: state.currencyRates.firstOrNull()
                        GlassCard {
                            Text("Card Purchase Calculator", style = MaterialTheme.typography.titleMedium)
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
                                    Text("Estimated Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
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
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = extra.glassSurface,
                            border = BorderStroke(1.dp, extra.glassBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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
}
