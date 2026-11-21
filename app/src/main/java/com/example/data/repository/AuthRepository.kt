package com.example.data.repository

import android.net.Uri
import com.example.data.model.HKPayConstants
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.util.Locale

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(HKPayConstants.FIREBASE_DATABASE_URL),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
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
        phone: String,
        password: String,
        nidNumber: String = "",
        nidFrontUri: Uri? = null,
        nidBackUri: Uri? = null,
        livenessUri: Uri? = null,
        livenessChallenge: String = "",
        referralCodeInput: String = "",
        referralBonusUsd: Double = REFERRAL_BONUS_USD,
        country: String = "Bangladesh",
        district: String = "",
        cityUpazila: String = "",
        pinEnabled: Boolean = false,
        biometricEnabled: Boolean = false
    ): Result<FirebaseUser> = runCatching {
        val cleanName = fullName.trim()
        val cleanEmail = email.trim()
        val cleanPhone = phone.trim()
        val cleanNid = nidNumber.trim()
        val cleanRefCode = referralCodeInput.trim().uppercase(Locale.US)

        // Pre-validate referral code before creating the auth account
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
        val now = System.currentTimeMillis()
        val publicUserId = generatePublicUserId(user.uid)
        val ownReferralCode = generateReferralCode(user.uid)
        val hasValidReferral = referrerUid.isNotBlank() && referrerUid != user.uid
        val welcomeBonus = if (hasValidReferral) referralBonusUsd else 0.0
        val hasLiveness = livenessUri != null
        val hasKycDocs = cleanNid.isNotBlank() && nidFrontUri != null

        // Run Firebase Auth profile update, email verification link, and Storage uploads in parallel
        val (frontUrl, backUrl, selfieUrl) = withContext(Dispatchers.IO) {
            coroutineScope {
                val profileUpdate = async {
                    runCatching {
                        user.updateProfile(
                            UserProfileChangeRequest.Builder()
                                .setDisplayName(cleanName)
                                .build()
                        ).awaitResult()
                    }
                }
                val verificationSend = async {
                    user.sendEmailVerification().awaitResult()
                }
                val frontTask = async {
                    nidFrontUri?.let { uri -> uploadKycImage(user.uid, "front_$now.jpg", uri) }.orEmpty()
                }
                val backTask = async {
                    nidBackUri?.let { uri -> uploadKycImage(user.uid, "back_$now.jpg", uri) }.orEmpty()
                }
                val livenessTask = async {
                    livenessUri?.let { uri -> uploadKycImage(user.uid, "liveness_$now.jpg", uri) }.orEmpty()
                }

                profileUpdate.await()
                verificationSend.await()
                Triple(frontTask.await(), backTask.await(), livenessTask.await())
            }
        }

        val initialKycStatus = if (hasKycDocs) "Pending" else "Not Submitted"
        val profileMap = mapOf(
            "uid" to user.uid,
            "publicUserId" to publicUserId,
            "fullName" to cleanName,
            "email" to cleanEmail,
            "phone" to cleanPhone,
            "country" to country.trim().ifBlank { "Bangladesh" },
            "district" to district.trim(),
            "cityUpazila" to cityUpazila.trim(),
            "nidNumber" to cleanNid,
            "livenessVerified" to hasLiveness,
            "livenessChallenge" to livenessChallenge,
            "livenessTimestamp" to if (hasLiveness) now else 0L,
            "preferredCurrency" to "USD",
            "photoUrl" to "",
            "joinDate" to now,
            "kycStatus" to initialKycStatus,
            "accountStatus" to "Active",
            "biometricEnabled" to biometricEnabled,
            "pinEnabled" to pinEnabled,
            "twoFactorEnabled" to false,
            "twoFactorSecret" to "",
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

        val welcomeNotifRef = database.reference.child("notifications").child(user.uid).push()
        val welcomeMessage = if (hasValidReferral) {
            "Your HK PAY account ($publicUserId) is ready with a $${String.format(Locale.US, "%.2f", referralBonusUsd)} referral fee credit! Verify your email link to continue."
        } else {
            "Your HK PAY account ($publicUserId) is created. Click the verification link sent to $cleanEmail to activate card ordering."
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

        // Persist all user, referral, KYC, and welcome nodes concurrently in 1 RTT
        withContext(Dispatchers.IO) {
            coroutineScope {
                val tasks = mutableListOf<kotlinx.coroutines.Deferred<Any?>>(
                    async { database.reference.child("users").child(user.uid).setValue(profileMap).awaitResult() },
                    async {
                        database.reference.child("referral_codes").child(ownReferralCode).setValue(
                            mapOf(
                                "uid" to user.uid,
                                "ownerName" to cleanName,
                                "createdAt" to now
                            )
                        ).awaitResult()
                    },
                    async { welcomeNotifRef.setValue(welcomeNotif).awaitResult() }
                )

                if (hasKycDocs) {
                    val kycMap = mapOf(
                        "userId" to user.uid,
                        "fullName" to cleanName,
                        "dateOfBirth" to "",
                        "country" to country.trim().ifBlank { "Bangladesh" },
                        "documentType" to "NID",
                        "documentNumber" to cleanNid,
                        "frontDocumentUrl" to frontUrl,
                        "backDocumentUrl" to backUrl,
                        "selfieUrl" to selfieUrl,
                        "livenessChallenge" to livenessChallenge,
                        "livenessTimestamp" to if (hasLiveness) now else 0L,
                        "livenessVerified" to hasLiveness,
                        "status" to "Pending",
                        "rejectionReason" to "",
                        "submittedAt" to now,
                        "reviewedAt" to 0L
                    )
                    tasks.add(async {
                        database.reference.child("kyc_submissions").child(user.uid).setValue(kycMap).awaitResult()
                    })
                }

                if (hasValidReferral) {
                    tasks.add(async {
                        recordReferralRewards(
                            referrerUid = referrerUid,
                            newUid = user.uid,
                            newUserName = cleanName,
                            codeUsed = cleanRefCode,
                            bonusUsd = referralBonusUsd,
                            timestamp = now
                        )
                    })
                }

                tasks.awaitAll()
            }
        }

        user
    }

    private suspend fun uploadKycImage(uid: String, fileName: String, uri: Uri): String {
        val ref = storage.reference.child("kyc_uploads/$uid/$fileName")
        ref.putFile(uri).awaitResult()
        return ref.downloadUrl.awaitResult().toString()
    }

    private suspend fun recordReferralRewards(
        referrerUid: String,
        newUid: String,
        newUserName: String,
        codeUsed: String,
        bonusUsd: Double,
        timestamp: Long
    ) = coroutineScope {
        val referrerRewardId = "REF_${newUid.take(8)}"
        val referrerNotifRef = database.reference.child("notifications").child(referrerUid).push()

        awaitAll(
            async {
                database.reference.child("referrals").child(referrerUid).child(newUid).setValue(
                    mapOf(
                        "referredUserId" to newUid,
                        "referredName" to newUserName,
                        "rewardAmount" to bonusUsd,
                        "refereeBonus" to bonusUsd,
                        "status" to "Credited",
                        "createdAt" to timestamp
                    )
                ).awaitResult()
            },
            async {
                database.reference.child("referral_rewards").child(newUid).child("WELCOME_${newUid.take(6)}").setValue(
                    mapOf(
                        "rewardId" to "WELCOME_${newUid.take(6)}",
                        "sourceUid" to newUid,
                        "title" to "Welcome Referral Bonus ($codeUsed)",
                        "amount" to bonusUsd,
                        "currency" to "USD",
                        "type" to "Welcome Fee Credit",
                        "status" to "Available",
                        "createdAt" to timestamp
                    )
                ).awaitResult()
            },
            async {
                database.reference.child("referral_rewards").child(referrerUid).child(referrerRewardId).setValue(
                    mapOf(
                        "rewardId" to referrerRewardId,
                        "sourceUid" to newUid,
                        "title" to "Friend Joined: $newUserName",
                        "amount" to bonusUsd,
                        "currency" to "USD",
                        "type" to "Referral Bonus",
                        "status" to "Available",
                        "createdAt" to timestamp
                    )
                ).awaitResult()
            },
            async {
                referrerNotifRef.setValue(
                    mapOf(
                        "id" to (referrerNotifRef.key ?: ""),
                        "userId" to referrerUid,
                        "title" to "Referral Reward Earned!",
                        "message" to "$newUserName signed up with your referral code ($codeUsed). You earned $${String.format(Locale.US, "%.2f", bonusUsd)} card fee credit.",
                        "type" to "Referral Bonus",
                        "referenceId" to referrerRewardId,
                        "isRead" to false,
                        "createdAt" to timestamp
                    )
                ).awaitResult()
            }
        )
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
