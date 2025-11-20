package uk.ac.tees.mad.savesmart.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.savesmart.data.local.GoalDao
import uk.ac.tees.mad.savesmart.data.local.UserPreferencesManager
import uk.ac.tees.mad.savesmart.data.local.toEntity
import uk.ac.tees.mad.savesmart.data.local.toGoal
import uk.ac.tees.mad.savesmart.data.model.Goal
import java.util.UUID
import javax.inject.Inject

data class CreateGoalState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

data class DashboardState(
    val isLoading: Boolean = false,
    val goals: List<Goal> = emptyList(),
    val error: String? = null
)

data class UpdateUsernameState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class GoalViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val goalDao: GoalDao,
    private val preferencesManager: UserPreferencesManager
) : ViewModel() {

    var createGoalState by mutableStateOf(CreateGoalState())
        private set

    var updateUsernameState by mutableStateOf(UpdateUsernameState())
        private set

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState

    var goalTitle by mutableStateOf("")
        private set

    var targetAmount by mutableStateOf("")
        private set

    var startingAmount by mutableStateOf("")
        private set

    var newUsername by mutableStateOf("")
        private set

    val currentCurrency: StateFlow<String> = preferencesManager.currencyFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "GBP")

    val currentTheme: StateFlow<String> = preferencesManager.themeFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "light")

    fun toggleTheme(onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                val newTheme = if (currentTheme.value == "light") "dark" else "light"
                preferencesManager.saveTheme(newTheme)
                onSuccess()
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    init {
        loadGoalsFromCache()
    }


    fun refreshGoals() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: return@launch
            val goals = goalDao.getGoals(userId).map { it.toGoal() }
            _dashboardState.value = _dashboardState.value.copy(
                goals = goals,
                isLoading = false
            )
            println("✅ Manual refresh: ${goals.size} goals")
        }
    }

    // ✅ FIXED: Always update state, even if empty
    private fun loadGoalsFromCache() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: return@launch

            goalDao.getGoalsFlow(userId)
                .map { entities -> entities.map { it.toGoal() } }
                .collect { cachedGoals ->
                    // ✅ Always update, even if empty
                    _dashboardState.value = _dashboardState.value.copy(
                        goals = cachedGoals,
                        isLoading = false
                    )
                }
        }
    }

    private fun syncGoalsWithFirebase() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                val snapshot = firestore.collection("goals")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val goals = snapshot.documents.mapNotNull { doc ->
                    try {
                        Goal.fromMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }

                goalDao.insertGoals(goals.map { it.toEntity() })

                if (goals.isNotEmpty()) {
                    preferencesManager.saveCurrency(goals.first().currency)
                }

                _dashboardState.value = _dashboardState.value.copy(
                    goals = goals,
                    isLoading = false
                )

            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun loadGoals() {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true)
        syncGoalsWithFirebase()
    }

    // ✅ FIXED: Removed onSuccess callback
    fun createGoal() {
        if (!validateGoalInputs()) return

        viewModelScope.launch {
            createGoalState = createGoalState.copy(isLoading = true, error = null)
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw Exception("User not logged in")

                val target = targetAmount.toDoubleOrNull()
                    ?: throw Exception("Invalid target amount")

                val starting = startingAmount.toDoubleOrNull() ?: 0.0

                val goalId = "goal_${UUID.randomUUID()}"
                val goal = Goal(
                    id = goalId,
                    userId = userId,
                    title = goalTitle.trim(),
                    targetAmount = target,
                    currentAmount = starting,
                    currency = currentCurrency.value,
                    createdAt = Timestamp.now(),
                    updatedAt = Timestamp.now()
                )

                // Save to Room first
                goalDao.insertGoal(goal.toEntity())

                // Then sync to Firebase (don't wait)
                launch {
                    try {
                        firestore.collection("goals")
                            .document(goalId)
                            .set(goal.toMap())
                            .await()
                    } catch (e: Exception) {
                        println("⚠️ Failed to sync goal to Firebase: ${e.message}")
                    }
                }

                // ✅ Clear form FIRST
                goalTitle = ""
                targetAmount = ""
                startingAmount = ""

                // ✅ Then set success
                createGoalState = createGoalState.copy(
                    isLoading = false,
                    isSuccess = true
                )

            } catch (e: Exception) {
                createGoalState = createGoalState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create goal"
                )
            }
        }
    }

    fun updateAllGoalsCurrency(newCurrency: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw Exception("User not logged in")

                preferencesManager.saveCurrency(newCurrency)
                goalDao.updateCurrency(userId, newCurrency)

                val snapshot = firestore.collection("goals")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val batch = firestore.batch()
                snapshot.documents.forEach { doc ->
                    batch.update(doc.reference, "currency", newCurrency)
                }
                batch.commit().await()

                loadGoals()
                onSuccess()
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update currency"
                )
            }
        }
    }

    fun updateUsername(onSuccess: () -> Unit) {
        val trimmedUsername = newUsername.trim()

        when {
            trimmedUsername.isBlank() -> {
                updateUsernameState = updateUsernameState.copy(error = "Please enter a username")
                return
            }

            trimmedUsername.length < 3 -> {
                updateUsernameState =
                    updateUsernameState.copy(error = "Username must be at least 3 characters")
                return
            }

            trimmedUsername.length > 30 -> {
                updateUsernameState =
                    updateUsernameState.copy(error = "Username must be less than 30 characters")
                return
            }
        }

        viewModelScope.launch {
            updateUsernameState = updateUsernameState.copy(isLoading = true, error = null)
            try {
                val currentUser = firebaseAuth.currentUser
                    ?: throw Exception("User not logged in")

                preferencesManager.saveUserName(trimmedUsername)

                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedUsername)
                    .build()

                currentUser.updateProfile(profileUpdates).await()

                updateUsernameState = updateUsernameState.copy(
                    isLoading = false,
                    isSuccess = true
                )

                newUsername = ""
                onSuccess()
            } catch (e: Exception) {
                updateUsernameState = updateUsernameState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to update username"
                )
            }
        }
    }

    fun updateGoalTitle(title: String) {
        goalTitle = title
        createGoalState = createGoalState.copy(error = null)
    }

    fun updateTargetAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            targetAmount = amount
            createGoalState = createGoalState.copy(error = null)
        }
    }

    fun updateStartingAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            startingAmount = amount
            createGoalState = createGoalState.copy(error = null)
        }
    }

    fun updateNewUsername(username: String) {
        newUsername = username
        updateUsernameState = updateUsernameState.copy(error = null)
    }

    private fun validateGoalInputs(): Boolean {
        val target = targetAmount.toDoubleOrNull()
        val starting = startingAmount.toDoubleOrNull() ?: 0.0

        when {
            goalTitle.isBlank() -> {
                createGoalState = createGoalState.copy(error = "Please enter a goal name")
                return false
            }

            goalTitle.length < 3 -> {
                createGoalState =
                    createGoalState.copy(error = "Goal name must be at least 3 characters")
                return false
            }

            targetAmount.isBlank() -> {
                createGoalState = createGoalState.copy(error = "Please enter a target amount")
                return false
            }

            target == null || target <= 0 -> {
                createGoalState =
                    createGoalState.copy(error = "Please enter a valid target amount greater than 0")
                return false
            }

            target > 10000000 -> {
                createGoalState = createGoalState.copy(error = "Target amount is too large")
                return false
            }

            starting < 0 -> {
                createGoalState = createGoalState.copy(error = "Starting amount cannot be negative")
                return false
            }

            starting > target -> {
                createGoalState =
                    createGoalState.copy(error = "Starting amount cannot exceed target amount")
                return false
            }
        }
        return true
    }

    // ✅ FIXED: Only reset success flag
    fun resetCreateGoalSuccess() {
        createGoalState = createGoalState.copy(isSuccess = false)
    }

    fun resetUsernameState() {
        newUsername = ""
        updateUsernameState = UpdateUsernameState()
    }

    fun formatCurrency(amount: Double, currency: String = "GBP"): String {
        val symbol = when (currency) {
            "GBP" -> "£"
            "USD" -> "$"
            "EUR" -> "€"
            "INR" -> "₹"
            else -> currency
        }
        return "$symbol%.2f".format(amount)
    }
}