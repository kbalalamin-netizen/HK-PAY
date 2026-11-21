package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.AppSettings
import com.example.data.model.ThemeMode
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayBackdrop
import com.example.ui.components.HKPayPrimaryButton
import com.example.ui.components.ThemeCycleIconButton
import com.example.ui.components.WorldwideLocationSelector
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.AuthUiState
import com.example.util.FormatUtils
import com.example.util.SecurityUtils
import androidx.compose.material3.Switch
import androidx.compose.material3.FilterChip
import java.io.File

private val LIVENESS_CHALLENGES = listOf(
    "Look directly into the camera and smile naturally",
    "Hold camera at eye level and tilt slightly left",
    "Look straight ahead with eyes wide open in bright light"
)

@Composable
fun SplashScreen() {
    val extra = LocalHKPayColors.current
    var started by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (started) 1f else 0.88f,
        animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing),
        label = "splash_logo_scale"
    )
    val pulse by rememberInfiniteTransition(label = "glow_pulse").animateFloat(
        initialValue = 0.65f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    LaunchedEffect(Unit) { started = true }

    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(
                                    extra.cyanGlow.copy(alpha = 0.28f * pulse),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Image(
                    painter = painterResource(id = R.drawable.hk_pay_logo),
                    contentDescription = "HK PAY Brand Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(132.dp)
                        .scale(scale)
                        .clip(RoundedCornerShape(30.dp))
                )
            }
            Spacer(Modifier.height(22.dp))
            Text(
                text = "HK PAY",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "PHYSICAL PAYMENT CARDS",
                style = MaterialTheme.typography.labelMedium,
                color = extra.cyanGlow
            )
            Spacer(Modifier.height(30.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(26.dp),
                strokeWidth = 2.6.dp,
                color = extra.cyanGlow
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSignInClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val extra = LocalHKPayColors.current
    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.widthIn(max = 520.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HK PAY",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = extra.cyanGlow
                        )
                        ThemeCycleIconButton(
                            themeMode = themeMode,
                            onToggleTheme = onToggleTheme
                        )
                    }
                    Spacer(Modifier.height(14.dp))
                    Image(
                        painter = painterResource(id = R.drawable.hk_pay_logo),
                        contentDescription = "HK PAY Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(110.dp)
                            .clip(RoundedCornerShape(26.dp))
                    )
                    Spacer(Modifier.height(20.dp))
                    Text(
                        text = "Global Physical Cards,\nDelivered to Your Door",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "Order verified international payment cards with real-time tracking, transparent USD-to-BDT rates, and NID + Liveness security.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(26.dp))

                    GlassCard {
                        WelcomeFeatureRow(
                            icon = HKPayIcons.CreditCard,
                            title = "Physical Card Issuance",
                            subtitle = "Browse live packages and order physical cards with courier tracking."
                        )
                        Spacer(Modifier.height(14.dp))
                        WelcomeFeatureRow(
                            icon = HKPayIcons.Security,
                            title = "NID & Live Liveness Security",
                            subtitle = "Email link check, NID verification, and instant live selfie capture."
                        )
                        Spacer(Modifier.height(14.dp))
                        WelcomeFeatureRow(
                            icon = HKPayIcons.Speed,
                            title = "Fast Cloud Sync & Controls",
                            subtitle = "Instant sign-in, linked card balance view, and live FX rates."
                        )
                    }

                    Spacer(Modifier.height(26.dp))
                    HKPayPrimaryButton(
                        text = "Sign In to HK PAY",
                        onClick = onSignInClick,
                        testTag = "welcome_sign_in_button"
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onSignUpClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("welcome_sign_up_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.6f)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = extra.cyanGlow)
                    ) {
                        Text(
                            text = "Create New Account",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WelcomeFeatureRow(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    val extra = LocalHKPayColors.current
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(extra.electricBlue.copy(alpha = 0.16f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = extra.cyanGlow,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SignInScreen(
    uiState: AuthUiState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignInWithPin: (String, String) -> Unit,
    onForgotPassword: () -> Unit,
    onNavigateSignUp: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var usePinMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.pinVerifiedNeedsPassword) {
        if (uiState.pinVerifiedNeedsPassword) {
            usePinMode = false
        }
    }

    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("signin_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    ThemeCycleIconButton(themeMode = themeMode, onToggleTheme = onToggleTheme)
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.hk_pay_logo),
                        contentDescription = "HK PAY Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Welcome Back",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Sign in to manage your HK PAY cards",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))
                AuthFeedbackBanner(uiState.errorMessage, uiState.infoMessage)

                GlassCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = !usePinMode,
                            onClick = { usePinMode = false },
                            label = { Text("Email & Password") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f).testTag("signin_mode_password")
                        )
                        FilterChip(
                            selected = usePinMode,
                            onClick = { usePinMode = true },
                            label = { Text("Email & PIN") },
                            leadingIcon = { Icon(HKPayIcons.PinLock, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            modifier = Modifier.weight(1f).testTag("signin_mode_pin")
                        )
                    }
                    Spacer(Modifier.height(14.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("signin_email_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(14.dp))

                    if (usePinMode) {
                        OutlinedTextField(
                            value = pinCode,
                            onValueChange = { pinCode = it.filter { c -> c.isDigit() }.take(4) },
                            label = { Text("4-Digit Security PIN") },
                            leadingIcon = { Icon(HKPayIcons.PinLock, contentDescription = null) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) HKPayIcons.VisibilityOff else HKPayIcons.Visibility,
                                        contentDescription = if (passwordVisible) "Hide PIN" else "Show PIN"
                                    )
                                }
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.NumberPassword,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_pin_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    } else {
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                            trailingIcon = {
                                IconButton(
                                    onClick = { passwordVisible = !passwordVisible },
                                    modifier = Modifier.testTag("signin_toggle_password")
                                ) {
                                    Icon(
                                        imageVector = if (passwordVisible) HKPayIcons.VisibilityOff else HKPayIcons.Visibility,
                                        contentDescription = if (passwordVisible) "Hide password" else "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("signin_password_input"),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onForgotPassword,
                            modifier = Modifier.testTag("signin_forgot_password_button")
                        ) {
                            Text("Forgot Password?")
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    HKPayPrimaryButton(
                        text = "Sign In",
                        onClick = {
                            if (usePinMode) onSignInWithPin(email, pinCode)
                            else onSignIn(email, password)
                        },
                        isLoading = uiState.isLoading,
                        testTag = "signin_submit_button"
                    )
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "New to HK PAY?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onNavigateSignUp,
                        modifier = Modifier.testTag("signin_goto_signup_button")
                    ) {
                        Text("Create Account", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    uiState: AuthUiState,
    publicConfig: AppSettings,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSignUp: (
        fullName: String,
        email: String,
        phone: String,
        password: String,
        confirmPassword: String,
        nidNumber: String,
        nidFrontUri: Uri?,
        nidBackUri: Uri?,
        livenessUri: Uri?,
        livenessChallenge: String,
        referralCode: String,
        country: String,
        district: String,
        cityUpazila: String,
        appPin: String,
        confirmAppPin: String,
        enableBiometric: Boolean
    ) -> Unit,
    onNavigateSignIn: () -> Unit,
    onBack: () -> Unit
) {
    val extra = LocalHKPayColors.current

    var currentStep by remember { mutableIntStateOf(1) }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var referralCode by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Step 2: Country / Zila / Upazila + App Lock 4-Digit PIN & Biometric preference
    var country by remember { mutableStateOf("Bangladesh") }
    var district by remember { mutableStateOf("Dhaka") }
    var cityUpazila by remember { mutableStateOf("Dhanmondi") }
    var appPin by remember { mutableStateOf("") }
    var confirmAppPin by remember { mutableStateOf("") }
    var enableBiometric by remember { mutableStateOf(true) }

    // Step 3: Live NID Scan & Face Liveness
    var nidNumber by remember { mutableStateOf("") }
    var nidFrontUri by remember { mutableStateOf<Uri?>(null) }
    var nidBackUri by remember { mutableStateOf<Uri?>(null) }
    var livenessUri by remember { mutableStateOf<Uri?>(null) }
    var challengeIdx by remember { mutableIntStateOf(0) }
    var livenessCompletedPrompt by remember { mutableStateOf("") }
    var stepError by remember { mutableStateOf<String?>(null) }

    var showDocFrontScanner by remember { mutableStateOf(false) }
    var showDocBackScanner by remember { mutableStateOf(false) }
    var showFaceLivenessScanner by remember { mutableStateOf(false) }

    val activeChallenge = LIVENESS_CHALLENGES[challengeIdx % LIVENESS_CHALLENGES.size]

    if (showDocFrontScanner) {
        LiveDocumentScanDialog(
            title = "Scan Document Front Side",
            subtitle = "Hold your NID or Passport front inside the glowing frame.",
            onCaptured = { uri ->
                nidFrontUri = uri
                showDocFrontScanner = false
                stepError = null
            },
            onDismiss = { showDocFrontScanner = false }
        )
    }

    if (showDocBackScanner) {
        LiveDocumentScanDialog(
            title = "Scan Document Back Side",
            subtitle = "Capture the back barcode & address side clearly.",
            onCaptured = { uri ->
                nidBackUri = uri
                showDocBackScanner = false
                stepError = null
            },
            onDismiss = { showDocBackScanner = false }
        )
    }

    if (showFaceLivenessScanner) {
        LiveFaceLivenessDialog(
            livenessChallenge = activeChallenge,
            onCompleted = { uri, challengeSummary ->
                livenessUri = uri
                livenessCompletedPrompt = challengeSummary
                showFaceLivenessScanner = false
                stepError = null
            },
            onDismiss = { showFaceLivenessScanner = false }
        )
    }

    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 540.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            if (currentStep > 1) currentStep -= 1 else onBack()
                        },
                        modifier = Modifier.testTag("signup_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    ThemeCycleIconButton(themeMode = themeMode, onToggleTheme = onToggleTheme)
                }
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.hk_pay_logo),
                        contentDescription = "HK PAY Logo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Create HK PAY Account",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = when (currentStep) {
                                1 -> "Step 1 of 3 • Account, Email & Phone (No OTP)"
                                2 -> "Step 2 of 3 • Region & 4-Digit App Lock PIN"
                                else -> "Step 3 of 3 • Live NID Scan & Face Liveness"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = extra.cyanGlow
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { currentStep / 3f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = extra.cyanGlow,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )

                Spacer(Modifier.height(16.dp))

                if (!publicConfig.registrationEnabled) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = extra.warning.copy(alpha = 0.14f),
                        border = BorderStroke(1.dp, extra.warning.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                    ) {
                        Text(
                            text = "Registrations are temporarily paused by administrator. Please check back later.",
                            style = MaterialTheme.typography.bodySmall,
                            color = extra.warning,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                AuthFeedbackBanner(stepError ?: uiState.errorMessage, uiState.infoMessage)

                when (currentStep) {
                    1 -> {
                        GlassCard {
                            OutlinedTextField(
                                value = fullName,
                                onValueChange = { fullName = it },
                                label = { Text("Full Legal Name (as on NID)") },
                                leadingIcon = { Icon(Icons.Rounded.Person, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_fullname_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = email,
                                onValueChange = { email = it },
                                label = { Text("Email Address (Verification Link)") },
                                leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_email_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { phone = it },
                                label = { Text("Phone Number (No SMS OTP Needed)") },
                                placeholder = { Text("+880 1XXX-XXXXXX") },
                                leadingIcon = { Icon(HKPayIcons.Phone, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_phone_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                                trailingIcon = {
                                    IconButton(
                                        onClick = { showPassword = !showPassword },
                                        modifier = Modifier.testTag("signup_toggle_password")
                                    ) {
                                        Icon(
                                            imageVector = if (showPassword) HKPayIcons.VisibilityOff else HKPayIcons.Visibility,
                                            contentDescription = if (showPassword) "Hide password" else "Show password"
                                        )
                                    }
                                },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_password_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = confirmPassword,
                                onValueChange = { confirmPassword = it },
                                label = { Text("Confirm Password") },
                                leadingIcon = { Icon(Icons.Rounded.Lock, contentDescription = null) },
                                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_confirm_password_input"),
                                shape = RoundedCornerShape(14.dp)
                            )

                            if (publicConfig.referralProgramEnabled) {
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = referralCode,
                                    onValueChange = { referralCode = it.uppercase() },
                                    label = { Text("Referral Code (Optional)") },
                                    placeholder = {
                                        Text(
                                            "e.g. HKR123456 — ${FormatUtils.formatMoney(publicConfig.referralBonusAmount, "USD")} bonus"
                                        )
                                    },
                                    leadingIcon = { Icon(HKPayIcons.CardGiftcard, contentDescription = null) },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("signup_referral_input"),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }

                            Spacer(Modifier.height(18.dp))
                            HKPayPrimaryButton(
                                text = "Next: Region & 4-Digit PIN",
                                enabled = publicConfig.registrationEnabled,
                                onClick = {
                                    val digits = phone.filter { it.isDigit() }
                                    when {
                                        fullName.trim().length < 2 -> stepError = "Enter your full legal name."
                                        !FormatUtils.isValidEmail(email) -> stepError = "Enter a valid email address."
                                        digits.length < 8 -> stepError = "Enter a valid phone number."
                                        password.length < 6 -> stepError = "Password must be at least 6 characters."
                                        password != confirmPassword -> stepError = "Passwords do not match."
                                        else -> {
                                            stepError = null
                                            currentStep = 2
                                        }
                                    }
                                },
                                testTag = "signup_next_step_button"
                            )
                        }
                    }

                    2 -> {
                        GlassCard {
                            WorldwideLocationSelector(
                                country = country,
                                district = district,
                                cityUpazila = cityUpazila,
                                onCountrySelected = { country = it },
                                onDistrictChanged = { district = it },
                                onCityUpazilaChanged = { cityUpazila = it }
                            )

                            Spacer(Modifier.height(18.dp))
                            Text(
                                text = "Set Up 4-Digit App Lock PIN",
                                style = MaterialTheme.typography.titleMedium,
                                color = extra.cyanGlow,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Your account stays signed in even after closing the app or restarting your phone — unlock anytime with your 4-digit PIN or Biometric.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            OutlinedTextField(
                                value = appPin,
                                onValueChange = { appPin = it.filter { c -> c.isDigit() }.take(4) },
                                label = { Text("4-Digit App Lock PIN") },
                                leadingIcon = { Icon(HKPayIcons.PinLock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Next
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_app_pin_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(10.dp))
                            OutlinedTextField(
                                value = confirmAppPin,
                                onValueChange = { confirmAppPin = it.filter { c -> c.isDigit() }.take(4) },
                                label = { Text("Confirm 4-Digit PIN") },
                                leadingIcon = { Icon(HKPayIcons.PinLock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.NumberPassword,
                                    imeAction = ImeAction.Done
                                ),
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_confirm_app_pin_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                                    Text(
                                        text = "Also Enable Biometric Unlock",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Text(
                                        text = "Choose Fingerprint / Face ID or 4-digit PIN when opening HK PAY",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Switch(
                                    checked = enableBiometric,
                                    onCheckedChange = { enableBiometric = it },
                                    modifier = Modifier.testTag("signup_biometric_switch")
                                )
                            }

                            Spacer(Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        stepError = null
                                        currentStep = 1
                                    },
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .height(52.dp),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Back")
                                }
                                Box(modifier = Modifier.weight(1.4f)) {
                                    HKPayPrimaryButton(
                                        text = "Next: Live NID & Face",
                                        onClick = {
                                            when {
                                                country.isBlank() -> stepError = "Select your country."
                                                !SecurityUtils.isValidPin(appPin) -> stepError = "Enter a 4-digit App Lock PIN."
                                                appPin != confirmAppPin -> stepError = "4-digit PINs do not match."
                                                else -> {
                                                    stepError = null
                                                    currentStep = 3
                                                }
                                            }
                                        },
                                        testTag = "signup_step2_next_button"
                                    )
                                }
                            }
                        }
                    }

                    else -> {
                        GlassCard {
                            Text(
                                text = "Direct NID Scan & Face Liveness",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Scan your NID/Passport directly with the live camera frame and complete the directional face liveness check.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))

                            OutlinedTextField(
                                value = nidNumber,
                                onValueChange = { nidNumber = it },
                                label = { Text("NID / Document Number") },
                                leadingIcon = { Icon(HKPayIcons.Badge, contentDescription = null) },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_nid_number_input"),
                                shape = RoundedCornerShape(14.dp)
                            )
                            Spacer(Modifier.height(12.dp))

                            SignUpMediaTile(
                                label = "Document Front Scan",
                                selectedUri = nidFrontUri,
                                icon = HKPayIcons.CameraAlt,
                                buttonLabel = if (nidFrontUri != null) "Front Scanned (Rescan / Change)" else "Live Scan NID Front",
                                onClick = { showDocFrontScanner = true },
                                testTag = "signup_nid_front_button"
                            )
                            Spacer(Modifier.height(10.dp))
                            SignUpMediaTile(
                                label = "Document Back Scan",
                                selectedUri = nidBackUri,
                                icon = HKPayIcons.CameraAlt,
                                buttonLabel = if (nidBackUri != null) "Back Scanned (Rescan / Change)" else "Live Scan NID Back",
                                onClick = { showDocBackScanner = true },
                                testTag = "signup_nid_back_button"
                            )

                            Spacer(Modifier.height(14.dp))
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = extra.electricBlue.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, extra.cyanGlow.copy(alpha = 0.45f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = HKPayIcons.FaceCheck,
                                                contentDescription = "Liveness prompt",
                                                tint = extra.cyanGlow,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "Directional Face Liveness Guide",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = extra.cyanGlow
                                            )
                                        }
                                        IconButton(
                                            onClick = { challengeIdx++ },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(
                                                imageVector = HKPayIcons.Refresh,
                                                contentDescription = "New liveness challenge",
                                                tint = extra.cyanGlow,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = livenessCompletedPrompt.ifBlank { activeChallenge },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                            Spacer(Modifier.height(10.dp))
                            SignUpMediaTile(
                                label = "Live Face Liveness",
                                selectedUri = livenessUri,
                                icon = HKPayIcons.FaceCheck,
                                buttonLabel = if (livenessUri != null) "Face Liveness Verified (Retake)" else "Start Live Face Liveness Scan",
                                onClick = { showFaceLivenessScanner = true },
                                testTag = "signup_liveness_capture_button"
                            )

                            Spacer(Modifier.height(18.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        stepError = null
                                        currentStep = 2
                                    },
                                    modifier = Modifier
                                        .weight(0.8f)
                                        .height(52.dp)
                                        .testTag("signup_step_back_button"),
                                    shape = RoundedCornerShape(14.dp)
                                ) {
                                    Text("Back")
                                }
                                Box(modifier = Modifier.weight(1.4f)) {
                                    HKPayPrimaryButton(
                                        text = "Create Account",
                                        enabled = publicConfig.registrationEnabled,
                                        isLoading = uiState.isLoading,
                                        onClick = {
                                            stepError = null
                                            onSignUp(
                                                fullName,
                                                email,
                                                phone,
                                                password,
                                                confirmPassword,
                                                nidNumber,
                                                nidFrontUri,
                                                nidBackUri,
                                                livenessUri,
                                                livenessCompletedPrompt.ifBlank { activeChallenge },
                                                referralCode,
                                                country,
                                                district,
                                                cityUpazila,
                                                appPin,
                                                confirmAppPin,
                                                enableBiometric
                                            )
                                        },
                                        testTag = "signup_submit_button"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already registered?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onNavigateSignIn,
                        modifier = Modifier.testTag("signup_goto_signin_button")
                    ) {
                        Text("Sign In", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SignUpMediaTile(
    label: String,
    selectedUri: Uri?,
    icon: ImageVector,
    buttonLabel: String,
    onClick: () -> Unit,
    testTag: String
) {
    Column {
        OutlinedButton(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(testTag),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(
                imageVector = if (selectedUri != null) Icons.Rounded.CheckCircle else icon,
                contentDescription = label
            )
            Spacer(Modifier.width(8.dp))
            Text(buttonLabel)
        }
        if (selectedUri != null) {
            Spacer(Modifier.height(8.dp))
            AsyncImage(
                model = selectedUri,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(118.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    uiState: AuthUiState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSendReset: (String) -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }

    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.widthIn(max = 520.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("forgot_back_button")
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    ThemeCycleIconButton(themeMode = themeMode, onToggleTheme = onToggleTheme)
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Reset Password",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Enter your registered email address and Firebase will send a secure password reset link.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(20.dp))
                AuthFeedbackBanner(uiState.errorMessage, uiState.infoMessage)

                GlassCard {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Registered Email") },
                        leadingIcon = { Icon(Icons.Rounded.Email, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("forgot_email_input"),
                        shape = RoundedCornerShape(14.dp)
                    )
                    Spacer(Modifier.height(18.dp))
                    HKPayPrimaryButton(
                        text = "Send Reset Link",
                        onClick = { onSendReset(email) },
                        isLoading = uiState.isLoading,
                        testTag = "forgot_submit_button"
                    )
                }
            }
        }
    }
}

@Composable
fun EmailVerificationScreen(
    email: String,
    uiState: AuthUiState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onResend: () -> Unit,
    onCheckVerified: () -> Unit,
    onSignOut: () -> Unit
) {
    val extra = LocalHKPayColors.current
    val context = LocalContext.current

    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 500.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ThemeCycleIconButton(themeMode = themeMode, onToggleTheme = onToggleTheme)
            }
            Spacer(Modifier.height(24.dp))
            Column(
                modifier = Modifier.widthIn(max = 500.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(CircleShape)
                        .background(extra.electricBlue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HKPayIcons.MarkEmailRead,
                        contentDescription = "Verify email",
                        tint = extra.cyanGlow,
                        modifier = Modifier.size(42.dp)
                    )
                }
                Spacer(Modifier.height(20.dp))
                Text(
                    text = "Verify Your Email",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "We sent a secure verification link to:\n$email",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(20.dp))
                AuthFeedbackBanner(uiState.errorMessage, uiState.infoMessage)

                GlassCard {
                    HKPayPrimaryButton(
                        text = "I've Verified — Continue",
                        onClick = onCheckVerified,
                        isLoading = uiState.isLoading,
                        testTag = "verify_check_button"
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                val intent = Intent(Intent.ACTION_MAIN).apply {
                                    addCategory(Intent.CATEGORY_APP_EMAIL)
                                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                }
                                context.startActivity(Intent.createChooser(intent, "Open Email App"))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_open_email_app_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(HKPayIcons.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Open Email Inbox")
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onResend,
                        enabled = !uiState.isLoading && uiState.resendCooldownSeconds == 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("verify_resend_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        val label = if (uiState.resendCooldownSeconds > 0) {
                            "Resend Link (${uiState.resendCooldownSeconds}s)"
                        } else {
                            "Resend Verification Link"
                        }
                        Text(label)
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(
                        onClick = onSignOut,
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .testTag("verify_signout_button")
                    ) {
                        Text("Use Different Account", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
private fun AuthFeedbackBanner(
    errorMessage: String?,
    infoMessage: String?
) {
    val extra = LocalHKPayColors.current
    AnimatedVisibility(visible = !errorMessage.isNullOrBlank()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(12.dp),
            color = extra.danger.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, extra.danger.copy(alpha = 0.45f))
        ) {
            Text(
                text = errorMessage.orEmpty(),
                color = extra.danger,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
    AnimatedVisibility(visible = !infoMessage.isNullOrBlank()) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            shape = RoundedCornerShape(12.dp),
            color = extra.success.copy(alpha = 0.14f),
            border = BorderStroke(1.dp, extra.success.copy(alpha = 0.45f))
        ) {
            Text(
                text = infoMessage.orEmpty(),
                color = extra.success,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
fun BiometricLockScreen(
    userEmail: String,
    errorMessage: String?,
    onPromptBiometric: () -> Unit,
    onUsePasswordInstead: () -> Unit
) {
    val extra = LocalHKPayColors.current

    LaunchedEffect(Unit) {
        onPromptBiometric()
    }

    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Column(
                modifier = Modifier.widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.hk_pay_logo),
                    contentDescription = "HK PAY Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(22.dp))
                )
                Spacer(Modifier.height(20.dp))
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(extra.electricBlue.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HKPayIcons.Fingerprint,
                        contentDescription = "Biometric unlock",
                        tint = extra.cyanGlow,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Unlock HK PAY",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Confirm your fingerprint or face ID to access $userEmail",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(18.dp))
                AuthFeedbackBanner(errorMessage = errorMessage, infoMessage = null)

                GlassCard {
                    HKPayPrimaryButton(
                        text = "Unlock with Biometrics",
                        onClick = onPromptBiometric,
                        icon = HKPayIcons.Fingerprint,
                        testTag = "biometric_unlock_button"
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onUsePasswordInstead,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("biometric_use_password_button"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Sign In with Email & Password")
                    }
                }
            }
        }
    }
}
