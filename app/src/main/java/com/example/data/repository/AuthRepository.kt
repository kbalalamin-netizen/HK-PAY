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
        password: String,
        referralCodeInput: String = ""
    ): Result<FirebaseUser> = runCatching {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanRefCode = referralCodeInput.trim().uppercase(Locale.US)

        var referrerUid = ""
        if (cleanRefCode.isNotBlank()) {
            val codeSnap = database.reference.child("referral_codes").child(cleanRefCode).get().awaitResult()
            if (!codeSnap.exists()) {
                error("Referral code \"$cleanRefCode\" was not found. Please check or leave it blank.")
            }
            referrerUid = codeSnap.child("uid").value?.toString().orEmpty()
            if (referrerUid.isBlank()) {
                error("Referral code \"$cleanRefCode\" is invalid.")
            }
        }

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
        val ownReferralCode = generateReferralCode(user.uid)
        val hasValidReferral = referrerUid.isNotBlank() && referrerUid != user.uid
        val welcomeBonus = if (hasValidReferral) REFERRAL_BONUS_USD else 0.0

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
            "biometricEnabled" to false,
            "referralCode" to ownReferralCode,
            "referredBy" to if (hasValidReferral) referrerUid else "",
            "referredByCode" to if (hasValidReferral) cleanRefCode else "",
            "rewardCredits" to welcomeBonus,
            "notificationPrefs" to mapOf(
                "orderUpdates" to true,
                "securityAlerts" to true,
                "rateAlerts" to true,
                "marketingNotices" to false
            )
        )

        database.reference.child("users").child(user.uid).setValue(profileMap).awaitResult()
        database.reference.child("referral_codes").child(ownReferralCode).setValue(
            mapOf(
                "uid" to user.uid,
                "ownerName" to cleanName,
                "createdAt" to now
            )
        ).awaitResult()

        if (hasValidReferral) {
            recordReferralRewards(
                referrerUid = referrerUid,
                newUid = user.uid,
                newUserName = cleanName,
                codeUsed = cleanRefCode,
                timestamp = now
            )
        }

        val welcomeNotifRef = database.reference.child("notifications").child(user.uid).push()
        val welcomeMessage = if (hasValidReferral) {
            "Your HK PAY account ($publicUserId) is ready with a $${String.format(Locale.US, "%.2f", REFERRAL_BONUS_USD)} referral fee credit! Verify your email to continue."
        } else {
            "Your HK PAY account ($publicUserId) has been created. Verify your email and complete KYC to unlock physical card issuance."
        }
        val welcomeNotif = mapOf(
            "id" to (welcomeNotifRef.key ?: ""),
            "userId" to user.uid,
            "title" to "Welcome to HK PAY",
            "message" to welcomeMessage,
            "type" to "System Notice",
            "referenceId" to user.uid,
            "isRead" to false,
            "createdAt" to now
        )
        welcomeNotifRef.setValue(welcomeNotif).awaitResult()

        user
    }

    private suspend fun recordReferralRewards(
        referrerUid: String,
        newUid: String,
        newUserName: String,
        codeUsed: String,
        timestamp: Long
    ) {
        database.reference.child("referrals").child(referrerUid).child(newUid).setValue(
            mapOf(
                "referredUserId" to newUid,
                "referredName" to newUserName,
                "rewardAmount" to REFERRAL_BONUS_USD,
                "refereeBonus" to REFERRAL_BONUS_USD,
                "status" to "Credited",
                "createdAt" to timestamp
            )
        ).awaitResult()

        database.reference.child("referral_rewards").child(newUid).child("WELCOME_${newUid.take(6)}").setValue(
            mapOf(
                "rewardId" to "WELCOME_${newUid.take(6)}",
                "sourceUid" to newUid,
                "title" to "Welcome Referral Bonus ($codeUsed)",
                "amount" to REFERRAL_BONUS_USD,
                "currency" to "USD",
                "type" to "Welcome Fee Credit",
                "status" to "Available",
                "createdAt" to timestamp
            )
        ).awaitResult()

        val referrerRewardId = "REF_${newUid.take(8)}"
        database.reference.child("referral_rewards").child(referrerUid).child(referrerRewardId).setValue(
            mapOf(
                "rewardId" to referrerRewardId,
                "sourceUid" to newUid,
                "title" to "Friend Joined: $newUserName",
                "amount" to REFERRAL_BONUS_USD,
                "currency" to "USD",
                "type" to "Referral Bonus",
                "status" to "Available",
                "createdAt" to timestamp
            )
        ).awaitResult()

        val referrerNotifRef = database.reference.child("notifications").child(referrerUid).push()
        referrerNotifRef.setValue(
            mapOf(
                "id" to (referrerNotifRef.key ?: ""),
                "userId" to referrerUid,
                "title" to "Referral Reward Earned!",
                "message" to "$newUserName signed up with your referral code ($codeUsed). You earned $${String.format(Locale.US, "%.2f", REFERRAL_BONUS_USD)} card fee credit.",
                "type" to "Referral Bonus",
                "referenceId" to referrerRewardId,
                "isRead" to false,
                "createdAt" to timestamp
            )
        ).awaitResult()
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
        const val REFERRAL_BONUS_USD = 5.0

        fun generatePublicUserId(uid: String): String {
            val clean = uid.replace(Regex("[^A-Za-z0-9]"), "").uppercase(Locale.US)
            val suffix = if (clean.length >= 6) clean.take(6) else clean.padEnd(6, '0')
            return "HKP-$suffix"
        }

        fun generateReferralCode(uid: String): String {
            val clean = uid.replace(Regex("[^A-Za-z0-9]"), "").uppercase(Locale.US)
            val suffix = if (clean.length >= 6) clean.takeLast(6) else clean.padStart(6, 'X')
            return "HKR$suffix"
        }
    }
}
