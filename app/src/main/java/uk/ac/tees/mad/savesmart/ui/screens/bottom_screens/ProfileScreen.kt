package uk.ac.tees.mad.savesmart.ui.screens.bottom_screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.savesmart.ui.screens.components.profile_screen.CreateGoalDialog
import uk.ac.tees.mad.savesmart.viewmodel.GoalViewModel

// Colors
private val PrimaryGreen = Color(0xFF4CAF50)
private val SecondaryGreen = Color(0xFF81C784)
private val DarkGreen = Color(0xFF2E7D32)
private val BackgroundLight = Color(0xFFF5F5F5)
private val TextDark = Color(0xFF212121)
private val TextLight = Color(0xFF757575)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    viewModel: GoalViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentUser = FirebaseAuth.getInstance().currentUser
    var showCreateGoalDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    val createGoalState = viewModel.createGoalState


    val dashboardState by viewModel.dashboardState.collectAsState()
    // Handle goal creation success
    LaunchedEffect(createGoalState.isSuccess) {
        if (createGoalState.isSuccess) {
            showCreateGoalDialog = false
            Toast.makeText(context, "Goal created successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BackgroundLight, Color.White)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Profile Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(PrimaryGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // User Name
                    Text(
                        text = currentUser?.displayName ?: "User",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGreen
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Email
                    Text(
                        text = currentUser?.email ?: "",
                        fontSize = 14.sp,
                        color = TextLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Goals Section Header
            Text(
                text = "Goals Management",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Create Goal Button
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Button(
                    onClick = { showCreateGoalDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Create New Goal",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Settings Section
            Text(
                text = "Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Settings Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
//                    SettingItem(
//                        icon = Icons.Default.Notifications,
//                        title = "Notifications",
//                        subtitle = "Manage reminder settings",
//                        onClick = {
//                            Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
//                        }
//                    )

//                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    SettingItem(
                        icon = Icons.Default.AttachMoney,
                        title = "Update Currency",
                        subtitle = "Change currency for all goals",
                        onClick = {
                            viewModel.loadGoals() // Load goals first
                            showCurrencyDialog = true
                        }
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    SettingItem(
                        icon = Icons.Default.DarkMode,
                        title = "Theme",
                        subtitle = "Light mode",
                        onClick = {
                            Toast.makeText(context, "Coming soon!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Account Section
            Text(
                text = "Account",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen,
                modifier = Modifier.padding(horizontal = 4.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Account Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
//                    SettingItem(
//                        icon = Icons.Default.Info,
//                        title = "About",
//                        subtitle = "SaveSmart v1.0.0",
//                        onClick = {
//                            Toast.makeText(context, "SaveSmart - Build your financial future", Toast.LENGTH_SHORT).show()
//                        }
//                    )
//
//                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    SettingItem(
                        icon = Icons.Default.ExitToApp,
                        title = "Logout",
                        subtitle = "Sign out of your account",
                        onClick = onLogout,
                        isDestructive = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Create Goal Dialog
    if (showCreateGoalDialog) {
        CreateGoalDialog(
            onDismiss = {
                if (!createGoalState.isLoading) {
                    showCreateGoalDialog = false
                    viewModel.resetCreateForm()
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
                viewModel.createGoal {
                    // Success handled in LaunchedEffect
                }
            },
            currentCurrency = viewModel.currentCurrency
        )
    }
    // Currency Update Dialog
    if (showCurrencyDialog) {
        val currentCurrency = dashboardState.goals.firstOrNull()?.currency ?: "GBP"
        CurrencyUpdateDialog(
            goals = dashboardState.goals,
            currentCurrency = currentCurrency, // Pass current currency
            onDismiss = { showCurrencyDialog = false },
            onCurrencySelected = { currency ->
                viewModel.updateAllGoalsCurrency(currency) {
                    showCurrencyDialog = false
                    Toast.makeText(
                        context,
                        "Currency updated to $currency successfully!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            isUpdating = dashboardState.isLoading
        )
    }
}


@Composable
private fun CurrencyUpdateDialog(
    goals: List<uk.ac.tees.mad.savesmart.data.model.Goal>,
    currentCurrency: String, // Add this parameter
    onDismiss: () -> Unit,
    onCurrencySelected: (String) -> Unit,
    isUpdating: Boolean
) {
    val currencies = listOf(
        "GBP" to "British Pound (£)",
        "USD" to "US Dollar ($)",
        "EUR" to "Euro (€)",
        "INR" to "Indian Rupee (₹)"
    )

    var selectedCurrency by remember { mutableStateOf(currentCurrency) }

    AlertDialog(
        onDismissRequest = { if (!isUpdating) onDismiss() },
        title = { Text("Update Currency") },
        text = {
            Column {
                if (goals.isEmpty()) {
                    Text(
                        "No goals found. Create a goal first.",
                        color = TextLight
                    )
                } else {
                    Text(
                        "This will update currency for all ${goals.size} goal${if (goals.size != 1) "s" else ""}.",
                        fontSize = 14.sp,
                        color = TextDark
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    currencies.forEach { (code, name) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = selectedCurrency == code,
                                    onClick = { selectedCurrency = code },
                                    enabled = !isUpdating
                                )
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedCurrency == code,
                                onClick = { selectedCurrency = code },
                                enabled = !isUpdating
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (goals.isNotEmpty()) {
                Button(
                    onClick = { onCurrencySelected(selectedCurrency) },
                    enabled = !isUpdating
                ) {
                    if (isUpdating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Update")
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isUpdating
            ) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun SettingItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color.Red else PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) Color.Red else TextDark
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextLight
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextLight,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}