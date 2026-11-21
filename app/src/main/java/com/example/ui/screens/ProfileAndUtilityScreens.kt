package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import coil.compose.AsyncImage
import com.example.data.model.NotificationPreferences
import com.example.data.model.ThemeMode
import com.example.ui.components.CopyableChip
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.StatusBadge
import com.example.ui.components.ThemeCycleIconButton
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.MainDashboardState
import com.example.util.BiometricHelper
import com.example.util.FormatUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    biometricEnabled: Boolean,
    onChangeTheme: (ThemeMode) -> Unit,
    onToggleTheme: () -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: (String, String) -> Unit,
    onNavigateKyc: () -> Unit,
    onNavigateReferrals: () -> Unit,
    onNavigateTutorials: () -> Unit,
    onNavigateNotificationSettings: () -> Unit,
    onNavigateTransactions: () -> Unit,
    onNavigateCurrencyRates: () -> Unit,
    onNavigateSupport: () -> Unit,
    onLogout: () -> Unit,
    onCopied: (String) -> Unit
) {
    val extra = LocalHKPayColors.current
    val clipboard = LocalClipboardManager.current
    val context = LocalContext.current
    val profile = state.userProfile

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showLogoutConfirm by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onUploadPhoto(uri)
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onSubmit = { current, next ->
                showChangePasswordDialog = false
                onChangePassword(current, next)
            }
        )
    }

    if (showLogoutConfirm) {
        AlertDialog(
            onDismissRequest = { showLogoutConfirm = false },
            title = { Text("Sign Out of HK PAY?") },
            text = { Text("Your active session will be cleared securely from this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLogoutConfirm = false
                        onLogout()
                    },
                    modifier = Modifier.testTag("profile_confirm_logout_button")
                ) {
                    Text("Sign Out", color = extra.danger)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Profile & Settings",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Profile completion: ${profile.completionPercent}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = extra.cyanGlow
                        )
                    }
                    ThemeCycleIconButton(
                        themeMode = themeMode,
                        onToggleTheme = onToggleTheme
                    )
                }
            }

            item {
                GlassCard {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(92.dp),
                                shape = CircleShape,
                                border = BorderStroke(2.dp, extra.cyanGlow),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                if (profile.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = profile.photoUrl,
                                        contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Rounded.Person,
                                            contentDescription = "Avatar",
                                            tint = extra.cyanGlow,
                                            modifier = Modifier.size(44.dp)
                                        )
                                    }
                                }
                            }
                            Surface(
                                onClick = {
                                    avatarPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = CircleShape,
                                color = extra.electricBlue,
                                modifier = Modifier
                                    .size(34.dp)
                                    .testTag("profile_update_photo_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = HKPayIcons.CameraAlt,
                                        contentDescription = "Update photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = profile.fullName.ifBlank { "HK PAY Member" },
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = profile.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            CopyableChip(
                                label = "User ID",
                                value = profile.publicUserId,
                                onCopy = {
                                    clipboard.setText(AnnotatedString(profile.publicUserId))
                                    onCopied("User ID copied")
                                }
                            )
                            if (profile.referralCode.isNotBlank()) {
                                CopyableChip(
                                    label = "Ref",
                                    value = profile.referralCode,
                                    onCopy = {
                                        clipboard.setText(AnnotatedString(profile.referralCode))
                                        onCopied("Referral code copied")
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(status = "Account: ${profile.accountStatus}")
                            StatusBadge(status = "KYC: ${state.effectiveKycStatus}")
                        }
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { profile.completionPercent / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(50)),
                            color = extra.cyanGlow,
                            trackColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    PriceLine("Phone", profile.phone.ifBlank { "Not set" })
                    PriceLine("NID Number", profile.nidNumber.ifBlank { "Not set" })
                    PriceLine("Liveness Check", if (profile.livenessVerified) "Completed" else "Pending")
                    PriceLine("City / Country", listOf(profile.cityUpazila, profile.country).filter { it.isNotBlank() }.joinToString(", ").ifBlank { "Bangladesh" })
                    PriceLine("Preferred Currency", profile.preferredCurrency.ifBlank { "USD" })
                    PriceLine("Join Date", FormatUtils.formatDate(profile.joinDate))
                    PriceLine("Available Fee Credit", FormatUtils.formatMoney(state.availableReferralCredit, "USD"))
                }
            }

            item {
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f).padding(end = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(HKPayIcons.Fingerprint, contentDescription = null, tint = extra.cyanGlow)
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(
                                    "Biometric Unlock",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Use Fingerprint or Face Unlock at launch and before sensitive actions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable) {
                                    val activity = context as? FragmentActivity
                                    if (activity != null) {
                                        BiometricHelper.authenticate(
                                            activity = activity,
                                            title = "Enable Biometric Unlock",
                                            subtitle = "Verify your fingerprint or face ID to turn on HK PAY lock",
                                            negativeButtonText = "Cancel",
                                            onSuccess = { onToggleBiometric(true) },
                                            onError = { msg -> onCopied(msg) }
                                        )
                                    } else {
                                        onCopied("Biometric prompt requires an active screen.")
                                    }
                                } else {
                                    onToggleBiometric(false)
                                }
                            },
                            modifier = Modifier.testTag("profile_biometric_switch")
                        )
                    }
                }
            }

            item {
                GlassCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(HKPayIcons.Palette, contentDescription = null, tint = extra.cyanGlow)
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "App Theme (Dark / Light / Navy)",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        ThemeMode.entries.forEach { mode ->
                            val modeIcon = when (mode) {
                                ThemeMode.DARK -> HKPayIcons.DarkMode
                                ThemeMode.LIGHT -> HKPayIcons.LightMode
                                ThemeMode.NAVY -> HKPayIcons.NavyTheme
                            }
                            FilterChip(
                                selected = themeMode == mode,
                                onClick = { onChangeTheme(mode) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = modeIcon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(mode.label) }
                            )
                        }
                    }
                }
            }

            item {
                GlassCard(contentPadding = PaddingValues(vertical = 6.dp)) {
                    ProfileActionRow(
                        icon = Icons.Rounded.Edit,
                        title = "Edit Profile",
                        subtitle = "Personal info, NID, occupation, currency & full address (${profile.completionPercent}%)",
                        onClick = onEditProfile,
                        testTag = "profile_edit_button"
                    )
                    if (state.appSettings.referralProgramEnabled) {
                        ProfileActionRow(
                            icon = HKPayIcons.CardGiftcard,
                            title = "Referral Program",
                            subtitle = "Code: ${profile.referralCode.ifBlank { "HKR" }} • ${state.referrals.size} joined",
                            onClick = onNavigateReferrals,
                            testTag = "profile_referral_button"
                        )
                    }
                    ProfileActionRow(
                        icon = HKPayIcons.PlayLesson,
                        title = "In-App Tutorials",
                        subtitle = "Guided walkthrough for cards, orders, and KYC",
                        onClick = onNavigateTutorials,
                        testTag = "profile_tutorials_button"
                    )
                    ProfileActionRow(
                        icon = HKPayIcons.VerifiedUser,
                        title = "KYC & Liveness Verification",
                        subtitle = "Status: ${state.effectiveKycStatus}",
                        onClick = onNavigateKyc,
                        testTag = "profile_kyc_button"
                    )
                    ProfileActionRow(
                        icon = HKPayIcons.LockReset,
                        title = "Change Password",
                        subtitle = "Update your account password securely",
                        onClick = {
                            val activity = context as? FragmentActivity
                            if (biometricEnabled && activity != null) {
                                BiometricHelper.authenticate(
                                    activity = activity,
                                    title = "Verify Identity",
                                    subtitle = "Confirm biometrics before changing your password",
                                    negativeButtonText = "Cancel",
                                    onSuccess = { showChangePasswordDialog = true },
                                    onError = { msg -> onCopied(msg) }
                                )
                            } else {
                                showChangePasswordDialog = true
                            }
                        },
                        testTag = "profile_change_password_button"
                    )
                    ProfileActionRow(
                        icon = HKPayIcons.NotificationsActive,
                        title = "Notifications Settings",
                        subtitle = "Manage order, security, and rate alerts",
                        onClick = onNavigateNotificationSettings,
                        testTag = "profile_notif_settings_button"
                    )
                    ProfileActionRow(
                        icon = HKPayIcons.ReceiptLong,
                        title = "Transaction History",
                        subtitle = "View card activity, fees, payments, and refunds",
                        onClick = onNavigateTransactions,
                        testTag = "profile_transactions_button"
                    )
                    ProfileActionRow(
                        icon = HKPayIcons.CurrencyExchange,
                        title = "Currency Exchange Rates",
                        subtitle = "Live USD, EUR, GBP to BDT rates",
                        onClick = onNavigateCurrencyRates,
                        testTag = "profile_rates_button"
                    )
                    ProfileActionRow(
                        icon = HKPayIcons.SupportAgent,
                        title = "Customer Support",
                        subtitle = "Create and track support tickets",
                        onClick = onNavigateSupport,
                        testTag = "profile_support_button"
                    )
                }
            }

            item {
                OutlinedButton(
                    onClick = { showLogoutConfirm = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("profile_logout_button"),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, extra.danger.copy(alpha = 0.6f))
                ) {
                    Icon(HKPayIcons.Logout, contentDescription = null, tint = extra.danger)
                    Spacer(Modifier.width(8.dp))
                    Text("Sign Out", color = extra.danger)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun ProfileActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    val extra = LocalHKPayColors.current
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = extra.cyanGlow)
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String) -> Unit
) {
    var current by remember { mutableStateOf("") }
    var next by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Password") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                if (!error.isNullOrBlank()) {
                    Text(error.orEmpty(), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                OutlinedTextField(
                    value = current,
                    onValueChange = { current = it },
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = next,
                    onValueChange = { next = it },
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = confirm,
                    onValueChange = { confirm = it },
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    current.isBlank() -> error = "Enter your current password."
                    next.length < 6 -> error = "New password must be at least 6 characters."
                    next != confirm -> error = "New passwords do not match."
                    else -> onSubmit(current, next)
                }
            }) { Text("Update") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditProfileScreen(
    state: MainDashboardState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onUploadPhoto: (Uri) -> Unit,
    onBack: () -> Unit,
    onSave: (
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
    ) -> Unit
) {
    val extra = LocalHKPayColors.current
    val profile = state.userProfile

    var fullName by remember(profile.fullName) { mutableStateOf(profile.fullName) }
    var phone by remember(profile.phone) { mutableStateOf(profile.phone) }
    var country by remember(profile.country) { mutableStateOf(profile.country.ifBlank { "Bangladesh" }) }
    var dateOfBirth by remember(profile.dateOfBirth) { mutableStateOf(profile.dateOfBirth) }
    var gender by remember(profile.gender) { mutableStateOf(profile.gender.ifBlank { "Male" }) }
    var nidNumber by remember(profile.nidNumber) { mutableStateOf(profile.nidNumber) }
    var occupation by remember(profile.occupation) { mutableStateOf(profile.occupation) }
    var monthlyIncomeRange by remember(profile.monthlyIncomeRange) {
        mutableStateOf(profile.monthlyIncomeRange.ifBlank { "$500 - $1,500" })
    }
    var preferredCurrency by remember(profile.preferredCurrency) {
        mutableStateOf(profile.preferredCurrency.ifBlank { "USD" })
    }
    var streetAddress by remember(profile.streetAddress) { mutableStateOf(profile.streetAddress) }
    var cityUpazila by remember(profile.cityUpazila) { mutableStateOf(profile.cityUpazila) }
    var district by remember(profile.district) { mutableStateOf(profile.district) }
    var postalCode by remember(profile.postalCode) { mutableStateOf(profile.postalCode) }
    var showDatePicker by remember { mutableStateOf(false) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onUploadPhoto(uri)
    }

    if (showDatePicker) {
        val dateState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { millis ->
                        dateOfBirth = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(millis))
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
                title = "Premium Profile",
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
                    .widthIn(max = 680.dp)
                    .fillMaxSize()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Completion Header & Avatar Card
                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            Surface(
                                modifier = Modifier.size(68.dp),
                                shape = CircleShape,
                                border = BorderStroke(2.dp, extra.cyanGlow),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                if (profile.photoUrl.isNotBlank()) {
                                    AsyncImage(
                                        model = profile.photoUrl,
                                        contentDescription = "Profile photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Rounded.Person, contentDescription = null, tint = extra.cyanGlow, modifier = Modifier.size(34.dp))
                                    }
                                }
                            }
                            Surface(
                                onClick = {
                                    avatarPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                },
                                shape = CircleShape,
                                color = extra.electricBlue,
                                modifier = Modifier.size(26.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(HKPayIcons.CameraAlt, contentDescription = "Change photo", tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "Profile Strength",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${profile.completionPercent}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = extra.cyanGlow,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { profile.completionPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(50)),
                                color = extra.cyanGlow,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Complete all KYC & address fields for instant physical card dispatch.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Personal & Identity Details
                GlassCard {
                    Text(
                        "Personal & Identity Details",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Legal Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_name_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number (No OTP)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_phone_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = dateOfBirth,
                        onValueChange = { dateOfBirth = it },
                        label = { Text("Date of Birth (YYYY-MM-DD)") },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(HKPayIcons.CalendarMonth, contentDescription = "Pick Date of Birth")
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_dob_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Gender", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female", "Other").forEach { item ->
                            FilterChip(
                                selected = gender == item,
                                onClick = { gender = item },
                                label = { Text(item) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = nidNumber,
                        onValueChange = { nidNumber = it },
                        label = { Text("National ID (NID) Number") },
                        leadingIcon = { Icon(HKPayIcons.Badge, contentDescription = null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_nid_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                }

                // Financial Profile & Preferences
                GlassCard {
                    Text(
                        "Financial & Card Preferences",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = occupation,
                        onValueChange = { occupation = it },
                        label = { Text("Occupation / Profession") },
                        placeholder = { Text("e.g. Software Engineer, Business Owner") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_occupation_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text("Monthly Income Range", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Under $500", "$500 - $1,500", "$1,500 - $3,500", "$3,500+").forEach { range ->
                            FilterChip(
                                selected = monthlyIncomeRange == range,
                                onClick = { monthlyIncomeRange = range },
                                label = { Text(range) }
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text("Preferred Display Currency", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("USD", "BDT", "EUR", "GBP").forEach { curr ->
                            FilterChip(
                                selected = preferredCurrency == curr,
                                onClick = { preferredCurrency = curr },
                                label = { Text(curr) }
                            )
                        }
                    }
                }

                // Delivery Address
                GlassCard {
                    Text(
                        "Physical Card Delivery Address",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = streetAddress,
                        onValueChange = { streetAddress = it },
                        label = { Text("Street Address / House & Road") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth().testTag("edit_profile_address_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = cityUpazila,
                            onValueChange = { cityUpazila = it },
                            label = { Text("City / Upazila") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_profile_city_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = district,
                            onValueChange = { district = it },
                            label = { Text("District / State") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_profile_district_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = postalCode,
                            onValueChange = { postalCode = it },
                            label = { Text("Postal Code") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_profile_postal_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                        OutlinedTextField(
                            value = country,
                            onValueChange = { country = it },
                            label = { Text("Country") },
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("edit_profile_country_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                    Spacer(Modifier.height(18.dp))
                    HKPayPrimaryButton(
                        text = "Save Premium Profile",
                        onClick = {
                            onSave(
                                fullName,
                                phone,
                                country,
                                dateOfBirth,
                                gender,
                                nidNumber,
                                occupation,
                                monthlyIncomeRange,
                                preferredCurrency,
                                streetAddress,
                                cityUpazila,
                                district,
                                postalCode
                            )
                        },
                        isLoading = state.isSubmitting,
                        enabled = fullName.isNotBlank(),
                        testTag = "edit_profile_save_button"
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun NotificationSettingsScreen(
    prefs: NotificationPreferences,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit,
    onSave: (NotificationPreferences) -> Unit
) {
    var orderUpdates by remember(prefs.orderUpdates) { mutableStateOf(prefs.orderUpdates) }
    var securityAlerts by remember(prefs.securityAlerts) { mutableStateOf(prefs.securityAlerts) }
    var rateAlerts by remember(prefs.rateAlerts) { mutableStateOf(prefs.rateAlerts) }
    var marketingNotices by remember(prefs.marketingNotices) { mutableStateOf(prefs.marketingNotices) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            HKPayTopBar(
                title = "Notification Settings",
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
                    .widthIn(max = 620.dp)
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassCard {
                    ToggleSettingRow(
                        title = "Order & Card Status Updates",
                        subtitle = "Personalized push notifications for payment, shipping, and activation",
                        checked = orderUpdates,
                        onCheckedChange = { orderUpdates = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ToggleSettingRow(
                        title = "Security & KYC Alerts",
                        subtitle = "KYC verification updates and account security notices",
                        checked = securityAlerts,
                        onCheckedChange = { securityAlerts = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ToggleSettingRow(
                        title = "Currency Rate Alerts",
                        subtitle = "Notifications when exchange rates update",
                        checked = rateAlerts,
                        onCheckedChange = { rateAlerts = it }
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    ToggleSettingRow(
                        title = "System Announcements",
                        subtitle = "New physical card packages and platform notices",
                        checked = marketingNotices,
                        onCheckedChange = { marketingNotices = it }
                    )
                    Spacer(Modifier.height(16.dp))
                    HKPayPrimaryButton(
                        text = "Save Preferences",
                        onClick = {
                            onSave(
                                NotificationPreferences(
                                    orderUpdates = orderUpdates,
                                    securityAlerts = securityAlerts,
                                    rateAlerts = rateAlerts,
                                    marketingNotices = marketingNotices
                                )
                            )
                        },
                        testTag = "notif_settings_save_button"
                    )
                }
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
