package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.HKPayRepository
import com.example.util.FormatUtils
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AuthSessionState {
    data object Splash : AuthSessionState
    data object Unauthenticated : AuthSessionState
    data class NeedsVerification(val email: String, val uid: String) : AuthSessionState
    data class Authenticated(val user: FirebaseUser) : AuthSessionState
}

data class AuthUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val infoMessage: String? = null,
    val resendCooldownSeconds: Int = 0
)

class AuthViewModel(
    private val authRepository: AuthRepository = AuthRepository(),
    private val hkPayRepository: HKPayRepository = HKPayRepository()
) : ViewModel() {

    private val _sessionState = MutableStateFlow<AuthSessionState>(AuthSessionState.Splash)
    val sessionState: StateFlow<AuthSessionState> = _sessionState.asStateFlow()

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            delay(1300)
            authRepository.observeAuthState().collect { user ->
                when {
                    user == null -> _sessionState.value = AuthSessionState.Unauthenticated
                    !user.isEmailVerified -> {
                        _sessionState.value = AuthSessionState.NeedsVerification(
                            email = user.email.orEmpty(),
                            uid = user.uid
                        )
                    }
                    else -> {
                        hkPayRepository.ensureUserProfileExists(
                            uid = user.uid,
                            email = user.email.orEmpty(),
                            fullName = user.displayName.orEmpty()
                        )
                        _sessionState.value = AuthSessionState.Authenticated(user)
                    }
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, infoMessage = null) }
    }

    fun signIn(email: String, password: String) {
        if (!FormatUtils.isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.signIn(email, password)
                .onSuccess { user ->
                    if (!user.isEmailVerified) {
                        _sessionState.value = AuthSessionState.NeedsVerification(
                            email = user.email.orEmpty(),
                            uid = user.uid
                        )
                    } else {
                        hkPayRepository.ensureUserProfileExists(
                            uid = user.uid,
                            email = user.email.orEmpty(),
                            fullName = user.displayName.orEmpty()
                        )
                        _sessionState.value = AuthSessionState.Authenticated(user)
                    }
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(err)) }
                }
        }
    }

    fun signUp(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        if (fullName.trim().length < 2) {
            _uiState.update { it.copy(errorMessage = "Please enter your full legal name.") }
            return
        }
        if (!FormatUtils.isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        if (password.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters.") }
            return
        }
        if (password != confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Passwords do not match.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.signUp(fullName, email, password)
                .onSuccess { user ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Verification email sent to ${user.email}. Please verify to continue."
                        )
                    }
                    _sessionState.value = AuthSessionState.NeedsVerification(
                        email = user.email.orEmpty(),
                        uid = user.uid
                    )
                    startResendCooldown()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(err)) }
                }
        }
    }

    fun sendPasswordReset(email: String) {
        if (!FormatUtils.isValidEmail(email)) {
            _uiState.update { it.copy(errorMessage = "Please enter a valid email address.") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.sendPasswordReset(email)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Password reset link sent to $email. Check your inbox."
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(err)) }
                }
        }
    }

    fun resendVerificationEmail() {
        if (_uiState.value.resendCooldownSeconds > 0) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.resendEmailVerification()
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            infoMessage = "Verification email resent. Please check your inbox and spam folder."
                        )
                    }
                    startResendCooldown()
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(err)) }
                }
        }
    }

    fun checkEmailVerification() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, infoMessage = null) }
            authRepository.reloadCurrentUser()
                .onSuccess { user ->
                    if (user.isEmailVerified) {
                        hkPayRepository.ensureUserProfileExists(
                            uid = user.uid,
                            email = user.email.orEmpty(),
                            fullName = user.displayName.orEmpty()
                        )
                        _sessionState.value = AuthSessionState.Authenticated(user)
                        _uiState.update { it.copy(isLoading = false) }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Email is not verified yet. Please tap the verification link in your email."
                            )
                        }
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = mapAuthError(err)) }
                }
        }
    }

    fun signOut() {
        authRepository.signOut()
        _uiState.value = AuthUiState()
        _sessionState.value = AuthSessionState.Unauthenticated
    }

    private fun startResendCooldown() {
        viewModelScope.launch {
            for (sec in 45 downTo 0) {
                _uiState.update { it.copy(resendCooldownSeconds = sec) }
                if (sec > 0) delay(1000)
            }
        }
    }

    private fun mapAuthError(error: Throwable): String {
        return when (error) {
            is FirebaseAuthInvalidUserException -> "No HK PAY account found for this email."
            is FirebaseAuthInvalidCredentialsException -> "Invalid email or password. Please verify and try again."
            is FirebaseAuthUserCollisionException -> "An account with this email already exists."
            is FirebaseNetworkException -> "Network error. Please check your internet connection."
            else -> error.localizedMessage ?: "Authentication failed. Please try again."
        }
    }
}
