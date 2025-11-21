package uk.ac.tees.mad.savesmart.navigation.bottom_navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavScreen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavScreen(
        route = "dashboard",
        title = "Dashboard",
        icon = Icons.Default.Home
    )


    object Motivation : BottomNavScreen(
        route = "motivation",
        title = "Motivation",
        icon = Icons.Default.FormatQuote
    )

    object Profile : BottomNavScreen(
        route = "profile",
        title = "Profile",
        icon = Icons.Default.Person
    )
    object AddSavings : BottomNavScreen(
        route = "add_savings",
        title = "Add Savings",
        icon = Icons.Default.AddCircle
    )
}

val bottomNavScreens = listOf(
    BottomNavScreen.Dashboard,
    BottomNavScreen.Motivation,
    BottomNavScreen.Profile,
)