package com.example

import android.app.Application
import com.example.data.model.HKPayConstants
import com.example.notifications.NotificationHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.database.FirebaseDatabase

class HKPayApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)

        runCatching {
            FirebaseDatabase.getInstance(HKPayConstants.FIREBASE_DATABASE_URL)
                .setPersistenceEnabled(true)
        }

        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        val providerFactory = if (BuildConfig.DEBUG) {
            DebugAppCheckProviderFactory.getInstance()
        } else {
            PlayIntegrityAppCheckProviderFactory.getInstance()
        }
        firebaseAppCheck.installAppCheckProviderFactory(providerFactory)

        NotificationHelper.createNotificationChannels(this)
    }
}
