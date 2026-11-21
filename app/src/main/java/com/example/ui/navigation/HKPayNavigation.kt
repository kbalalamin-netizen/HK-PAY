package com.example.ui.navigation

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.model.AppLanguage
import com.example.data.model.AppSettings
import com.example.data.model.ThemeMode
import com.example.ui.components.AppLockScreen
import com.example.ui.components.GlassCard
import com.example.ui.components.HKPayBackdrop
import com.example.ui.components.LanguageSelectorDialog
import com.example.ui.components.PermissionStepDialog
import com.example.ui.components.ThemeCycleIconButton
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.BuyCardScreen
import com.example.ui.screens.CardSelectionScreen
import com.example.ui.screens.CardStoreScreen
import com.example.ui.screens.CurrencyRatesScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EditProfileScreen
import com.example.ui.screens.EmailVerificationScreen
import com.example.ui.screens.ForgotPasswordScreen
import com.example.ui.screens.KycScreen
import com.example.ui.screens.MyCardsScreen
import com.example.ui.screens.NotificationSettingsScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrderDetailScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.PinLockSettingsScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SupportDetailScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TermsScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.TutorialsScreen
import com.example.ui.screens.TwoFactorSetupScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.HKPayIcons
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.AuthSessionState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiEvent
import com.example.util.BiometricHelper
import com.example.util.HKPayStrings
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

// Auth Routes
@Serializable object WelcomeRoute
@Serializable object SignInRoute
@Serializable object SignUpRoute
@Serializable object ForgotPasswordRoute

// Primary Tab Routes
@Serializable object HomeRoute
@Serializable object CardsRoute
@Serializable object BuyStoreRoute
@Serializable object OrdersRoute
@Serializable object ProfileRoute

// Secondary Flow Routes
@Serializable data class CardSelectionRoute(val initialPackageId: String = "")
@Serializable data class BuyCardRoute(val packageId: String, val selectedDesign: String = "")
@Serializable data class PaymentRoute(val orderId: String)
@Serializable data class OrderDetailRoute(val orderId: String)
@Serializable object KycRoute
@Serializable object CurrencyRatesRoute
@Serializable object TransactionsRoute
@Serializable object NotificationsRoute
@Serializable object SupportRoute
@Serializable data class SupportDetailRoute(val ticketId: String)
@Serializable object EditProfileRoute
@Serializable object NotificationSettingsRoute
@Serializable object TutorialsRoute
@Serializable object ReferralRoute
@Serializable object PinLockSettingsRoute
@Serializable object TwoFactorRoute
@Serializable object PrivacyPolicyRoute
@Serializable object TermsRoute
@Serializable object AboutRoute

