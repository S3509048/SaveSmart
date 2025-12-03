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
import uk.ac.tees.mad.savesmart.data.api.FrankfurterApi
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
    private val preferencesManager: UserPreferencesManager,
    private val frankfurterApi: FrankfurterApi  //  Inject API for conversion
) : ViewModel() {

    val notificationsEnabled: StateFlow<Boolean> = preferencesManager.notificationsEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, true)

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
        loadGoals()
    }

    fun toggleNotifications(enabled: Boolean) {
        viewModelScope.launch {
            try {
                preferencesManager.setNotificationsEnabled(enabled)
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun refreshGoals() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: return@launch
            val goals = goalDao.getGoals(userId).map { it.toGoal() }
            _dashboardState.value = _dashboardState.value.copy(
                goals = goals,
                isLoading = false
            )
        }
    }

    private fun loadGoalsFromCache() {
        viewModelScope.launch {
            val userId = firebaseAuth.currentUser?.uid ?: return@launch

            goalDao.getGoalsFlow(userId)
                .map { entities -> entities.map { it.toGoal() } }
                .collect { cachedGoals ->
                    _dashboardState.value = _dashboardState.value.copy(
                        goals = cachedGoals,
                        isLoading = false
                    )
                }
        }
    }

    //    private fun syncGoalsWithFirebase() {
//        viewModelScope.launch {
//            try {
//                val userId = firebaseAuth.currentUser?.uid ?: return@launch
//
//                val snapshot = firestore.collection("goals")
//                    .whereEqualTo("userId", userId)
//                    .get()
//                    .await()
//
//                val goals = snapshot.documents.mapNotNull { doc ->
//                    try {
//                        Goal.fromMap(doc.data ?: emptyMap())
//                    } catch (e: Exception) {
//                        null
//                    }
//                }.sortedByDescending { it.createdAt }
//
//                goalDao.insertGoals(goals.map { it.toEntity() })
//
//                if (goals.isNotEmpty()) {
//                    preferencesManager.saveCurrency(goals.first().currency)
//                }
//
//                _dashboardState.value = _dashboardState.value.copy(
//                    goals = goals,
//                    isLoading = false
//                )
//
//            } catch (e: Exception) {
//                _dashboardState.value = _dashboardState.value.copy(
//                    isLoading = false,
//                    error = e.message
//                )
//            }
//        }
//    }


//    private fun syncGoalsWithFirebase() {
//        viewModelScope.launch {
//            try {
//                val userId = firebaseAuth.currentUser?.uid ?: return@launch
//
//                val snapshot = firestore.collection("goals")
//                    .whereEqualTo("userId", userId)
//                    .get()
//                    .await()
//
//                val firebaseGoals = snapshot.documents.mapNotNull { doc ->
//                    try {
//                        Goal.fromMap(doc.data ?: emptyMap())
//                    } catch (e: Exception) {
//                        null
//                    }
//                }.sortedByDescending { it.createdAt }
//
//                // Get existing local goals
//                val localGoals = goalDao.getGoals(userId).map { it.toGoal() }
//
//                //  Merge: Keep local goals that aren't in Firebase yet
//                val localOnlyGoals = localGoals.filter { local ->
//                    firebaseGoals.none { it.id == local.id }
//                }
//
//                //  Combine Firebase goals with local-only goals
//                val mergedGoals = (firebaseGoals + localOnlyGoals)
//                    .sortedByDescending { it.createdAt }
//
//                //  Now insert the merged list
//                goalDao.insertGoals(mergedGoals.map { it.toEntity() })
//
//                if (mergedGoals.isNotEmpty()) {
//                    preferencesManager.saveCurrency(mergedGoals.first().currency)
//                }
//
//                _dashboardState.value = _dashboardState.value.copy(
//                    goals = mergedGoals,
//                    isLoading = false,
//                    error = null
//                )
//
//            } catch (e: Exception) {
//                //  On error (like no internet), just stop loading
//                // Don't modify the database or show error
//                _dashboardState.value = _dashboardState.value.copy(
//                    isLoading = false,
//                    error = null  // Don't show error on refresh failure
//                )
//            }
//        }
//    }

    private fun syncGoalsWithFirebase() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch

                val snapshot = firestore.collection("goals")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val firebaseGoals = snapshot.documents.mapNotNull { doc ->
                    try {
                        Goal.fromMap(doc.data ?: emptyMap())
                    } catch (e: Exception) {
                        null
                    }
                }.sortedByDescending { it.createdAt }

                //  FIX: Keep entities to preserve sync status
                val localGoalsEntities = goalDao.getGoals(userId)

                //  Separate synced and unsynced local goals
                val unsyncedLocalGoals = localGoalsEntities
                    .filter { !it.isSynced }
                    .map { it.toGoal() }

                val syncedLocalGoalIds = localGoalsEntities
                    .filter { it.isSynced }
                    .map { it.goalId }
                    .toSet()

                //  Keep NEW local goals (not in Firebase at all)
                val newLocalGoals = localGoalsEntities
                    .filter { entity ->
                        firebaseGoals.none { it.id == entity.goalId }
                    }
                    .map { it.toGoal() }

                //  Merge: Unsynced local + New local + Firebase (but not ones that are unsynced locally)
                val mergedGoals = (unsyncedLocalGoals + newLocalGoals +
                        firebaseGoals.filter { fbGoal ->
                            // Only take Firebase goals that aren't unsynced locally
                            unsyncedLocalGoals.none { it.id == fbGoal.id }
                        })
                    .distinctBy { it.id }
                    .sortedByDescending { it.createdAt }

                //  Insert with preserved sync status
                mergedGoals.forEach { goal ->
                    val entity = goal.toEntity()
                    val existingEntity = localGoalsEntities.find { it.goalId == goal.id }

                    // Preserve sync status if exists locally
                    if (existingEntity != null) {
                        goalDao.insertGoal(entity.copy(isSynced = existingEntity.isSynced))
                    } else {
                        goalDao.insertGoal(entity.copy(isSynced = true)) // New from Firebase
                    }
                }

                if (mergedGoals.isNotEmpty()) {
                    preferencesManager.saveCurrency(mergedGoals.first().currency)
                }

                _dashboardState.value = _dashboardState.value.copy(
                    goals = mergedGoals,
                    isLoading = false,
                    error = null
                )

                //  Sync unsynced goals to Firebase
                syncUnsyncedGoalAmounts()

            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = null
                )
            }
        }
    }

    private fun syncUnsyncedGoalAmounts() {
        viewModelScope.launch {
            try {
                val unsyncedGoals = goalDao.getUnsyncedGoals()

                if (unsyncedGoals.isEmpty()) {
                    println(" All goal amounts already synced")
                    return@launch
                }

                println("📤 Syncing ${unsyncedGoals.size} unsynced goal amount(s)...")

                unsyncedGoals.forEach { goalEntity ->
                    try {
                        firestore.collection("goals")
                            .document(goalEntity.goalId)
                            .update(
                                mapOf(
                                    "currentAmount" to goalEntity.currentAmount,
                                    "updatedAt" to Timestamp.now()
                                )
                            )
                            .await()

                        //  Mark as synced
                        goalDao.updateGoalAmount(
                            goalId = goalEntity.goalId,
                            newAmount = goalEntity.currentAmount,
                            timestamp = System.currentTimeMillis(),
                            synced = true
                        )

                        println(" Synced goal amount: ${goalEntity.goalId}")
                    } catch (e: Exception) {
                        println("⚠️ Failed to sync goal ${goalEntity.goalId}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                println("⚠️ Goal amount sync failed: ${e.message}")
            }
        }
    }



    fun syncUnsyncedGoals() {
        viewModelScope.launch {
            try {
                val userId = firebaseAuth.currentUser?.uid ?: return@launch
                val localGoals = goalDao.getGoals(userId).map { it.toGoal() }

                if (localGoals.isEmpty()) return@launch

                // Get Firebase goal IDs to check what's missing
                val snapshot = firestore.collection("goals")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()

                val firebaseGoalIds = snapshot.documents.map { it.id }.toSet()

                // Find unsynced goals
                val unsyncedGoals = localGoals.filter { it.id !in firebaseGoalIds }

                if (unsyncedGoals.isEmpty()) {
                    println(" All goals already synced")
                    return@launch
                }

                println("📤 Syncing ${unsyncedGoals.size} unsynced goal(s)...")

                // Sync each unsynced goal
                unsyncedGoals.forEach { goal ->
                    try {
                        firestore.collection("goals")
                            .document(goal.id)
                            .set(goal.toMap())
                            .await()
                        println(" Synced goal: ${goal.title}")
                    } catch (e: Exception) {
                        println("⚠️ Failed to sync goal ${goal.id}: ${e.message}")
                    }
                }

                // Refresh after syncing
                loadGoals()

            } catch (e: Exception) {
                println("⚠️ Sync check failed: ${e.message}")
            }
        }
    }

    fun loadGoals() {
        _dashboardState.value = _dashboardState.value.copy(isLoading = true)
        syncGoalsWithFirebase()
    }

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

                goalDao.insertGoal(goal.toEntity())

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

                goalTitle = ""
                targetAmount = ""
                startingAmount = ""

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

    //  UPDATED: Now converts actual amounts, not just symbol
    fun updateAllGoalsCurrency(newCurrency: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw Exception("User not logged in")

                val goals = _dashboardState.value.goals

                if (goals.isEmpty()) {
                    // No goals, just update preference
                    preferencesManager.saveCurrency(newCurrency)
                    _dashboardState.value = _dashboardState.value.copy(isLoading = false)
                    onSuccess()
                    return@launch
                }

                val oldCurrency = goals.first().currency

                //  If same currency, just return
                if (oldCurrency == newCurrency) {
                    _dashboardState.value = _dashboardState.value.copy(isLoading = false)
                    onSuccess()
                    return@launch
                }

                //  Get conversion rate (using 1.0 as base amount)
                val conversionResponse = frankfurterApi.convertCurrency(
                    amount = 1.0,
                    fromCurrency = oldCurrency,
                    toCurrency = newCurrency
                )

                val conversionRate = conversionResponse.rates[newCurrency]
                    ?: throw Exception("Failed to get conversion rate")

                //  Convert all goals with new amounts
                val convertedGoals = goals.map { goal ->
                    goal.copy(
                        currency = newCurrency,
                        currentAmount = goal.currentAmount * conversionRate,
                        targetAmount = goal.targetAmount * conversionRate,
                        updatedAt = Timestamp.now()
                    )
                }

                // Update Room database
                goalDao.insertGoals(convertedGoals.map { it.toEntity() })

                //  Update Firebase
                val batch = firestore.batch()
                convertedGoals.forEach { goal ->
                    val goalRef = firestore.collection("goals").document(goal.id)
                    batch.update(
                        goalRef,
                        mapOf(
                            "currency" to goal.currency,
                            "currentAmount" to goal.currentAmount,
                            "targetAmount" to goal.targetAmount,
                            "updatedAt" to goal.updatedAt
                        )
                    )
                }
                batch.commit().await()

                // Update preference
                preferencesManager.saveCurrency(newCurrency)

                //  Refresh UI
                _dashboardState.value = _dashboardState.value.copy(
                    goals = convertedGoals,
                    isLoading = false
                )

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
                preferencesManager.saveUserName(trimmedUsername)
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(trimmedUsername)
                    .build()
                val currentUser = firebaseAuth.currentUser
                    ?: throw Exception("User not logged in")

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