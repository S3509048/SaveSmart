package uk.ac.tees.mad.savesmart.ui.screens.components.profile_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGoalDialog(
    onDismiss: () -> Unit,
    goalTitle: String,
    onGoalTitleChange: (String) -> Unit,
    targetAmount: String,
    onTargetAmountChange: (String) -> Unit,
    startingAmount: String,
    onStartingAmountChange: (String) -> Unit,
    errorMessage: String?,
    isLoading: Boolean,
    onCreateClick: () -> Unit,
    currentCurrency: String = "GBP"
) {
    // ✅ Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val errorColor = MaterialTheme.colorScheme.error

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = surfaceColor  // ✅ Theme-aware
            )
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                Surface(
                    color = primaryColor,  // ✅ Theme-aware
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Create New Goal",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = onPrimaryColor  // ✅ Theme-aware
                        )
                        Text(
                            text = "Set your savings target",
                            fontSize = 14.sp,
                            color = onPrimaryColor.copy(alpha = 0.9f)  // ✅ Theme-aware
                        )
                    }
                }

                // Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    // Goal Name
                    OutlinedTextField(
                        value = goalTitle,
                        onValueChange = onGoalTitleChange,
                        label = { Text("Goal Name") },
                        placeholder = { Text("e.g., Emergency Fund") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Label,
                                contentDescription = null,
                                tint = primaryColor  // ✅ Theme-aware
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,  // ✅ Theme-aware
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Target Amount
                    OutlinedTextField(
                        value = targetAmount,
                        onValueChange = onTargetAmountChange,
                        label = { Text("Target Amount") },
                        placeholder = { Text("e.g., 1000") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = primaryColor  // ✅ Theme-aware
                            )
                        },
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
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Starting Amount
                    OutlinedTextField(
                        value = startingAmount,
                        onValueChange = onStartingAmountChange,
                        label = { Text("Starting Amount (Optional)") },
                        placeholder = { Text("Already saved?") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = null,
                                tint = primaryColor  // ✅ Theme-aware
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = primaryColor,
                            focusedLabelColor = primaryColor,
                            cursorColor = primaryColor
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )

                    Text(
                        text = "Leave blank if starting from zero",
                        fontSize = 11.sp,
                        color = onSurfaceColor.copy(alpha = 0.6f),  // ✅ Theme-aware
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )

                    // Error Message
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = errorColor,  // ✅ Theme-aware
                            fontSize = 13.sp,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Start
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Preview
                    if (targetAmount.isNotEmpty()) {
                        val target = targetAmount.toDoubleOrNull() ?: 0.0
                        val starting = startingAmount.toDoubleOrNull() ?: 0.0

                        if (target > 0) {
                            GoalPreviewCard(
                                title = goalTitle.ifEmpty { "Your Goal" },
                                targetAmount = target,
                                startingAmount = starting,
                                currency = currentCurrency
                            )
                        }
                    }
                }

                // Action Buttons
                HorizontalDivider(color = onSurfaceColor.copy(alpha = 0.1f))  // ✅ Theme-aware

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        Text("Cancel")
                    }

                    // Create Button
                    Button(
                        onClick = onCreateClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor  // ✅ Theme-aware
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && goalTitle.isNotEmpty() && targetAmount.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = onPrimaryColor,  // ✅ Theme-aware
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Create",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalPreviewCard(
    title: String,
    targetAmount: Double,
    startingAmount: Double,
    currency: String = "GBP"
) {
    // ✅ Get theme colors
    val primaryColor = MaterialTheme.colorScheme.primary
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // Get currency symbol
    val symbol = when (currency) {
        "GBP" -> "£"
        "USD" -> "$"
        "EUR" -> "€"
        "INR" -> "₹"
        else -> currency
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.1f)  // ✅ Theme-aware
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Preview",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor  // ✅ Theme-aware
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = onSurfaceColor  // ✅ Theme-aware
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            val progress = if (targetAmount > 0) (startingAmount / targetAmount).toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = primaryColor,  // ✅ Theme-aware
                trackColor = primaryColor.copy(alpha = 0.3f),  // ✅ Theme-aware
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$symbol%.2f".format(startingAmount),
                    fontSize = 13.sp,
                    color = onSurfaceColor  // ✅ Theme-aware
                )
                Text(
                    text = "$symbol%.2f".format(targetAmount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor  // ✅ Theme-aware
                )
            }
        }
    }
}