private data class TopLevelDestination(
    val route: Any,
    val labelProvider: (AppLanguage) -> String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TopLevelDestinations = listOf(
    TopLevelDestination(HomeRoute, HKPayStrings::navHome, Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    TopLevelDestination(CardsRoute, HKPayStrings::navCards, HKPayIcons.CreditCard, HKPayIcons.CreditCard, "nav_cards"),
    TopLevelDestination(BuyStoreRoute, HKPayStrings::navBuy, HKPayIcons.AddCard, HKPayIcons.AddCard, "nav_buy"),
    TopLevelDestination(OrdersRoute, HKPayStrings::navOrders, HKPayIcons.LocalShipping, HKPayIcons.LocalShipping, "nav_orders"),
    TopLevelDestination(ProfileRoute, HKPayStrings::navProfile, Icons.Rounded.Person, Icons.Outlined.Person, "nav_profile")
)

@Composable
fun HKPayAppRoot(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    themeMode: ThemeMode,
    pendingOrderId: String? = null,
    onConsumePendingOrderId: () -> Unit = {}
) {
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val publicConfig by authViewModel.publicConfig.collectAsStateWithLifecycle()
    val appLanguage by mainViewModel.appLanguage.collectAsStateWithLifecycle()
    val biometricEnabled by mainViewModel.biometricEnabled.collectAsStateWithLifecycle()
    val pinLockEnabled by mainViewModel.pinLockEnabled.collectAsStateWithLifecycle()
    val isUnlockedThisSession by mainViewModel.isBiometricUnlockedThisSession.collectAsStateWithLifecycle()
    val pinUnlockError by mainViewModel.pinUnlockError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var biometricError by remember { mutableStateOf<String?>(null) }

    when (val session = sessionState) {
        is AuthSessionState.Splash -> {
            SplashScreen()
        }

        is AuthSessionState.Unauthenticated -> {
            LaunchedEffect(Unit) { mainViewModel.clearSession() }
            if (publicConfig.maintenanceMode) {
                MaintenanceShieldScreen(
                    settings = publicConfig,
                    themeMode = themeMode,
                    onToggleTheme = mainViewModel::cycleThemeMode,
                    onSignOut = null
                )
            } else {
                AuthFlowHost(
                    authUiState = authUiState,
                    publicConfig = publicConfig,
                    themeMode = themeMode,
                    onToggleTheme = mainViewModel::cycleThemeMode,
                    onSignIn = authViewModel::signIn,
                    onSignInWithPin = authViewModel::verifyEmailPinForSignIn,
                    onSignUp = authViewModel::signUp,
                    onForgotPassword = authViewModel::sendPasswordReset,
                    onClearMessages = authViewModel::clearMessages
                )
            }
        }

        is AuthSessionState.NeedsVerification -> {
            EmailVerificationScreen(
                email = session.email,
                uiState = authUiState,
                themeMode = themeMode,
                onToggleTheme = mainViewModel::cycleThemeMode,
                onResend = authViewModel::resendVerificationEmail,
                onCheckVerified = authViewModel::checkEmailVerification,
                onSignOut = authViewModel::signOut
            )
        }

        is AuthSessionState.Authenticated -> {
            LaunchedEffect(session.user.uid) {
                mainViewModel.bindUser(session.user.uid)
            }

            val isLockRequired = (pinLockEnabled || biometricEnabled) && !isUnlockedThisSession
            if (isLockRequired) {
                val promptBiometricAction = {
                    val activity = context as? FragmentActivity
                    if (activity != null) {
                        biometricError = null
                        mainViewModel.clearPinUnlockError()
                        BiometricHelper.authenticate(
                            activity = activity,
                            title = "HK PAY Security Unlock",
                            subtitle = "Verify your fingerprint or face ID to unlock HK PAY",
                            negativeButtonText = if (pinLockEnabled) "Use 4-Digit PIN" else "Cancel",
                            onSuccess = {
                                biometricError = null
                                mainViewModel.unlockBiometrically()
                            },
                            onNegativeAction = { /* stay on PIN keypad */ },
                            onError = { msg -> biometricError = msg }
                        )
                    } else {
                        biometricError = "Biometric prompt is unavailable in this view."
                    }
                }

                AppLockScreen(
                    userEmail = session.user.email.orEmpty(),
                    pinEnabled = pinLockEnabled,
                    biometricEnabled = biometricEnabled,
                    errorMessage = pinUnlockError ?: biometricError,
                    onUnlockWithPin = { pin ->
                        biometricError = null
                        mainViewModel.unlockWithPin(pin)
                    },
                    onPromptBiometric = promptBiometricAction,
                    onSignOut = {
                        mainViewModel.clearSession()
                        authViewModel.signOut()
                    }
                )
            } else {
                MainAuthenticatedHost(
                    mainViewModel = mainViewModel,
                    themeMode = themeMode,
                    appLanguage = appLanguage,
                    biometricEnabled = biometricEnabled,
                    pendingOrderId = pendingOrderId,
                    onConsumePendingOrderId = onConsumePendingOrderId,
                    onLogout = {
                        mainViewModel.clearSession()
                        authViewModel.signOut()
                    }
                )
            }
        }
    }
}

@Composable
private fun MaintenanceShieldScreen(
    settings: AppSettings,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSignOut: (() -> Unit)?
) {
    val extra = LocalHKPayColors.current
    HKPayBackdrop {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .widthIn(max = 520.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ThemeCycleIconButton(themeMode = themeMode, onToggleTheme = onToggleTheme)
            }
            Spacer(Modifier.weight(1f))
            Column(
                modifier = Modifier.widthIn(max = 520.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(CircleShape)
                        .background(extra.warning.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = HKPayIcons.Shield,
                        contentDescription = "Maintenance mode",
                        tint = extra.warning,
                        modifier = Modifier.size(38.dp)
                    )
                }
                Spacer(Modifier.height(18.dp))
                Text(
                    text = "Scheduled Maintenance",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                GlassCard {
                    Text(
                        text = settings.maintenanceMessage.ifBlank {
                            "HK PAY is currently undergoing scheduled administrator maintenance. Your cards and funds remain safe. Please check back shortly."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center
                    )
                    if (settings.supportEmail.isNotBlank() || settings.supportPhone.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = listOf(settings.supportEmail, settings.supportPhone)
                                .filter { it.isNotBlank() }
                                .joinToString(" • "),
                            style = MaterialTheme.typography.labelMedium,
                            color = extra.cyanGlow,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (onSignOut != null) {
                        Spacer(Modifier.height(14.dp))
                        OutlinedButton(
                            onClick = onSignOut,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Session Logout")
                        }
                    }
                }
            }
            Spacer(Modifier.weight(1f))
        }
    }
}

@Composable
private fun AuthFlowHost(
    authUiState: com.example.ui.viewmodel.AuthUiState,
    publicConfig: AppSettings,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onSignIn: (String, String) -> Unit,
    onSignInWithPin: (String, String) -> Unit,
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
    onForgotPassword: (String) -> Unit,
    onClearMessages: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = WelcomeRoute
    ) {
        composable<WelcomeRoute> {
            WelcomeScreen(
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onSignInClick = {
                    onClearMessages()
                    navController.navigate(SignInRoute)
                },
                onSignUpClick = {
                    onClearMessages()
                    navController.navigate(SignUpRoute)
                }
            )
        }
        composable<SignInRoute> {
            SignInScreen(
                uiState = authUiState,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onSignIn = onSignIn,
                onSignInWithPin = onSignInWithPin,
                onForgotPassword = {
                    onClearMessages()
                    navController.navigate(ForgotPasswordRoute)
                },
                onNavigateSignUp = {
                    onClearMessages()
                    navController.navigate(SignUpRoute)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable<SignUpRoute> {
            SignUpScreen(
                uiState = authUiState,
                publicConfig = publicConfig,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onSignUp = onSignUp,
                onNavigateSignIn = {
                    onClearMessages()
                    navController.navigate(SignInRoute)
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable<ForgotPasswordRoute> {
            ForgotPasswordScreen(
                uiState = authUiState,
                themeMode = themeMode,
                onToggleTheme = onToggleTheme,
                onSendReset = onForgotPassword,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainAuthenticatedHost(
    mainViewModel: MainViewModel,
    themeMode: ThemeMode,
    appLanguage: AppLanguage,
    biometricEnabled: Boolean,
    pendingOrderId: String?,
    onConsumePendingOrderId: () -> Unit,
    onLogout: () -> Unit
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val balanceVisible by mainViewModel.balanceVisible.collectAsStateWithLifecycle()
    val tutorialCompleted by mainViewModel.tutorialCompleted.collectAsStateWithLifecycle()
    val pinLockEnabled by mainViewModel.pinLockEnabled.collectAsStateWithLifecycle()
    val twoFactorEnabled by mainViewModel.twoFactorEnabled.collectAsStateWithLifecycle()
    val twoFactorSecret by mainViewModel.twoFactorSecret.collectAsStateWithLifecycle()
    val permissionsWizardDone by mainViewModel.permissionsWizardDone.collectAsStateWithLifecycle()
    val isCardManagementVerified by mainViewModel.isCardManagementVerified.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val extra = LocalHKPayColors.current

    var showLanguagePicker by remember { mutableStateOf(false) }
    var manualPermissionsWizardOpen by remember { mutableStateOf(false) }
    var dismissedAutoPermissionsWizard by remember { mutableStateOf(false) }

    if (showLanguagePicker) {
        LanguageSelectorDialog(
            currentLanguage = appLanguage,
            onSelectLanguage = { lang -> mainViewModel.setAppLanguage(lang) },
            onDismiss = { showLanguagePicker = false }
        )
    }

    val shouldShowPermissionsWizard = manualPermissionsWizardOpen ||
        (!permissionsWizardDone && !dismissedAutoPermissionsWizard)

    if (shouldShowPermissionsWizard) {
        PermissionStepDialog(
            onBeforeSystemPrompt = mainViewModel::suppressNextBackgroundLock,
            onCompleted = {
                mainViewModel.setPermissionsWizardDone(true)
                manualPermissionsWizardOpen = false
                dismissedAutoPermissionsWizard = true
                scope.launch { snackbarHostState.showSnackbar("Permissions configured.") }
            },
            onDismiss = {
                mainViewModel.setPermissionsWizardDone(true)
                manualPermissionsWizardOpen = false
                dismissedAutoPermissionsWizard = true
            }
        )
    }

    LaunchedEffect(pendingOrderId) {
        if (!pendingOrderId.isNullOrBlank()) {
            navController.navigate(OrderDetailRoute(pendingOrderId)) {
                launchSingleTop = true
            }
            onConsumePendingOrderId()
        }
    }

    LaunchedEffect(mainViewModel) {
        mainViewModel.events.collect { event ->
            when (event) {
                is UiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.OrderCreated -> {
                    snackbarHostState.showSnackbar("Order ${event.orderId} created")
                    navController.navigate(PaymentRoute(event.orderId)) {
                        popUpTo(BuyStoreRoute)
                    }
                }
                is UiEvent.PaymentSubmitted -> {
                    snackbarHostState.showSnackbar("Payment submitted for backend verification")
                    navController.navigate(OrdersRoute) {
                        popUpTo(HomeRoute)
                    }
                }
                is UiEvent.KycSubmitted -> {
                    snackbarHostState.showSnackbar("KYC & Liveness verification submitted")
                    navController.popBackStack()
                }
                is UiEvent.TicketCreated -> {
                    snackbarHostState.showSnackbar("Ticket ${event.ticketId} created")
                    navController.navigate(SupportDetailRoute(event.ticketId))
                }
                is UiEvent.ProfileUpdated -> {
                    snackbarHostState.showSnackbar("Profile updated")
                    navController.popBackStack()
                }
                is UiEvent.PinUpdated -> Unit
            }
        }
    }

    if (state.appSettings.maintenanceMode) {
        MaintenanceShieldScreen(
            settings = state.appSettings,
            themeMode = themeMode,
            onToggleTheme = mainViewModel::cycleThemeMode,
            onSignOut = onLogout
        )
        return
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val isTopLevelRoute = TopLevelDestinations.any { dest ->
        currentDestination?.hasRoute(dest.route::class) == true
    }

    fun navigateTopLevel(route: Any) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun navigateSliderAction(actionRoute: String) {
        when (actionRoute.lowercase()) {
            "cards" -> navigateTopLevel(CardsRoute)
            "orders" -> navigateTopLevel(OrdersRoute)
            "profile" -> navigateTopLevel(ProfileRoute)
            "kyc" -> navController.navigate(KycRoute)
            "referrals" -> navController.navigate(ReferralRoute)
            "rates" -> navController.navigate(CurrencyRatesRoute)
            "support" -> navController.navigate(SupportRoute)
            "transactions" -> navController.navigate(TransactionsRoute)
            "tutorials" -> navController.navigate(TutorialsRoute)
            "designs" -> navController.navigate(CardSelectionRoute())
            else -> navigateTopLevel(BuyStoreRoute)
        }
    }

    fun showMessage(message: String) {
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    HKPayBackdrop {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = Color.Transparent,
                contentWindowInsets = WindowInsets.safeDrawing,
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                bottomBar = {
                    if (isTopLevelRoute && !useRail) {
                        NavigationBar(
                            containerColor = extra.glassSurfaceElevated,
                            tonalElevation = 4.dp,
                            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                        ) {
                            TopLevelDestinations.forEach { dest ->
                                val selected = currentDestination?.hasRoute(dest.route::class) == true
                                val labelText = dest.labelProvider(appLanguage)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = labelText
                                        )
                                    },
                                    label = { Text(labelText, style = MaterialTheme.typography.labelMedium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = extra.cyanGlow,
                                        selectedTextColor = extra.cyanGlow,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = extra.electricBlue.copy(alpha = 0.22f)
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isTopLevelRoute && useRail) {
                        NavigationRail(
                            containerColor = extra.glassSurfaceElevated,
                            modifier = Modifier.fillMaxHeight()
                        ) {
                            TopLevelDestinations.forEach { dest ->
                                val selected = currentDestination?.hasRoute(dest.route::class) == true
                                val labelText = dest.labelProvider(appLanguage)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = labelText
                                        )
                                    },
                                    label = { Text(labelText) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = extra.cyanGlow,
                                        selectedTextColor = extra.cyanGlow,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = extra.electricBlue.copy(alpha = 0.22f)
                                    ),
                                    modifier = Modifier.testTag(dest.testTag)
                                )
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxSize()) {
                        NavHost(
                            navController = navController,
                            startDestination = HomeRoute
                        ) {
                            composable<HomeRoute> {
                                DashboardScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    appLanguage = appLanguage,
                                    balanceVisible = balanceVisible,
                                    tutorialCompleted = tutorialCompleted,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onOpenLanguagePicker = { showLanguagePicker = true },
                                    onToggleBalanceVisible = mainViewModel::toggleBalanceVisibility,
                                    onToggleCardFreeze = mainViewModel::toggleCardFreeze,
                                    onNavigateRoute = ::navigateSliderAction,
                                    onNavigateBuyCard = { navigateTopLevel(BuyStoreRoute) },
                                    onNavigateCardSelection = { navController.navigate(CardSelectionRoute()) },
                                    onNavigateMyCards = { navigateTopLevel(CardsRoute) },
                                    onNavigateOrders = { navigateTopLevel(OrdersRoute) },
                                    onNavigateOrderDetail = { orderId -> navController.navigate(OrderDetailRoute(orderId)) },
                                    onNavigateTransactions = { navController.navigate(TransactionsRoute) },
                                    onNavigateProfile = { navigateTopLevel(ProfileRoute) },
                                    onNavigateSupport = { navController.navigate(SupportRoute) },
                                    onNavigateNotifications = { navController.navigate(NotificationsRoute) },
                                    onNavigateCurrencyRates = { navController.navigate(CurrencyRatesRoute) },
                                    onNavigateKyc = { navController.navigate(KycRoute) },
                                    onNavigateTutorials = { navController.navigate(TutorialsRoute) },
                                    onNavigateReferrals = { navController.navigate(ReferralRoute) },
                                    onCopiedMessage = ::showMessage
                                )
                            }

                            composable<CardsRoute> {
                                MyCardsScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    balanceVisible = balanceVisible,
                                    biometricEnabled = biometricEnabled,
                                    isCardManagementVerified = isCardManagementVerified,
                                    twoFactorEnabled = twoFactorEnabled,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onMarkCardManagementVerified = mainViewModel::markCardManagementVerified,
                                    onToggleCardFreeze = mainViewModel::toggleCardFreeze,
                                    onRevealToggle = mainViewModel::toggleCardSensitiveData,
                                    verifyTwoFactorCode = mainViewModel::verifyTwoFactorCode,
                                    onNavigateStore = { navigateTopLevel(BuyStoreRoute) },
                                    onCopied = ::showMessage
                                )
                            }

                            composable<BuyStoreRoute> {
                                CardStoreScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onSelectPackage = { pkg ->
                                        navController.navigate(CardSelectionRoute(pkg.id))
                                    },
                                    onOpenCardSelector = { pkgId ->
                                        navController.navigate(CardSelectionRoute(pkgId))
                                    }
                                )
                            }

                            composable<CardSelectionRoute> { backStackEntry ->
                                val args = backStackEntry.toRoute<CardSelectionRoute>()
                                CardSelectionScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    initialPackageId = args.initialPackageId,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onProceedToBuy = { packageId, designName ->
                                        navController.navigate(BuyCardRoute(packageId, designName))
                                    }
                                )
                            }

                            composable<OrdersRoute> {
                                OrdersScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onSelectOrder = { orderId -> navController.navigate(OrderDetailRoute(orderId)) },
                                    onNavigateStore = { navigateTopLevel(BuyStoreRoute) }
                                )
                            }

                            composable<ProfileRoute> {
                                ProfileScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    appLanguage = appLanguage,
                                    biometricEnabled = biometricEnabled,
                                    pinLockEnabled = pinLockEnabled,
                                    twoFactorEnabled = twoFactorEnabled,
                                    onChangeTheme = mainViewModel::setThemeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onOpenLanguagePicker = { showLanguagePicker = true },
                                    onToggleBiometric = mainViewModel::setBiometricEnabled,
                                    onUploadPhoto = mainViewModel::uploadProfilePhoto,
                                    onBeforeExternalPicker = mainViewModel::suppressNextBackgroundLock,
                                    onEditProfile = { navController.navigate(EditProfileRoute) },
                                    onChangePassword = mainViewModel::changePassword,
                                    onNavigatePinSettings = { navController.navigate(PinLockSettingsRoute) },
                                    onNavigateTwoFactor = { navController.navigate(TwoFactorRoute) },
                                    onOpenPermissionsWizard = { manualPermissionsWizardOpen = true },
                                    onNavigatePrivacyPolicy = { navController.navigate(PrivacyPolicyRoute) },
                                    onNavigateTerms = { navController.navigate(TermsRoute) },
                                    onNavigateAbout = { navController.navigate(AboutRoute) },
                                    onNavigateKyc = { navController.navigate(KycRoute) },
                                    onNavigateReferrals = { navController.navigate(ReferralRoute) },
                                    onNavigateTutorials = { navController.navigate(TutorialsRoute) },
                                    onNavigateNotificationSettings = { navController.navigate(NotificationSettingsRoute) },
                                    onNavigateTransactions = { navController.navigate(TransactionsRoute) },
                                    onNavigateCurrencyRates = { navController.navigate(CurrencyRatesRoute) },
                                    onNavigateSupport = { navController.navigate(SupportRoute) },
                                    onLogout = onLogout,
                                    onCopied = ::showMessage
                                )
                            }

                            composable<BuyCardRoute> { backStackEntry ->
                                val args = backStackEntry.toRoute<BuyCardRoute>()
                                val pkg = state.cardPackages.find { it.id == args.packageId }
                                BuyCardScreen(
                                    cardPackage = pkg,
                                    selectedDesign = args.selectedDesign,
                                    state = state,
                                    themeMode = themeMode,
                                    biometricEnabled = biometricEnabled,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onConfirmOrder = mainViewModel::createOrder
                                )
                            }

                            composable<PaymentRoute> { backStackEntry ->
                                val args = backStackEntry.toRoute<PaymentRoute>()
                                val order = state.orders.find { it.orderId == args.orderId }
                                PaymentScreen(
                                    order = order,
                                    state = state,
                                    themeMode = themeMode,
                                    biometricEnabled = biometricEnabled,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBeforeExternalPicker = mainViewModel::suppressNextBackgroundLock,
                                    onBack = { navController.popBackStack() },
                                    onSubmitPayment = mainViewModel::submitPayment,
                                    onCopied = ::showMessage
                                )
                            }

                            composable<OrderDetailRoute> { backStackEntry ->
                                val args = backStackEntry.toRoute<OrderDetailRoute>()
                                val order = state.orders.find { it.orderId == args.orderId }
                                OrderDetailScreen(
                                    order = order,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onPayNow = { orderId -> navController.navigate(PaymentRoute(orderId)) },
                                    onCopied = ::showMessage
                                )
                            }

                            composable<KycRoute> {
                                KycScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBeforeExternalPicker = mainViewModel::suppressNextBackgroundLock,
                                    onBack = { navController.popBackStack() },
                                    onSubmitKyc = mainViewModel::submitKyc
                                )
                            }

                            composable<CurrencyRatesRoute> {
                                CurrencyRatesScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<TransactionsRoute> {
                                TransactionHistoryScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onOpenOrder = { orderId -> navController.navigate(OrderDetailRoute(orderId)) }
                                )
                            }

                            composable<NotificationsRoute> {
                                NotificationsScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onMarkRead = mainViewModel::markNotificationRead,
                                    onOpenOrder = { orderId -> navController.navigate(OrderDetailRoute(orderId)) }
                                )
                            }

                            composable<SupportRoute> {
                                SupportScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onCreateTicket = mainViewModel::createSupportTicket,
                                    onSelectTicket = { ticketId -> navController.navigate(SupportDetailRoute(ticketId)) }
                                )
                            }

                            composable<SupportDetailRoute> { backStackEntry ->
                                val args = backStackEntry.toRoute<SupportDetailRoute>()
                                val ticket = state.supportTickets.find { it.ticketId == args.ticketId }
                                SupportDetailScreen(
                                    ticket = ticket,
                                    isSubmitting = state.isSubmitting,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onSendReply = mainViewModel::replyToTicket
                                )
                            }

                            composable<EditProfileRoute> {
                                EditProfileScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onUploadPhoto = mainViewModel::uploadProfilePhoto,
                                    onBeforeExternalPicker = mainViewModel::suppressNextBackgroundLock,
                                    onBack = { navController.popBackStack() },
                                    onSave = mainViewModel::updateProfile
                                )
                            }

                            composable<NotificationSettingsRoute> {
                                NotificationSettingsScreen(
                                    prefs = state.userProfile.notificationPrefs,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onSave = { prefs ->
                                        mainViewModel.updateNotificationPrefs(prefs)
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable<TutorialsRoute> {
                                TutorialsScreen(
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onComplete = {
                                        mainViewModel.setTutorialCompleted(true)
                                        showMessage("Tutorials completed!")
                                        navController.popBackStack()
                                    },
                                    onSkip = {
                                        mainViewModel.setTutorialCompleted(true)
                                        navController.popBackStack()
                                    },
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<ReferralRoute> {
                                ReferralScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() },
                                    onClaimCode = mainViewModel::claimReferralCode,
                                    onCopied = ::showMessage
                                )
                            }

                            composable<PinLockSettingsRoute> {
                                PinLockSettingsScreen(
                                    pinLockEnabled = pinLockEnabled,
                                    biometricEnabled = biometricEnabled,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onSaveOrChangePin = mainViewModel::setupOrChangePin,
                                    onDisablePinLock = mainViewModel::disablePinLock,
                                    onToggleBiometric = mainViewModel::setBiometricEnabled,
                                    onShowMessage = ::showMessage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<TwoFactorRoute> {
                                TwoFactorSetupScreen(
                                    userEmail = state.userProfile.email,
                                    twoFactorEnabled = twoFactorEnabled,
                                    savedSecret = twoFactorSecret,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onEnableTwoFactor = mainViewModel::enableTwoFactor,
                                    onDisableTwoFactor = mainViewModel::disableTwoFactor,
                                    onShowMessage = ::showMessage,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<PrivacyPolicyRoute> {
                                PrivacyPolicyScreen(
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<TermsRoute> {
                                TermsScreen(
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<AboutRoute> {
                                AboutScreen(
                                    supportEmail = state.appSettings.supportEmail,
                                    themeMode = themeMode,
                                    onToggleTheme = mainViewModel::cycleThemeMode,
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
