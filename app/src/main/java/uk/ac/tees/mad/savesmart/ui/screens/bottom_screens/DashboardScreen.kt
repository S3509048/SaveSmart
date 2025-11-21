package uk.ac.tees.mad.savesmart.ui.screens.bottom_screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import uk.ac.tees.mad.savesmart.data.model.Goal
import uk.ac.tees.mad.savesmart.ui.screens.components.profile_screen.CreateGoalDialog
import uk.ac.tees.mad.savesmart.viewmodel.GoalViewModel

// Colors
private val PrimaryGreen = Color(0xFF4CAF50)
private val SecondaryGreen = Color(0xFF81C784)
private val DarkGreen = Color(0xFF2E7D32)
private val BackgroundLight = Color(0xFFF5F5F5)
private val TextDark = Color(0xFF212121)
private val TextLight = Color(0xFF757575)

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    popBack: () -> Unit,
    onAddSavingsClick: (String) -> Unit = {},
    viewModel: GoalViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.dashboardState.collectAsState()
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    val createGoalState = viewModel.createGoalState

    val currentCurrency by viewModel.currentCurrency.collectAsState()

    // Load goals when screen opens
    LaunchedEffect(Unit) {
        viewModel.refreshGoals()
    }

    // Handle goal creation success
    LaunchedEffect(createGoalState.isSuccess) {
        if (createGoalState.isSuccess) {
            showCreateGoalDialog = false
            Toast.makeText(context, "Goal created successfully!", Toast.LENGTH_SHORT).show()
//            viewModel.loadGoals() // Reload goals after creation
            viewModel.resetCreateGoalSuccess()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(BackgroundLight, Color.White)
//                )
//            )
    ) {
        when {
            state.isLoading -> {
                // Loading state
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryGreen
                )
            }

            state.error != null -> {
                // Error state
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "😕",
                        fontSize = 48.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.error ?: "Something went wrong",
                        color = TextDark,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.loadGoals() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }

            state.goals.isEmpty() -> {
                // Empty state - No goals yet
                EmptyState(onCreateGoalClick = { showCreateGoalDialog = true })
            }

            else -> {
                // Show goals
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header
                    item {
                        Column {
                            Text(
                                text = "Your Goals",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = DarkGreen
                            )
                            Text(
                                text = "${state.goals.size} active goal${if (state.goals.size != 1) "s" else ""}",
                                fontSize = 14.sp,
                                color = TextLight
                            )
                        }
                    }

                    // Goals list
                    items(state.goals) { goal ->
                        GoalCard(
                            goal = goal,
                            onAddSavingsClick = { onAddSavingsClick(goal.id) },
                            viewModel = viewModel
                        )
                    }

                    // Add new goal button
                    item {
                        OutlinedButton(
                            onClick = { showCreateGoalDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = PrimaryGreen
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create New Goal")
                        }
                    }

                    // Bottom spacing
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    // Create Goal Dialog
    if (showCreateGoalDialog) {
        CreateGoalDialog(
            onDismiss = {
                if (!createGoalState.isLoading) {
                    showCreateGoalDialog = false
//                    viewModel.resetCreateForm()
                }
            },
            goalTitle = viewModel.goalTitle,
            onGoalTitleChange = { viewModel.updateGoalTitle(it) },
            targetAmount = viewModel.targetAmount,
            onTargetAmountChange = { viewModel.updateTargetAmount(it) },
            startingAmount = viewModel.startingAmount,
            onStartingAmountChange = { viewModel.updateStartingAmount(it) },
            errorMessage = createGoalState.error,
            isLoading = createGoalState.isLoading,
            onCreateClick = {
                viewModel.createGoal()
            },
            currentCurrency = currentCurrency
        )
    }
}

@Composable
private fun EmptyState(
    onCreateGoalClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(PrimaryGreen.copy(alpha = 0.1f), RoundedCornerShape(60.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Savings,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(64.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "No Goals Yet!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = DarkGreen
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Start your savings journey by\ncreating your first goal",
            fontSize = 15.sp,
            color = TextLight,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onCreateGoalClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = PrimaryGreen
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Create Your First Goal",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Motivational text
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF1F8F4)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "💡 Tip: Start with a small, achievable goal to build momentum!",
                fontSize = 13.sp,
                color = TextDark,
                modifier = Modifier.padding(16.dp)
            )
        }
    }
}

@Composable
private fun GoalCard(
    goal: Goal,
    onAddSavingsClick: () -> Unit,
    viewModel: GoalViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Goal title and amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${goal.getProgressPercentage().toInt()}% Complete",
                        fontSize = 12.sp,
                        color = PrimaryGreen,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Completion badge
                if (goal.isCompleted()) {
                    Surface(
                        color = PrimaryGreen,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "✓ Done",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { goal.getProgressPercentage() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = PrimaryGreen,
                trackColor = SecondaryGreen.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Amount display
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Saved",
                        fontSize = 11.sp,
                        color = TextLight
                    )
                    Text(
                        text = viewModel.formatCurrency(goal.currentAmount, goal.currency),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        fontSize = 11.sp,
                        color = TextLight
                    )
                    Text(
                        text = viewModel.formatCurrency(goal.targetAmount, goal.currency),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Add savings button
            Button(
                onClick = onAddSavingsClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !goal.isCompleted()
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (goal.isCompleted()) "Goal Completed!" else "Add Savings",
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}