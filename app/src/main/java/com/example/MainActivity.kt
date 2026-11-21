package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.notifications.NotificationHelper
import com.example.ui.navigation.HKPayAppRoot
import com.example.ui.theme.HKPayTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainViewModel

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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val orderId = intent.getStringExtra(NotificationHelper.EXTRA_ORDER_ID)?.takeIf { it.isNotBlank() }
        if (orderId != null) {
            pendingOrderId = orderId
        }
    }
}
