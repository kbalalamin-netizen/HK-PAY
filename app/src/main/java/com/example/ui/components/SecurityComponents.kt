package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.AppLanguage
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHKPayColors
import com.example.util.BiometricAvailability
import com.example.util.BiometricHelper
import com.example.util.HKPayStrings
import com.example.util.LocationCatalog
import java.util.Locale

/**
 * Reusable payment card preview composable with placeholder fields for cardholder name and number.
 */
@Composable
fun PaymentCardPreview(
    cardholderName: String = "CARDHOLDER NAME",
    cardNumberPlaceholder: String = "4532  ••••  ••••  9810",
    expiryPlaceholder: String = "12/29",
    network: String = "VISA",
    cardTitle: String = "HK PAY Titanium",
    isFrozen: Boolean = false,
    modifier: Modifier = Modifier
) {
    HKPayPhysicalCardVisual(
        cardName = cardTitle,
        cardholderName = cardholderName.ifBlank { "CARDHOLDER NAME" },
        cardType = "Physical Card",
        network = network,
        maskedOrFullNumber = cardNumberPlaceholder.ifBlank { "••••  ••••  ••••  ••••" },
        expiryDate = expiryPlaceholder.ifBlank { "MM/YY" },
        statusBadgeText = if (isFrozen) "Frozen" else "Preview",
        isFrozen = isFrozen,
        modifier = modifier
    )
}

/**
 * Unified App Launch Security Screen powered by androidx.biometric and 4-digit PIN lock.
 * Automatically triggers BiometricPrompt on launch when biometric unlock is enabled,
 * displays hardware/enrollment readiness, and supports PIN or Email & Password fallback
 * without wiping session persistence across app restarts.
 */
