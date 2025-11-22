package uk.ac.tees.mad.savesmart.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay


// Colors
private val PrimaryGreen = Color(0xFF4CAF50)
private val SecondaryGreen = Color(0xFF81C784)
private val DarkGreen = Color(0xFF2E7D32)
private val GoldCoin = Color(0xFFFFD700)
private val BackgroundLight = Color(0xFFF5F5F5)
private val TextDark = Color(0xFF212121)
private val TextLight = Color(0xFF757575)

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier,
    navigateToMain: () -> Unit,
    navigateToLogin: () -> Unit,
) {


    // List of 3 motivational quotes
    val quotes = listOf(
        "Small savings today, big dreams tomorrow.",
        "Every penny saved is a step toward your goal.",
        "Financial freedom starts with a single save."
    )
    // Randomly select a quote
    val selectedQuote = remember { quotes.random() }

    // Navigation effect
    LaunchedEffect(Unit) {
        delay(3000) // 3 seconds delay

        // Check Firebase authentication
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser != null) {
            navigateToMain()
            // User is logged in, navigate to Dashboard
//            navController.navigate(Screen.DashboardScreen.route) {
//                popUpTo(Screen.SplashScreen.route) { inclusive = true }
//            }
        } else {
            // User is not logged in, navigate to Login
            navigateToLogin()
//            navController.navigate(Screen.LoginScreen.route) {
//                popUpTo(Screen.SplashScreen.route) { inclusive = true }
//            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
//            .background(
//                Brush.verticalGradient(
//                    colors = listOf(BackgroundLight, Color.White)
//                )
//            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(horizontal = 32.dp)
        ) {
            // Animation Section
            CoinDropAnimation()

            // App Name
            Text(
                text = "SaveSmart",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGreen
            )

            Text(
                text = "Build Your Financial Future",
                fontSize = 14.sp,
                color = TextLight
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Quote Section
            QuoteCard(quote = selectedQuote)
        }
    }
}

@Composable
fun CoinDropAnimation() {
    var isAnimating by remember { mutableStateOf(true) }

    // Coin drop animation
    val coinOffsetY by animateFloatAsState(
        targetValue = if (isAnimating) 0f else 120f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = FastOutSlowInEasing
        ),
        label = "coinDrop"
    )

    // Coin rotation
    val coinRotation by animateFloatAsState(
        targetValue = if (isAnimating) 0f else 720f,
        animationSpec = tween(
            durationMillis = 1000,
            easing = LinearEasing
        ),
        label = "coinRotation"
    )

    // Piggy bank scale (bounce effect when coin lands)
    val piggyScale by animateFloatAsState(
        targetValue = if (isAnimating) 1f else 1.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "piggyScale"
    )

    // Start animation
    LaunchedEffect(Unit) {
        delay(300)
        isAnimating = false
    }

    Box(
        modifier = Modifier
            .size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Piggy Bank
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(piggyScale)
                .offset(y = 40.dp)
                .background(PrimaryGreen, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🐷",
                fontSize = 64.sp
            )
        }

        // Coin
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(y = (-60 + coinOffsetY).dp)
                .background(GoldCoin, CircleShape)
                .alpha(0.9f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "£",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF8B6914)
            )
        }
    }
}

@Composable
fun QuoteCard(quote: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        SecondaryGreen.copy(alpha = 0.15f),
                        PrimaryGreen.copy(alpha = 0.08f)
                    )
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(24.dp)
    ) {
        Row {
            // Opening Quote
            Text(
                text = "\"",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen.copy(alpha = 0.6f),
                modifier = Modifier.offset(y = (-4).dp)
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = quote,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.primary,
                    lineHeight = 24.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Closing Quote
            Text(
                text = "\"",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryGreen.copy(alpha = 0.6f),
                modifier = Modifier
                    .align(Alignment.Bottom)
                    .offset(y = 4.dp)
            )
        }
    }
}