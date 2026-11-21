package com.example

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notifications.NotificationHelper
import com.example.ui.navigation.HKPayAppRoot
import com.example.ui.theme.HKPayTheme
import com.example.ui.viewmodel.AuthSessionState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel
import com.example.util.LocalAppLanguage

class MainActivity : FragmentActivity() {

    private var pendingOrderId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pendingOrderId = intent?.getStringExtra(NotificationHelper.EXTRA_ORDER_ID)?.takeIf { it.isNotBlank() }

        setContent {
            val authViewModel: AuthViewModel = viewModel()
            val mainViewModel: MainViewModel = viewModel()
            val themeMode by mainViewModel.themeMode.collectAsStateWithLifecycle()
            val appLanguage by mainViewModel.appLanguage.collectAsStateWithLifecycle()
            val sessionState by authViewModel.sessionState.collectAsStateWithLifecycle()
            val lifecycleOwner = LocalLifecycleOwner.current

            // Enforce App Lock whenever app moves to background (unless suppressed for external picker or config change)
            DisposableEffect(lifecycleOwner, mainViewModel) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_STOP && !this@MainActivity.isChangingConfigurations) {
                        mainViewModel.lockAppIfEnabled()
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }

            // Block screenshots / screen recording while inside authenticated financial screens (Dashboard & Cards)
            DisposableEffect(sessionState) {
                if (sessionState is AuthSessionState.Authenticated) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
                } else {
                    window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                }
                onDispose { }
            }

            CompositionLocalProvider(LocalAppLanguage provides appLanguage) {
                HKPayTheme(themeMode = themeMode) {
                    HKPayAppRoot(
                        authViewModel = authViewModel,
                        mainViewModel = mainViewModel,
                        themeMode = themeMode,
                        pendingOrderId = pendingOrderId,
                        onConsumePendingOrderId = { pendingOrderId = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val orderId = intent.getStringExtra(NotificationHelper.EXTRA_ORDER_ID)?.takeIf { it.isNotBlank() }
        if (orderId != null) {
            pendingOrderId = orderId
        }
    }
}
