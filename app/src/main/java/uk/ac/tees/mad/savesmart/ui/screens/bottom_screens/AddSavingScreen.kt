package uk.ac.tees.mad.savesmart.ui.screens.bottom_screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import uk.ac.tees.mad.savesmart.viewmodel.SavingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingScreen(
    navController: NavController,
    goalId: String? = null,
    viewModel: SavingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val state = viewModel.addSavingsState

    // Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    // Load goal when screen opens
    LaunchedEffect(goalId) {
        goalId?.let {
            viewModel.loadGoal(it)
        }
    }

    // Handle success
    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Deposit added successfully!", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
            viewModel.resetSuccessState()
        }
    }

    // Get currency symbol for display
    val currencySymbol = when (viewModel.currentCurrency) {
        "GBP" -> "£"
        "USD" -> "$"
        "EUR" -> "€"
        "INR" -> "₹"
        else -> viewModel.currentCurrency
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        backgroundColor,
                        surfaceColor
                    )
                )
            )
    ) {
        if (state.isLoading && state.currentGoal == null) {
            // Loading goal
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = primaryColor
            )
        } else if (state.currentGoal != null) {
            // Show form
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Goal Info Card
                Text(
                    text = "Add Savings Now",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    color = onSurfaceColor,  // Theme-aware
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                GoalInfoCard(
                    goal = state.currentGoal,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Amount Input
                OutlinedTextField(
                    value = viewModel.depositAmount,
                    onValueChange = { viewModel.updateDepositAmount(it) },
                    label = { Text("Deposit Amount") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Money,
                            contentDescription = "Amount",
                            tint = primaryColor
                        )
                    },
                    prefix = { Text(currencySymbol) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Note Input (Optional)
                OutlinedTextField(
                    value = viewModel.depositNote,
                    onValueChange = { viewModel.updateDepositNote(it) },
                    label = { Text("Note (Optional)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Note,
                            contentDescription = "Note",
                            tint = primaryColor
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                        }
                    ),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primaryColor,
                        focusedLabelColor = primaryColor,
                        cursorColor = primaryColor
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                    //  Currency Converter Section
                if (viewModel.depositAmount.isNotEmpty()) {
                    CurrencyConverterCard(
                        amount = viewModel.depositAmount,
                        fromCurrency = viewModel.currentCurrency,
                        conversionState = viewModel.conversionState,
                        onCurrencySelected = { currency ->
                            viewModel.updateTargetCurrency(currency)
                        },
                        viewModel = viewModel
                    )
                }
                // Error Message
                if (state.error != null) {
                    Text(
                        text = state.error,
                        color = errorColor,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Preview Card
                if (viewModel.depositAmount.isNotEmpty()) {
                    val amount = viewModel.depositAmount.toDoubleOrNull() ?: 0.0
                    val newTotal = state.currentGoal.currentAmount + amount

                    PreviewCard(
                        currentAmount = state.currentGoal.currentAmount,
                        depositAmount = amount,
                        newTotal = newTotal,
                        targetAmount = state.currentGoal.targetAmount,
                        currency = viewModel.currentCurrency,
                        viewModel = viewModel
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Add Deposit Button
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        goalId?.let {
                            viewModel.addDeposit(it)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    ),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !state.isLoading && viewModel.depositAmount.isNotEmpty()
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            color = onPrimaryColor,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = "Add Deposit",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Cancel button
                OutlinedButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.dp,
                        primaryColor
                    )
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }
        } else if (state.error != null) {
            // Error loading goal
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = state.error,
                    color = errorColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryColor
                    )
                ) {
                    Text("Go Back")
                }
            }
        }
    }
}

@Composable
private fun GoalInfoCard(
    goal: uk.ac.tees.mad.savesmart.data.model.Goal,
    viewModel: SavingsViewModel
) {
    // ✅ Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = goal.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { goal.getProgressPercentage() / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = primaryColor,
                trackColor = primaryColor.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Current / Target
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Current",
                        fontSize = 12.sp,
                        color = onSurfaceColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = viewModel.formatCurrency(goal.currentAmount, goal.currency),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = onSurfaceColor
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Target",
                        fontSize = 12.sp,
                        color = onSurfaceColor.copy(alpha = 0.6f)
                    )
                    Text(
                        text = viewModel.formatCurrency(goal.targetAmount, goal.currency),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress percentage
            Text(
                text = "${goal.getProgressPercentage().toInt()}% Complete",
                fontSize = 14.sp,
                color = primaryColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun PreviewCard(
    currentAmount: Double,
    depositAmount: Double,
    newTotal: Double,
    targetAmount: Double,
    currency: String,
    viewModel: SavingsViewModel
) {
    // ✅ Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Preview",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current Amount:",
                    fontSize = 13.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
                Text(
                    text = viewModel.formatCurrency(currentAmount, currency),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = onSurfaceColor
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Deposit:",
                    fontSize = 13.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f)
                )
                Text(
                    text = "+ ${viewModel.formatCurrency(depositAmount, currency)}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = primaryColor
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = onSurfaceColor.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "New Total:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
                Text(
                    text = viewModel.formatCurrency(newTotal, currency),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            val remaining = (targetAmount - newTotal).coerceAtLeast(0.0)
            Text(
                text = if (remaining > 0) {
                    "Remaining: ${viewModel.formatCurrency(remaining, currency)}"
                } else {
                    "Goal Completed!"
                },
                fontSize = 12.sp,
                color = if (remaining > 0) {
                    onSurfaceColor.copy(alpha = 0.6f)
                } else {
                    primaryColor
                },
                fontWeight = if (remaining > 0) FontWeight.Normal else FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencyConverterCard(
    amount: String,
    fromCurrency: String,
    conversionState: uk.ac.tees.mad.savesmart.data.model.ConversionState,
    onCurrencySelected: (String) -> Unit,
    viewModel: SavingsViewModel
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Available currencies
    val currencies = listOf("USD", "EUR", "INR", "GBP")
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = surfaceColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Currency Converter",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor
                )

                // Currency Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it },
                ) {
                    OutlinedTextField(
                        value = conversionState.targetCurrency,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To", fontSize = 12.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .menuAnchor()
                            .width(120.dp),
//                            .height(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor
                        ),
                        textStyle = MaterialTheme.typography.bodySmall
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        currencies.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(currency) },
                                onClick = {
                                    onCurrencySelected(currency)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(12.dp))

            // Conversion Display
            if (conversionState.isLoading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = primaryColor,
                        strokeWidth = 2.dp
                    )
                }
            } else if (conversionState.convertedAmount != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Amount",
                            fontSize = 12.sp,
                            color = onSurfaceColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = viewModel.formatCurrency(
                                amount.toDoubleOrNull() ?: 0.0,
                                fromCurrency
                            ),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = onSurfaceColor
                        )
                    }

                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = primaryColor,
                        modifier = Modifier.size(24.dp)
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Converts to",
                            fontSize = 12.sp,
                            color = onSurfaceColor.copy(alpha = 0.6f)
                        )
                        Text(
                            text = viewModel.formatCurrency(
                                conversionState.convertedAmount,
                                conversionState.targetCurrency
                            ),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor
                        )
                    }
                }
            } else if (conversionState.error != null) {
                Text(
                    text = conversionState.error,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            } else {
                Text(
                    text = "Enter an amount to see conversion",
                    fontSize = 12.sp,
                    color = onSurfaceColor.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}