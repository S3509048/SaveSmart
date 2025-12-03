package uk.ac.tees.mad.savesmart.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.savesmart.data.local.DepositDao
import uk.ac.tees.mad.savesmart.data.local.GoalDao
import uk.ac.tees.mad.savesmart.data.local.UserPreferencesManager
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val goalDao: GoalDao,        // Inject
    private val depositDao: DepositDao,   //  Inject
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    var authState by mutableStateOf(AuthState())
        private set

    var email by mutableStateOf("")
        private set

    var password by mutableStateOf("")
        private set

    var name by mutableStateOf("")
        private set

    var confirmPassword by mutableStateOf("")
        private set

    fun updateEmail(newEmail: String) {
        email = newEmail
        authState = authState.copy(error = null)
    }

    fun updatePassword(newPassword: String) {
        password = newPassword
        authState = authState.copy(error = null)
    }

    fun updateName(newName: String) {
        name = newName
        authState = authState.copy(error = null)
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        confirmPassword = newConfirmPassword
        authState = authState.copy(error = null)
    }

    fun login(onSuccess: () -> Unit) {
        if (!validateLoginInputs()) return

        viewModelScope.launch {
            authState = authState.copy(isLoading = true, error = null)
            try {
                firebaseAuth.signInWithEmailAndPassword(email, password).await()
                authState = authState.copy(isLoading = false, isSuccess = true)
                onSuccess()
            } catch (e: Exception) {
                authState = authState.copy(
                    isLoading = false,
                    error = getErrorMessage(e)
                )
            }
        }
    }

    fun signUp(onSuccess: () -> Unit) {
        if (!validateSignUpInputs()) return

        viewModelScope.launch {
            authState = authState.copy(isLoading = true, error = null)
            try {
                // Create user account
                val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()

                // Update user profile with name
                result.user?.updateProfile(
                    userProfileChangeRequest {
                        displayName = name
                    }
                )?.await()

                authState = authState.copy(isLoading = false, isSuccess = true)
                onSuccess()
            } catch (e: Exception) {
                authState = authState.copy(
                    isLoading = false,
                    error = getErrorMessage(e)
                )
            }
        }
    }

    fun resetPassword(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (email.isBlank()) {
            onError("Please enter your email address")
            return
        }

        viewModelScope.launch {
            authState = authState.copy(isLoading = true, error = null)
            try {
                firebaseAuth.sendPasswordResetEmail(email).await()
                authState = authState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                authState = authState.copy(isLoading = false)
                onError(getErrorMessage(e))
            }
        }
    }

    private fun validateLoginInputs(): Boolean {
        when {
            email.isBlank() -> {
                authState = authState.copy(error = "Email cannot be empty")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                authState = authState.copy(error = "Please enter a valid email")
                return false
            }
            password.isBlank() -> {
                authState = authState.copy(error = "Password cannot be empty")
                return false
            }
            password.length < 6 -> {
                authState = authState.copy(error = "Password must be at least 6 characters")
                return false
            }
        }
        return true
    }

    private fun validateSignUpInputs(): Boolean {
        when {
            email.isBlank() -> {
                authState = authState.copy(error = "Email cannot be empty")
                return false
            }
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                authState = authState.copy(error = "Please enter a valid email")
                return false
            }
            name.isBlank() -> {
                authState = authState.copy(error = "Name cannot be empty")
                return false
            }
            name.length < 2 -> {
                authState = authState.copy(error = "Name must be at least 2 characters")
                return false
            }
            password.isBlank() -> {
                authState = authState.copy(error = "Password cannot be empty")
                return false
            }
            password.length < 6 -> {
                authState = authState.copy(error = "Password must be at least 6 characters")
                return false
            }
            confirmPassword.isBlank() -> {
                authState = authState.copy(error = "Please confirm your password")
                return false
            }
            password != confirmPassword -> {
                authState = authState.copy(error = "Passwords do not match")
                return false
            }
        }
        return true
    }
    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val userId=firebaseAuth.currentUser?.uid
                if(userId!=null){
                    goalDao.deleteAllGoals(userId)
                    depositDao.deleteAllDeposits(userId)
                }
                preferencesManager.clearAllPreferences()
                firebaseAuth.signOut()
                resetState() // Clears local state
                onSuccess()
            } catch (e: Exception) {
                authState = authState.copy(error = e.message ?: "Logout failed")
            }
        }
    }

    private fun getErrorMessage(exception: Exception): String {
        return when {
            exception.message?.contains("no user record", ignoreCase = true) == true ->
                "No account found with this email"
            exception.message?.contains("password is invalid", ignoreCase = true) == true ->
                "Incorrect password"
            exception.message?.contains("email address is already", ignoreCase = true) == true ->
                "This email is already registered"
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Network error. Please check your connection"
            else -> exception.message ?: "Authentication failed"
        }
    }

    fun resetState() {
        authState = AuthState()
        email = ""
        password = ""
        name = ""
        confirmPassword = ""
    }
}