package com.example.data.repository

import com.example.data.model.HKPayConstants
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.Locale

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(HKPayConstants.FIREBASE_DATABASE_URL)
) {

    val currentUser: FirebaseUser?
        get() = auth.currentUser

    fun observeAuthState(): Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser)
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signUp(
        fullName: String,
        email: String,
        password: String
    ): Result<FirebaseUser> = runCatching {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val result = auth.createUserWithEmailAndPassword(cleanEmail, password).awaitResult()
        val user = result.user ?: error("Unable to initialize user account.")

        user.updateProfile(
            UserProfileChangeRequest.Builder()
                .setDisplayName(cleanName)
                .build()
        ).awaitResult()

        user.sendEmailVerification().awaitResult()

        val now = System.currentTimeMillis()
        val publicUserId = generatePublicUserId(user.uid)
        val profileMap = mapOf(
            "uid" to user.uid,
            "publicUserId" to publicUserId,
            "fullName" to cleanName,
            "email" to cleanEmail,
            "phone" to "",
            "country" to "Bangladesh",
            "photoUrl" to "",
            "joinDate" to now,
            "kycStatus" to "Not Submitted",
            "accountStatus" to "Active",
            "notificationPrefs" to mapOf(
                "orderUpdates" to true,
                "securityAlerts" to true,
                "rateAlerts" to true,
                "marketingNotices" to false
            )
        )

        database.reference.child("users").child(user.uid).setValue(profileMap).awaitResult()

        val welcomeNotifRef = database.reference.child("notifications").child(user.uid).push()
        val welcomeNotif = mapOf(
            "id" to (welcomeNotifRef.key ?: ""),
            "userId" to user.uid,
            "title" to "Welcome to HK PAY",
            "message" to "Your HK PAY account ($publicUserId) has been created. Verify your email and complete KYC to unlock physical card issuance.",
            "type" to "System Notice",
            "referenceId" to user.uid,
            "isRead" to false,
            "createdAt" to now
        )
        welcomeNotifRef.setValue(welcomeNotif).awaitResult()

        user
    }

    suspend fun signIn(email: String, password: String): Result<FirebaseUser> = runCatching {
        val result = auth.signInWithEmailAndPassword(email.trim(), password).awaitResult()
        result.user ?: error("Authentication failed.")
    }

    suspend fun sendPasswordReset(email: String): Result<Unit> = runCatching {
        auth.sendPasswordResetEmail(email.trim()).awaitResult()
        Unit
    }

    suspend fun resendEmailVerification(): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No active user session.")
        user.sendEmailVerification().awaitResult()
        Unit
    }

    suspend fun reloadCurrentUser(): Result<FirebaseUser> = runCatching {
        val user = auth.currentUser ?: error("No active user session.")
        user.reload().awaitResult()
        auth.currentUser ?: error("Session expired.")
    }

    suspend fun changePassword(currentPassword: String, newPassword: String): Result<Unit> = runCatching {
        val user = auth.currentUser ?: error("No active user session.")
        val email = user.email ?: error("Account email is unavailable.")
        val credential = EmailAuthProvider.getCredential(email, currentPassword)
        user.reauthenticate(credential).awaitResult()
        user.updatePassword(newPassword).awaitResult()
        Unit
    }

    fun signOut() {
        auth.signOut()
    }

    companion object {
        fun generatePublicUserId(uid: String): String {
            val clean = uid.replace(Regex("[^A-Za-z0-9]"), "").uppercase(Locale.US)
            val suffix = if (clean.length >= 6) clean.take(6) else clean.padEnd(6, '0')
            return "HKP-$suffix"
        }
    }
}
