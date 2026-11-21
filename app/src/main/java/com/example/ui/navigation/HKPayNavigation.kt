package com.example.ui.navigation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCard
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.rounded.AddCard
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalShipping
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.data.model.ThemeMode
import com.example.ui.components.HKPayBackdrop
import com.example.ui.screens.BiometricLockScreen
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
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReferralScreen
import com.example.ui.screens.SignInScreen
import com.example.ui.screens.SignUpScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.SupportDetailScreen
import com.example.ui.screens.SupportScreen
import com.example.ui.screens.TransactionHistoryScreen
import com.example.ui.screens.TutorialsScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.LocalHKPayColors
import com.example.ui.viewmodel.AuthSessionState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.UiEvent
import com.example.util.BiometricHelper
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

private data class TopLevelDestination(
    val route: Any,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

private val TopLevelDestinations = listOf(
    TopLevelDestination(HomeRoute, "Home", Icons.Rounded.Home, Icons.Outlined.Home, "nav_home"),
    TopLevelDestination(CardsRoute, "Cards", Icons.Rounded.CreditCard, Icons.Outlined.CreditCard, "nav_cards"),
    TopLevelDestination(BuyStoreRoute, "Buy", Icons.Rounded.AddCard, Icons.Outlined.AddCard, "nav_buy"),
    TopLevelDestination(OrdersRoute, "Orders", Icons.Rounded.LocalShipping, Icons.Outlined.LocalShipping, "nav_orders"),
    TopLevelDestination(ProfileRoute, "Profile", Icons.Rounded.Person, Icons.Outlined.Person, "nav_profile")
)

@Composable
fun HKPayAppRoot(
    authViewModel: AuthViewModel,
    mainViewModel: MainViewModel,
    themeMode: ThemeMode
) {
    val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()
    val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()
    val biometricEnabled by mainViewModel.biometricEnabled.collectAsStateWithLifecycle()
    val isBiometricUnlocked by mainViewModel.isBiometricUnlockedThisSession.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var biometricError by remember { mutableStateOf<String?>(null) }

    when (val session = sessionState) {
        is AuthSessionState.Splash -> {
            SplashScreen()
        }

        is AuthSessionState.Unauthenticated -> {
            LaunchedEffect(Unit) { mainViewModel.clearSession() }
            AuthFlowHost(
                authUiState = authUiState,
                onSignIn = authViewModel::signIn,
                onSignUp = authViewModel::signUp,
                onForgotPassword = authViewModel::sendPasswordReset,
                onClearMessages = authViewModel::clearMessages
            )
        }

        is AuthSessionState.NeedsVerification -> {
            EmailVerificationScreen(
                email = session.email,
                uiState = authUiState,
                onResend = authViewModel::resendVerificationEmail,
                onCheckVerified = authViewModel::checkEmailVerification,
                onSignOut = authViewModel::signOut
            )
        }

        is AuthSessionState.Authenticated -> {
            LaunchedEffect(session.user.uid) {
                mainViewModel.bindUser(session.user.uid)
            }

            if (biometricEnabled && !isBiometricUnlocked) {
                BiometricLockScreen(
                    userEmail = session.user.email.orEmpty(),
                    errorMessage = biometricError,
                    onPromptBiometric = {
                        val activity = context as? FragmentActivity
                        if (activity != null) {
                            biometricError = null
                            BiometricHelper.authenticate(
                                activity = activity,
                                title = "HK PAY Biometric Unlock",
                                subtitle = "Verify your fingerprint or face ID to continue",
                                negativeButtonText = "Use Email & Password",
                                onSuccess = { mainViewModel.unlockBiometrically() },
                                onNegativeAction = {
                                    mainViewModel.clearSession()
                                    authViewModel.signOut()
                                },
                                onError = { msg -> biometricError = msg }
                            )
                        } else {
                            biometricError = "Biometric prompt is unavailable in this view."
                        }
                    },
                    onUsePasswordInstead = {
                        mainViewModel.clearSession()
                        authViewModel.signOut()
                    }
                )
            } else {
                MainAuthenticatedHost(
                    mainViewModel = mainViewModel,
                    themeMode = themeMode,
                    biometricEnabled = biometricEnabled,
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
private fun AuthFlowHost(
    authUiState: com.example.ui.viewmodel.AuthUiState,
    onSignIn: (String, String) -> Unit,
    onSignUp: (String, String, String, String, String) -> Unit,
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
                onSignIn = onSignIn,
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
    biometricEnabled: Boolean,
    onLogout: () -> Unit
) {
    val state by mainViewModel.state.collectAsStateWithLifecycle()
    val tutorialCompleted by mainViewModel.tutorialCompleted.collectAsStateWithLifecycle()
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val extra = LocalHKPayColors.current
    val context = LocalContext.current

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { }
    )

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
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
                    snackbarHostState.showSnackbar("KYC documents submitted for review")
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
            }
        }
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
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label, style = MaterialTheme.typography.labelMedium) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = extra.cyanGlow,
                                        selectedTextColor = extra.cyanGlow,
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
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateTopLevel(dest.route) },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                            contentDescription = dest.label
                                        )
                                    },
                                    label = { Text(dest.label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = extra.cyanGlow,
                                        selectedTextColor = extra.cyanGlow,
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
                                    tutorialCompleted = tutorialCompleted,
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
                                    biometricEnabled = biometricEnabled,
                                    onRevealToggle = mainViewModel::toggleCardSensitiveData,
                                    onNavigateStore = { navigateTopLevel(BuyStoreRoute) },
                                    onCopied = ::showMessage
                                )
                            }

                            composable<BuyStoreRoute> {
                                CardStoreScreen(
                                    state = state,
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
                                    initialPackageId = args.initialPackageId,
                                    onBack = { navController.popBackStack() },
                                    onProceedToBuy = { packageId, designName ->
                                        navController.navigate(BuyCardRoute(packageId, designName))
                                    }
                                )
                            }

                            composable<OrdersRoute> {
                                OrdersScreen(
                                    state = state,
                                    onSelectOrder = { orderId -> navController.navigate(OrderDetailRoute(orderId)) },
                                    onNavigateStore = { navigateTopLevel(BuyStoreRoute) }
                                )
                            }

                            composable<ProfileRoute> {
                                ProfileScreen(
                                    state = state,
                                    themeMode = themeMode,
                                    biometricEnabled = biometricEnabled,
                                    onChangeTheme = mainViewModel::setThemeMode,
                                    onToggleBiometric = mainViewModel::setBiometricEnabled,
                                    onUploadPhoto = mainViewModel::uploadProfilePhoto,
                                    onEditProfile = { navController.navigate(EditProfileRoute) },
                                    onChangePassword = mainViewModel::changePassword,
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
                                    biometricEnabled = biometricEnabled,
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
                                    biometricEnabled = biometricEnabled,
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
                                    onBack = { navController.popBackStack() },
                                    onPayNow = { orderId -> navController.navigate(PaymentRoute(orderId)) },
                                    onCopied = ::showMessage
                                )
                            }

                            composable<KycRoute> {
                                KycScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onSubmitKyc = mainViewModel::submitKyc
                                )
                            }

                            composable<CurrencyRatesRoute> {
                                CurrencyRatesScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<TransactionsRoute> {
                                TransactionHistoryScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable<NotificationsRoute> {
                                NotificationsScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onMarkRead = mainViewModel::markNotificationRead,
                                    onOpenOrder = { orderId -> navController.navigate(OrderDetailRoute(orderId)) }
                                )
                            }

                            composable<SupportRoute> {
                                SupportScreen(
                                    state = state,
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
                                    onBack = { navController.popBackStack() },
                                    onSendReply = mainViewModel::replyToTicket
                                )
                            }

                            composable<EditProfileRoute> {
                                EditProfileScreen(
                                    state = state,
                                    onBack = { navController.popBackStack() },
                                    onSave = mainViewModel::updateProfile
                                )
                            }

                            composable<NotificationSettingsRoute> {
                                NotificationSettingsScreen(
                                    prefs = state.userProfile.notificationPrefs,
                                    onBack = { navController.popBackStack() },
                                    onSave = { prefs ->
                                        mainViewModel.updateNotificationPrefs(prefs)
                                        navController.popBackStack()
                                    }
                                )
                            }

                            composable<TutorialsRoute> {
                                TutorialsScreen(
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
                                    onBack = { navController.popBackStack() },
                                    onClaimCode = mainViewModel::claimReferralCode,
                                    onCopied = ::showMessage
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
