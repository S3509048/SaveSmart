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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

// Colors
private val PrimaryGreen = Color(0xFF4CAF50)
private val SecondaryGreen = Color(0xFF81C784)
private val DarkGreen = Color(0xFF2E7D32)
private val TextDark = Color(0xFF212121)
private val TextLight = Color(0xFF757575)
private val ErrorRed = Color(0xFFD32F2F)

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
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Header
                Surface(
                    color = PrimaryGreen,
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
                            color = Color.White
                        )
                        Text(
                            text = "Set your savings target",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.9f)
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
                                tint = PrimaryGreen
                            )
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
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
                                tint = PrimaryGreen
                            )
                        },
//                        prefix = { Text("£") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
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
                                tint = PrimaryGreen
                            )
                        },
//                        prefix = { Text("£") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            focusedLabelColor = PrimaryGreen,
                            cursorColor = PrimaryGreen
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    )

                    Text(
                        text = "Leave blank if starting from zero",
                        fontSize = 11.sp,
                        color = TextLight,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )

                    // Error Message
                    if (errorMessage != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = ErrorRed,
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
                Divider(color = Color.LightGray.copy(alpha = 0.3f))

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
                            containerColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading && goalTitle.isNotEmpty() && targetAmount.isNotEmpty()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
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
    currency: String = "GBP" // Add currency parameter
) {
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
            containerColor = SecondaryGreen.copy(alpha = 0.1f)
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
                color = DarkGreen
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            val progress = if (targetAmount > 0) (startingAmount / targetAmount).toFloat() else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = PrimaryGreen,
                trackColor = SecondaryGreen.copy(alpha = 0.3f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$symbol%.2f".format(startingAmount),
                    fontSize = 13.sp,
                    color = TextDark
                )
                Text(
                    text = "$symbol%.2f".format(targetAmount),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkGreen
                )
            }

//            val remaining = targetAmount - startingAmount
//            if (remaining > 0) {
//                Text(
//                    text = "$symbol%.2f remaining",
//                    fontSize = 11.sp,
//                    color = TextLight,
//                    modifier = Modifier.padding(top = 4.dp)
//                )
//            }
        }
    }
}