@Composable
fun AppLockScreen(
    userEmail: String,
    pinEnabled: Boolean,
    biometricEnabled: Boolean,
    errorMessage: String?,
    onUnlockWithPin: (String) -> Unit,
    onPromptBiometric: () -> Unit,
    onSignOut: () -> Unit
) {
    val extra = LocalHKPayColors.current
    val context = LocalContext.current
    val biometricAvailability = remember(context) { BiometricHelper.checkAvailability(context) }
    var pinDigits by remember { mutableStateOf("") }
    var hasAutoPrompted by remember { mutableStateOf(false) }

    LaunchedEffect(biometricEnabled, biometricAvailability) {
        if (biometricEnabled &&
            !hasAutoPrompted &&
            biometricAvailability == BiometricAvailability.AVAILABLE
        ) {
            hasAutoPrompted = true
            onPromptBiometric()
        }
    }

    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(Modifier.height(8.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(74.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(extra.electricBlue.copy(alpha = 0.16f))
                        .border(1.5.dp, extra.cyanGlow.copy(alpha = 0.6f), RoundedCornerShape(22.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.hk_pay_logo),
                        contentDescription = "HK PAY Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(58.dp)
                            .clip(RoundedCornerShape(16.dp))
                    )
                }
                Spacer(Modifier.height(14.dp))
                Text(
                    text = HKPayStrings.appLockTitle(),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = if (pinEnabled && biometricEnabled) {
                        "Verify with Fingerprint / Face ID or enter your 4-digit security PIN."
                    } else if (pinEnabled) {
                        "Enter your 4-digit security PIN to unlock HK PAY."
                    } else {
                        "Confirm your Fingerprint or Face Unlock to sign in to HK PAY."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (userEmail.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.labelMedium,
                        color = extra.cyanGlow
                    )
                }

                if (biometricEnabled) {
                    Spacer(Modifier.height(10.dp))
                    val (bioLabel, bioTint) = when (biometricAvailability) {
                        BiometricAvailability.AVAILABLE -> "Fingerprint / Face Unlock Ready" to extra.success
                        BiometricAvailability.NONE_ENROLLED -> "Enroll Fingerprint or Face in Device Settings" to extra.warning
                        BiometricAvailability.UNAVAILABLE -> "Biometric Sensor Unavailable" to extra.danger
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = bioTint.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, bioTint.copy(alpha = 0.45f)),
                        modifier = Modifier.testTag("applock_biometric_status")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = HKPayIcons.Fingerprint,
                                contentDescription = null,
                                tint = bioTint,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = bioLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = bioTint,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (pinEnabled) {
                    Spacer(Modifier.height(20.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(4) { idx ->
                            val filled = idx < pinDigits.length
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (filled) extra.cyanGlow else extra.glassSurface
                                    )
                                    .border(
                                        1.5.dp,
                                        if (filled) extra.cyanGlow else extra.glassBorder,
                                        CircleShape
                                    )
                            )
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = extra.danger.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, extra.danger.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = errorMessage,
                            color = extra.danger,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }
            }

            if (pinEnabled) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val rows = listOf(
                        listOf("1", "2", "3"),
                        listOf("4", "5", "6"),
                        listOf("7", "8", "9"),
                        listOf(if (biometricEnabled) "BIO" else "CLR", "0", "DEL")
                    )
                    rows.forEach { row ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            row.forEach { key ->
                                Surface(
                                    onClick = {
                                        when (key) {
                                            "DEL" -> if (pinDigits.isNotEmpty()) pinDigits = pinDigits.dropLast(1)
                                            "CLR" -> pinDigits = ""
                                            "BIO" -> onPromptBiometric()
                                            else -> {
                                                if (pinDigits.length < 4) {
                                                    val updated = pinDigits + key
                                                    pinDigits = updated
                                                    if (updated.length == 4) {
                                                        onUnlockWithPin(updated)
                                                        pinDigits = ""
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .size(68.dp)
                                        .testTag("applock_key_$key"),
                                    shape = CircleShape,
                                    color = if (key == "BIO") extra.electricBlue.copy(alpha = 0.22f) else extra.glassSurface,
                                    border = BorderStroke(
                                        1.dp,
                                        if (key == "BIO") extra.cyanGlow.copy(alpha = 0.6f) else extra.glassBorder
                                    )
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        when (key) {
                                            "BIO" -> Icon(
                                                imageVector = HKPayIcons.Fingerprint,
                                                contentDescription = "Biometric Unlock",
                                                tint = extra.cyanGlow,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            "DEL" -> Icon(
                                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                                contentDescription = "Backspace",
                                                tint = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.size(22.dp)
                                            )
                                            "CLR" -> Text(
                                                text = "C",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            else -> Text(
                                                text = key,
                                                style = MaterialTheme.typography.headlineSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Dedicated Biometric Launch Card when biometric-only app lock is active
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(86.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        listOf(
                                            extra.cyanGlow.copy(alpha = 0.28f),
                                            extra.electricBlue.copy(alpha = 0.12f)
                                        )
                                    )
                                )
                                .border(1.5.dp, extra.cyanGlow.copy(alpha = 0.7f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = HKPayIcons.Fingerprint,
                                contentDescription = "Biometric Sensor",
                                tint = extra.cyanGlow,
                                modifier = Modifier.size(44.dp)
                            )
                        }
                        Text(
                            text = "Biometric Vault Protection",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Tap below to verify with your fingerprint sensor or facial recognition.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        HKPayPrimaryButton(
                            text = "Sign In with Fingerprint / Face",
                            onClick = onPromptBiometric,
                            enabled = biometricAvailability == BiometricAvailability.AVAILABLE,
                            icon = HKPayIcons.Fingerprint,
                            testTag = "applock_biometric_button"
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (biometricEnabled && pinEnabled) {
                    OutlinedButton(
                        onClick = onPromptBiometric,
                        enabled = biometricAvailability == BiometricAvailability.AVAILABLE,
                        modifier = Modifier.testTag("applock_biometric_alt_button")
                    ) {
                        Icon(HKPayIcons.Fingerprint, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Use Fingerprint / Face")
                    }
                }
                TextButton(
                    onClick = onSignOut,
                    modifier = Modifier.testTag("applock_signout_button")
                ) {
                    Text("Sign In with Email & Password", color = extra.danger)
                }
            }
        }
    }
}

@Composable
fun TwoFactorVerifyDialog(
    title: String = "2-Factor Verification",
    subtitle: String = "Enter the 6-digit authenticator code from your TOTP app to authorize this action.",
    verifyCode: (String) -> Boolean,
    onVerified: () -> Unit,
    onDismiss: () -> Unit
) {
    val extra = LocalHKPayColors.current
    var code by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(HKPayIcons.Security, contentDescription = null, tint = extra.cyanGlow)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                OutlinedTextField(
                    value = code,
                    onValueChange = { input ->
                        code = input.filter { it.isDigit() }.take(6)
                        errorText = null
                    },
                    label = { Text("6-Digit 2FA Code") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("twofa_verify_input")
                )
                if (!errorText.isNullOrBlank()) {
                    Text(errorText.orEmpty(), color = extra.danger, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            HKPayPrimaryButton(
                text = "Verify 2FA",
                testTag = "twofa_verify_submit",
                onClick = {
                    if (code.length != 6) {
                        errorText = "Enter all 6 digits."
                    } else if (verifyCode(code)) {
                        onVerified()
                    } else {
                        errorText = "Invalid or expired 2FA code. Try again."
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorldwideLocationSelector(
    country: String,
    district: String,
    cityUpazila: String,
    onCountrySelected: (String) -> Unit,
    onDistrictChanged: (String) -> Unit,
    onCityUpazilaChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    enableGpsAutofill: Boolean = true
) {
    val context = LocalContext.current
    val extra = LocalHKPayColors.current
    var showCountryDialog by remember { mutableStateOf(false) }
    var countrySearch by remember { mutableStateOf("") }
    var districtExpanded by remember { mutableStateOf(false) }
    var upazilaExpanded by remember { mutableStateOf(false) }

    val selectedCountryInfo = LocationCatalog.countryFor(country)
    val districtOptions = remember(selectedCountryInfo.name) {
        LocationCatalog.regionsForCountry(selectedCountryInfo.name)
    }
    val subRegionOptions = remember(selectedCountryInfo.name, district) {
        LocationCatalog.subRegionsFor(selectedCountryInfo.name, district)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Worldwide Country & Region",
                style = MaterialTheme.typography.labelLarge,
                color = extra.cyanGlow
            )
            if (enableGpsAutofill) {
                TextButton(
                    onClick = {
                        detectCoarseCountry(context)?.let { detectedCountry ->
                            onCountrySelected(detectedCountry)
                            val firstDistrict = LocationCatalog.regionsForCountry(detectedCountry).firstOrNull().orEmpty()
                            onDistrictChanged(firstDistrict)
                            val firstSub = LocationCatalog.subRegionsFor(detectedCountry, firstDistrict).firstOrNull().orEmpty()
                            onCityUpazilaChanged(firstSub)
                        }
                    },
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.testTag("location_detect_country_button")
                ) {
                    Icon(HKPayIcons.Language, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Detect Region", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        // Country selector card button
        Surface(
            onClick = { showCountryDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("worldwide_country_selector"),
            shape = RoundedCornerShape(14.dp),
            color = extra.glassSurface,
            border = BorderStroke(1.dp, extra.glassBorder)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = selectedCountryInfo.flagEmoji,
                        fontSize = 22.sp
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Country",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${selectedCountryInfo.name} (${selectedCountryInfo.dialCode})",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                Text(
                    text = "Change",
                    style = MaterialTheme.typography.labelMedium,
                    color = extra.cyanGlow
                )
            }
        }

        // Cascading Zila / State dropdown or input
        if (districtOptions.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = districtExpanded,
                onExpandedChange = { districtExpanded = !districtExpanded }
            ) {
                OutlinedTextField(
                    value = district,
                    onValueChange = onDistrictChanged,
                    label = { Text(selectedCountryInfo.regionLabel) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = districtExpanded) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .testTag("worldwide_district_input")
                )
                ExposedDropdownMenu(
                    expanded = districtExpanded,
                    onDismissRequest = { districtExpanded = false }
                ) {
                    districtOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onDistrictChanged(option)
                                val firstSub = LocationCatalog.subRegionsFor(selectedCountryInfo.name, option).firstOrNull().orEmpty()
                                if (firstSub.isNotBlank()) {
                                    onCityUpazilaChanged(firstSub)
                                }
                                districtExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = district,
                onValueChange = onDistrictChanged,
                label = { Text(selectedCountryInfo.regionLabel) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("worldwide_district_input")
            )
        }

        // Cascading Upazila / City dropdown or input
        if (subRegionOptions.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = upazilaExpanded,
                onExpandedChange = { upazilaExpanded = !upazilaExpanded }
            ) {
                OutlinedTextField(
                    value = cityUpazila,
                    onValueChange = onCityUpazilaChanged,
                    label = { Text(selectedCountryInfo.subRegionLabel) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = upazilaExpanded) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryEditable)
                        .testTag("worldwide_upazila_input")
                )
                ExposedDropdownMenu(
                    expanded = upazilaExpanded,
                    onDismissRequest = { upazilaExpanded = false }
                ) {
                    subRegionOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onCityUpazilaChanged(option)
                                upazilaExpanded = false
                            }
                        )
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = cityUpazila,
                onValueChange = onCityUpazilaChanged,
                label = { Text(selectedCountryInfo.subRegionLabel) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("worldwide_upazila_input")
            )
        }
    }

    if (showCountryDialog) {
        val filtered = remember(countrySearch) {
            val q = countrySearch.trim().lowercase(Locale.US)
            if (q.isBlank()) LocationCatalog.countries
            else LocationCatalog.countries.filter {
                it.name.lowercase(Locale.US).contains(q) ||
                    it.isoCode.lowercase(Locale.US).contains(q) ||
                    it.dialCode.contains(q)
            }
        }
        AlertDialog(
            onDismissRequest = { showCountryDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(22.dp),
            title = { Text("Select Country", style = MaterialTheme.typography.titleMedium) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = countrySearch,
                        onValueChange = { countrySearch = it },
                        label = { Text("Search country or dial code") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered, key = { it.isoCode }) { item ->
                            val isSelected = item.name.equals(selectedCountryInfo.name, ignoreCase = true)
                            Surface(
                                onClick = {
                                    onCountrySelected(item.name)
                                    val firstRegion = LocationCatalog.regionsForCountry(item.name).firstOrNull().orEmpty()
                                    onDistrictChanged(firstRegion)
                                    val firstSub = LocationCatalog.subRegionsFor(item.name, firstRegion).firstOrNull().orEmpty()
                                    onCityUpazilaChanged(firstSub)
                                    showCountryDialog = false
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) extra.electricBlue.copy(alpha = 0.2f) else extra.glassSurface,
                                border = BorderStroke(1.dp, if (isSelected) extra.cyanGlow else extra.glassBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(item.flagEmoji, fontSize = 20.sp)
                                        Spacer(Modifier.width(10.dp))
                                        Text(
                                            text = item.name,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Text(
                                        text = item.dialCode,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = extra.cyanGlow
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showCountryDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun LanguageSelectorDialog(
    currentLanguage: AppLanguage,
    onSelectLanguage: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    val extra = LocalHKPayColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(22.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(HKPayIcons.Language, contentDescription = null, tint = extra.cyanGlow)
                Spacer(Modifier.width(8.dp))
                Text(HKPayStrings.languageLabel(), style = MaterialTheme.typography.titleMedium)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { lang ->
                    val selected = lang == currentLanguage
                    Surface(
                        onClick = {
                            onSelectLanguage(lang)
                            onDismiss()
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = if (selected) extra.electricBlue.copy(alpha = 0.2f) else extra.glassSurface,
                        border = BorderStroke(1.dp, if (selected) extra.cyanGlow else extra.glassBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("language_option_${lang.code}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(lang.flagEmoji, fontSize = 20.sp)
                                Spacer(Modifier.width(10.dp))
                                Text(
                                    text = "${lang.code}  •  ${lang.displayName}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (selected) {
                                Text(
                                    text = "ACTIVE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extra.cyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@Suppress("DEPRECATION")
private fun detectCoarseCountry(context: Context): String? {
    val hasCoarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    if (!hasCoarse) {
        val localeCountry = Locale.getDefault().displayCountry
        return LocationCatalog.countries.firstOrNull {
            it.name.equals(localeCountry, ignoreCase = true) ||
                it.isoCode.equals(Locale.getDefault().country, ignoreCase = true)
        }?.name
    }
    return runCatching {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        val loc = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            ?: lm?.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        if (loc != null && Geocoder.isPresent()) {
            val addresses = Geocoder(context, Locale.US).getFromLocation(loc.latitude, loc.longitude, 1)
            val countryName = addresses?.firstOrNull()?.countryName
            val countryCode = addresses?.firstOrNull()?.countryCode
            LocationCatalog.countries.firstOrNull {
                it.name.equals(countryName, ignoreCase = true) ||
                    it.isoCode.equals(countryCode, ignoreCase = true)
            }?.name ?: countryName
        } else {
            val iso = Locale.getDefault().country
            LocationCatalog.countries.firstOrNull { it.isoCode.equals(iso, ignoreCase = true) }?.name
        }
    }.getOrNull()
}
