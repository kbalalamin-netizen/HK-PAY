package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import com.example.BuildConfig
import com.example.R
import com.example.data.model.ThemeMode
import com.example.ui.components.CopyableChip
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayBackdrop
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.HKPayTopBar
import com.example.ui.components.SecurityNoticeBanner
import com.example.ui.components.StatusBadge
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalHKPayColors
import com.example.util.BiometricHelper
import com.example.util.SecurityUtils
import kotlinx.coroutines.delay

@Composable
fun PinLockSettingsScreen(
    pinLockEnabled: Boolean,
    biometricEnabled: Boolean,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSaveOrChangePin: (currentPin: String?, newPin: String, confirmPin: String) -> Unit,
    onDisablePinLock: (currentPin: String) -> Unit,
    onToggleBiometric: (Boolean) -> Unit,
    onShowMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val extra = LocalHKPayColors.current

    var currentPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }

    HKPayBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HKPayTopBar(
                    title = "App Lock & PIN Settings",
                    onBack = onBack,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                SecurityNoticeBanner(
                    text = "When App Lock is enabled, exiting HK PAY or restarting your phone keeps your Firebase session signed in while protecting access with your 4-digit PIN or Biometric unlock."
                )

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "4-Digit PIN Lock Status",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (pinLockEnabled) "Active on app resume & device restart" else "Not configured",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = if (pinLockEnabled) "Active" else "Inactive")
                    }
                    Spacer(Modifier.height(14.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Biometric Alternative Unlock",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                text = "Unlock with fingerprint or face alongside your 4-digit PIN",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = biometricEnabled,
                            onCheckedChange = { wantEnable ->
                                if (wantEnable && activity != null) {
                                    BiometricHelper.authenticate(
                                        activity = activity,
                                        title = "Enable Biometric Unlock",
                                        subtitle = "Verify fingerprint or face to enable",
                                        onSuccess = { onToggleBiometric(true) },
                                        onError = { msg -> onShowMessage(msg) }
                                    )
                                } else {
                                    onToggleBiometric(false)
                                }
                            },
                            modifier = Modifier.testTag("pin_settings_biometric_switch")
                        )
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (pinLockEnabled) "Change 4-Digit PIN" else "Set Up 4-Digit App Lock PIN",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extra.cyanGlow
                    )
                    Spacer(Modifier.height(12.dp))
                    if (pinLockEnabled) {
                        OutlinedTextField(
                            value = currentPin,
                            onValueChange = { currentPin = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("Current 4-Digit PIN") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("pin_settings_current_input")
                        )
                        Spacer(Modifier.height(10.dp))
                    }
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("New 4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_settings_new_input")
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it.filter { c -> c.isDigit() }.take(4) },
                        label = { Text("Confirm 4-Digit PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pin_settings_confirm_input")
                    )
                    Spacer(Modifier.height(16.dp))
                    HKPayPrimaryButton(
                        text = if (pinLockEnabled) "Update 4-Digit PIN" else "Enable 4-Digit PIN Lock",
                        icon = HKPayIcons.PinLock,
                        testTag = "pin_settings_save_button",
                        onClick = {
                            onSaveOrChangePin(currentPin.takeIf { pinLockEnabled }, newPin, confirmPin)
                            currentPin = ""
                            newPin = ""
                            confirmPin = ""
                        }
                    )
                    if (pinLockEnabled) {
                        Spacer(Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                if (currentPin.length != 4) {
                                    onShowMessage("Enter your current 4-digit PIN above to disable PIN Lock.")
                                } else {
                                    onDisablePinLock(currentPin)
                                    currentPin = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("pin_settings_disable_button"),
                            border = BorderStroke(1.dp, extra.danger.copy(alpha = 0.6f))
                        ) {
                            Text("Disable PIN Lock", color = extra.danger)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TwoFactorSetupScreen(
    userEmail: String,
    twoFactorEnabled: Boolean,
    savedSecret: String,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onEnableTwoFactor: (secret: String, verificationCode: String) -> Unit,
    onDisableTwoFactor: (verificationCode: String) -> Unit,
    onShowMessage: (String) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val context = LocalContext.current
    val extra = LocalHKPayColors.current

    val generatedSecret = remember(savedSecret) {
        savedSecret.ifBlank { SecurityUtils.generateTotpSecret() }
    }
    var verificationCode by remember { mutableStateOf("") }
    var currentEpoch by remember { mutableIntStateOf((System.currentTimeMillis() / 1000L).toInt()) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            currentEpoch = (System.currentTimeMillis() / 1000L).toInt()
        }
    }

    val secondsRemaining = 30 - (currentEpoch % 30)
    val livePreviewCode = remember(generatedSecret, currentEpoch / 30) {
        SecurityUtils.generateTotpCode(generatedSecret)
    }
    val otpAuthUri = remember(generatedSecret, userEmail) {
        SecurityUtils.buildOtpAuthUri(userEmail.ifBlank { "member@hkpay" }, generatedSecret)
    }

    HKPayBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HKPayTopBar(
                    title = "2-Factor Authentication",
                    onBack = onBack,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "TOTP 2FA Protection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (twoFactorEnabled) "Active on card reveals, security & freezes" else "Add an extra 6-digit authenticator layer",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        StatusBadge(status = if (twoFactorEnabled) "Active" else "Not Enabled")
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (twoFactorEnabled) "Authenticator Key (Linked)" else "Step 1: Add Key to Authenticator App",
                        style = MaterialTheme.typography.titleSmall,
                        color = extra.cyanGlow,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Copy this RFC 6238 secret key into Google Authenticator, Authy, or 1Password, or test with the live sync token below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    CopyableChip(
                        label = "Secret Key",
                        value = generatedSecret,
                        onCopy = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("HK PAY 2FA Secret", generatedSecret))
                            onShowMessage("2FA secret key copied.")
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    CopyableChip(
                        label = "Copy Setup URI",
                        value = "otpauth://totp/HK%20PAY",
                        onCopy = {
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cb.setPrimaryClip(ClipData.newPlainText("HK PAY 2FA URI", otpAuthUri))
                            onShowMessage("otpauth:// URI copied.")
                        }
                    )
                    Spacer(Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = extra.electricBlue.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Live 30s Sync Verification Code",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = extra.cyanGlow
                                )
                                Text(
                                    text = livePreviewCode.chunked(3).joinToString(" "),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = extra.glassSurface
                            ) {
                                Text(
                                    text = "${secondsRemaining}s",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = extra.cyanGlow,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (twoFactorEnabled) "Enter 6-Digit Code to Disable 2FA" else "Step 2: Confirm 6-Digit Code",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = verificationCode,
                        onValueChange = { verificationCode = it.filter { c -> c.isDigit() }.take(6) },
                        label = { Text("6-Digit 2FA Code") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("twofa_setup_code_input")
                    )
                    Spacer(Modifier.height(14.dp))
                    if (twoFactorEnabled) {
                        OutlinedButton(
                            onClick = {
                                onDisableTwoFactor(verificationCode)
                                verificationCode = ""
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("twofa_disable_button"),
                            border = BorderStroke(1.dp, extra.danger.copy(alpha = 0.6f))
                        ) {
                            Text("Disable 2-Factor Auth", color = extra.danger)
                        }
                    } else {
                        HKPayPrimaryButton(
                            text = "Activate 2-Factor Auth",
                            icon = HKPayIcons.Security,
                            testTag = "twofa_enable_button",
                            onClick = {
                                onEnableTwoFactor(generatedSecret, verificationCode)
                                verificationCode = ""
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PrivacyPolicyScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    HKPayLegalDocumentScreen(
        title = "Privacy Policy",
        subtitle = "HK PAY • Operated by HK IT LAB",
        sections = listOf(
            "1. Data We Collect" to "We collect only information necessary to verify identity and issue physical payment cards: your legal name, email address, phone number, worldwide delivery address (Country, Zila/State, Upazila/City), and encrypted KYC documents.",
            "2. Camera, Location & Contacts" to "Camera access is used solely during live document scanning and directional face liveness checks. Coarse location assists with country and regional card eligibility. Contacts access is optional for referral invitations. Media files use Android's zero-permission Photo Picker.",
            "3. App Security & Encryption" to "Authentication tokens use Firebase Auth encryption, KYC documents are stored in private Firebase Storage buckets, and App Lock 4-digit PINs are hashed with per-user cryptographic salts (SHA-256) strictly on your device.",
            "4. Screenshot & Copy Protection" to "To guard sensitive financial balances and cardholder activity, screen capture and casual text copying on the Dashboard are restricted by hardware-backed window security.",
            "5. Retention & Account Rights" to "You may update delivery details in Edit Profile or contact support at any time regarding KYC records and account requests."
        ),
        themeMode = themeMode,
        onToggleTheme = onToggleTheme,
        onBack = onBack
    )
}

@Composable
fun TermsScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    HKPayLegalDocumentScreen(
        title = "Terms & Conditions",
        subtitle = "HK PAY Cardholder Service Agreement",
        sections = listOf(
            "1. Eligibility & KYC Compliance" to "Physical HK PAY payment cards require valid identity verification (NID or Passport) and live directional face verification. Accounts are restricted to one verified holder per identity.",
            "2. Card Orders & Delivery" to "Order pricing, issuing fees, and delivery charges are shown transparently prior to confirmation. Courier tracking updates in real time through Firebase.",
            "3. Card Freeze & 2FA Controls" to "Cardholders must promptly freeze lost or compromised cards via the My Cards dashboard and enable 4-digit App Lock PIN or 2-Factor Authentication.",
            "4. Referral Program Credits" to "Referral discounts and bonuses apply exclusively to genuine new cardholders redeeming valid HK PAY referral codes.",
            "5. Platform Governance" to "HK IT LAB and HK PAY issuing partners reserve the right to pause compromised cards or reject fraudulent KYC submissions to protect cardholders."
        ),
        themeMode = themeMode,
        onToggleTheme = onToggleTheme,
        onBack = onBack
    )
}

@Composable
fun AboutScreen(
    supportEmail: String,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }
    val extra = LocalHKPayColors.current

    HKPayBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HKPayTopBar(
                    title = "About HK PAY",
                    onBack = onBack,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.drawable.hk_pay_logo),
                            contentDescription = "HK PAY Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(16.dp))
                        )
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "HK PAY",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Developed by HK IT LAB",
                                style = MaterialTheme.typography.titleSmall,
                                color = extra.cyanGlow,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(Modifier.height(14.dp))
                    Text(
                        text = "HK IT LAB builds high-trust mobile fintech, cross-border physical card platforms, and real-time cryptographic security systems designed for global cardholders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                GlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Core Platform Architecture",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = extra.cyanGlow
                    )
                    Spacer(Modifier.height(10.dp))
                    AboutFeatureRow("Organization", "HK IT LAB")
                    AboutFeatureRow("Platform", "HK PAY Global Physical Card Network")
                    AboutFeatureRow("Security Stack", "4-Digit PIN Lock • Biometric • TOTP 2FA")
                    AboutFeatureRow("Verification", "Live CameraX NID Scan & Directional Liveness")
                    AboutFeatureRow("Card Protection", "Instant Card Freeze & Screenshot Shield")
                    AboutFeatureRow("Support Desk", supportEmail.ifBlank { "support@hkitlab.com" })
                }
            }
        }
    }
}

@Composable
private fun AboutFeatureRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun HKPayLegalDocumentScreen(
    title: String,
    subtitle: String,
    sections: List<Pair<String, String>>,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onBack: () -> Unit
) {
    val extra = LocalHKPayColors.current
    HKPayBackdrop {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                HKPayTopBar(
                    title = title,
                    onBack = onBack,
                    themeMode = themeMode,
                    onToggleTheme = onToggleTheme
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = extra.electricBlue.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.35f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.titleSmall,
                        color = extra.cyanGlow,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(14.dp)
                    )
                }

                sections.forEach { (heading, body) ->
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = heading,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = body,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
