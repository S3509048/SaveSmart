package uk.ac.tees.mad.savesmart.viewmodel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import uk.ac.tees.mad.savesmart.data.api.FrankfurterApi
import uk.ac.tees.mad.savesmart.data.local.DepositDao
import uk.ac.tees.mad.savesmart.data.local.GoalDao
import uk.ac.tees.mad.savesmart.data.local.toDeposit
import uk.ac.tees.mad.savesmart.data.local.toEntity
import uk.ac.tees.mad.savesmart.data.local.toGoal
import uk.ac.tees.mad.savesmart.data.model.ConversionState
import uk.ac.tees.mad.savesmart.data.model.Deposit
import uk.ac.tees.mad.savesmart.data.model.Goal
import uk.ac.tees.mad.savesmart.utils.MilestoneTracker
import java.util.UUID
import javax.inject.Inject

data class AddSavingsState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val currentGoal: Goal? = null
)

@HiltViewModel
class SavingsViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val goalDao: GoalDao,
    private val depositDao: DepositDao,
    private val frankfurterApi: FrankfurterApi,
    @ApplicationContext private val context: Context
) : ViewModel() {

    var addSavingsState by mutableStateOf(AddSavingsState())
        private set

    var currentCurrency by mutableStateOf("GBP")
        private set

    var depositAmount by mutableStateOf("")
        private set

    var depositNote by mutableStateOf("")
        private set

    // Currency conversion state
    var conversionState by mutableStateOf(ConversionState())
        private set

    fun updateDepositAmount(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            depositAmount = amount
            addSavingsState = addSavingsState.copy(error = null)
        }

        //Auto-convert when amount changes
        if (amount.isNotEmpty()) {
            convertCurrency(amount.toDoubleOrNull() ?: 0.0)
        } else {
            conversionState = ConversionState()
        }
    }

    fun updateDepositNote(note: String) {
        depositNote = note
    }

    // Update target currency for conversion
    fun updateTargetCurrency(currency: String) {
        conversionState = conversionState.copy(targetCurrency = currency)

        // Re-convert with new currency if amount exists
        val amount = depositAmount.toDoubleOrNull()
        if (amount != null && amount > 0) {
            convertCurrency(amount)
        }
    }

    // Currency conversion function
    private fun convertCurrency(amount: Double) {
        // Don't convert if same currency
        if (conversionState.targetCurrency == currentCurrency) {
            conversionState = ConversionState(
                targetCurrency = conversionState.targetCurrency,
                convertedAmount = amount
            )
            return
        }

        viewModelScope.launch {
            conversionState = conversionState.copy(isLoading = true, error = null)
            try {
                val response = frankfurterApi.convertCurrency(
                    amount = amount,
                    fromCurrency = currentCurrency,
                    toCurrency = conversionState.targetCurrency
                )

                val convertedAmount = response.rates[conversionState.targetCurrency]

                conversionState = conversionState.copy(
                    isLoading = false,
                    convertedAmount = convertedAmount,
                    error = null
                )
            } catch (e: Exception) {
                conversionState = conversionState.copy(
                    isLoading = false,
                    error = "Conversion failed"
                )
            }
        }
    }

    fun loadGoal(goalId: String) {
        viewModelScope.launch {
            addSavingsState = addSavingsState.copy(isLoading = true, error = null)
            try {
                val cachedGoal = goalDao.getGoal(goalId)

                if (cachedGoal != null) {
                    val goal = cachedGoal.toGoal()
                    currentCurrency = goal.currency
                    addSavingsState = addSavingsState.copy(
                        isLoading = false,
                        currentGoal = goal
                    )
                } else {
                    loadGoalFromFirebase(goalId)
                }
            } catch (e: Exception) {
                addSavingsState = addSavingsState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load goal"
                )
            }
        }
    }

    private suspend fun loadGoalFromFirebase(goalId: String) {
        try {
            val goalDoc = firestore.collection("goals")
                .document(goalId)
                .get()
                .await()

            if (goalDoc.exists()) {
                val goal = Goal.fromMap(goalDoc.data ?: emptyMap())
                goalDao.insertGoal(goal.toEntity())
                currentCurrency = goal.currency
                addSavingsState = addSavingsState.copy(
                    isLoading = false,
                    currentGoal = goal
                )
            } else {
                addSavingsState = addSavingsState.copy(
                    isLoading = false,
                    error = "Goal not found"
                )
            }
        } catch (e: Exception) {
            addSavingsState = addSavingsState.copy(
                isLoading = false,
                error = "Goal not found in cache. Please connect to internet."
            )
        }
    }

    //  Removed onSuccess callback, handle navigation in UI
    fun addDeposit(
        goalId: String
    ) {
        if (!validateDeposit()) return

        viewModelScope.launch {
            addSavingsState = addSavingsState.copy(isLoading = true, error = null)
            try {
                val userId = firebaseAuth.currentUser?.uid
                    ?: throw Exception("User not logged in")

                val amount = depositAmount.toDoubleOrNull()
                    ?: throw Exception("Invalid amount")

                val depositId = UUID.randomUUID().toString()
                val deposit = Deposit(
                    id = depositId,
                    goalId = goalId,
                    userId = userId,
                    amount = amount,
                    note = depositNote,
                    createdAt = Timestamp.now(),
                    isSynced = false
                )

                // 1. Save deposit to Room FIRST
                depositDao.insertDeposit(deposit.toEntity())

                // 2. Update goal amount in Room
                val currentGoal = goalDao.getGoal(goalId)
                if (currentGoal != null) {

                    // for milestone acheivement

                    val oldAmount = currentGoal.currentAmount
                    val newAmount = oldAmount + amount


//                    val newAmount = currentGoal.currentAmount + amount
                    goalDao.updateGoalAmount(
                        goalId = goalId,
                        newAmount = newAmount,
                        timestamp = System.currentTimeMillis(),
                        synced = false
                    )
                    val goal = currentGoal.toGoal()
                    val reachedMilestone = MilestoneTracker.checkAndNotifyMilestone(
                        context = context,
                        goal = goal,
                        oldAmount = oldAmount,
                        newAmount = newAmount
                    )

                }

                // 3. Try to sync with Firebase in background (don't wait)
                launch {
                    try {
                        syncDepositToFirebase(goalId, depositId, amount)
                    } catch (e: Exception) {
                        println("️ Offline mode: Deposit saved locally, will sync later")
                    }
                }

                // FIXED: Clear form data FIRST, then set success
                depositAmount = ""
                depositNote = ""
                conversionState = ConversionState()

                //  Set success state (will be caught by LaunchedEffect)
                addSavingsState = addSavingsState.copy(
                    isLoading = false,
                    isSuccess = true,
                    error = null
                )

            } catch (e: Exception) {
                addSavingsState = addSavingsState.copy(
                    isLoading = false,
                    isSuccess = false,
                    error = e.message ?: "Failed to add deposit"
                )
            }
        }
    }

    private suspend fun syncDepositToFirebase(goalId: String, depositId: String, amount: Double) {
        val goalRef = firestore.collection("goals").document(goalId)
        val goalDoc = goalRef.get().await()

        if (!goalDoc.exists()) {
            throw Exception("Goal not found in Firebase")
        }

        val firebaseGoal = Goal.fromMap(goalDoc.data ?: emptyMap())
        val newAmount = firebaseGoal.currentAmount + amount

        val depositEntity = depositDao.getUnsyncedDeposits().find { it.id == depositId }
        val deposit = depositEntity?.toDeposit() ?: throw Exception("Deposit not found")

        firestore.runTransaction { transaction ->
            transaction.update(
                goalRef,
                mapOf(
                    "currentAmount" to newAmount,
                    "updatedAt" to Timestamp.now()
                )
            )

            transaction.set(
                firestore.collection("deposits").document(depositId),
                deposit.toMap()
            )
        }.await()

        depositDao.markAsSynced(depositId)
    }

    fun syncPendingDeposits() {
        viewModelScope.launch {
            try {
                val unsyncedDeposits = depositDao.getUnsyncedDeposits()

                unsyncedDeposits.forEach { depositEntity ->
                    try {
                        val deposit = depositEntity.toDeposit()

                        firestore.collection("deposits")
                            .document(deposit.id)
                            .set(deposit.toMap())
                            .await()

                        depositDao.markAsSynced(deposit.id)

                        println(" Synced deposit: ${deposit.id}")
                    } catch (e: Exception) {
                        println("❌ Failed to sync deposit: ${depositEntity.id}")
                    }
                }

                val unsyncedGoals = goalDao.getUnsyncedGoals()
                unsyncedGoals.forEach { goalEntity ->
                    try {
                        val goal = goalEntity.toGoal()

                        firestore.collection("goals")
                            .document(goal.id)
                            .update(
                                mapOf(
                                    "currentAmount" to goal.currentAmount,
                                    "updatedAt" to Timestamp.now()
                                )
                            )
                            .await()

                        println(" Synced goal: ${goal.id}")
                    } catch (e: Exception) {
                        println("❌ Failed to sync goal: ${goalEntity.goalId}")
                    }
                }
            } catch (e: Exception) {
                println("❌ Sync failed: ${e.message}")
            }
        }
    }

    private fun validateDeposit(): Boolean {
        val amount = depositAmount.toDoubleOrNull()

        when {
            depositAmount.isBlank() -> {
                addSavingsState = addSavingsState.copy(error = "Please enter an amount")
                return false
            }

            amount == null || amount <= 0 -> {
                addSavingsState =
                    addSavingsState.copy(error = "Please enter a valid amount greater than 0")
                return false
            }

            amount > 1000000 -> {
                addSavingsState = addSavingsState.copy(error = "Amount is too large")
                return false
            }
        }
        return true
    }

    //  FIXED: Only reset success flag, not entire state
    fun resetSuccessState() {
        addSavingsState = addSavingsState.copy(isSuccess = false)
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