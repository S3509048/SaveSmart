package uk.ac.tees.mad.savesmart.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import uk.ac.tees.mad.savesmart.navigation.bottom_navigation.BottomNavScreen
import uk.ac.tees.mad.savesmart.navigation.bottom_navigation.bottomNavScreens
import uk.ac.tees.mad.savesmart.ui.screens.bottom_screens.AddSavingScreen
import uk.ac.tees.mad.savesmart.ui.screens.bottom_screens.DashboardScreen
import uk.ac.tees.mad.savesmart.ui.screens.bottom_screens.MotivationScreen
import uk.ac.tees.mad.savesmart.ui.screens.bottom_screens.ProfileScreen
import uk.ac.tees.mad.savesmart.viewmodel.GoalViewModel
import uk.ac.tees.mad.savesmart.viewmodel.SavingsViewModel
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.content.Context

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    logout: () -> Unit,
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState()
    val currentRoute =
        navBackStackEntry.value?.destination?.route ?: BottomNavScreen.Dashboard.route

    val context = LocalContext.current
    val goalViewModel = hiltViewModel<GoalViewModel>()
    val savingViewModel = hiltViewModel<SavingsViewModel>()

    // ✅ Auto-sync when app resumes with internet
    val isOnline = remember { mutableStateOf(isNetworkAvailable(context)) }

//    LaunchedEffect(Unit) {
//        // Trigger sync when app starts if online
//        if (isOnline.value) {
//            savingViewModel.syncPendingDeposits()
//            goalViewModel.loadGoals()  // Refresh from Firebase
//        }
//    }

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavScreens.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            bottomNavController.navigate(screen.route) {
                                popUpTo(bottomNavController.graph.startDestinationId) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        label = {
                            Text(screen.title)
                        },
                        icon = {
                            Icon(screen.icon, contentDescription = screen.title)
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = BottomNavScreen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(
                route = BottomNavScreen.Dashboard.route
            ) {
                DashboardScreen(
                    onAddSavingsClick = { goalId ->
                        bottomNavController.navigate("add_savings/$goalId")
                    },
                    viewModel = goalViewModel,
                    popBack = {
                        bottomNavController.popBackStack()
                    }
                )
            }
            composable(
                route = BottomNavScreen.Motivation.route
            ) {
                MotivationScreen()
            }
            composable(
                route = BottomNavScreen.Profile.route
            ) {
                ProfileScreen(
                    onLogout = logout,
                    viewModel = goalViewModel,
                    popBack = {
                        bottomNavController.popBackStack()
                    }
                )
            }

            // Add Savings Screen with argument
            composable(
                route = "add_savings/{goalId}",
                arguments = listOf(
                    navArgument("goalId") {
                        type = NavType.StringType
                    }
                )
            ) { backStackEntry ->
                val goalId = backStackEntry.arguments?.getString("goalId")
                AddSavingScreen(
                    navController = bottomNavController,
                    goalId = goalId,
                    viewModel = savingViewModel
                )
            }
        }
    }
}

//  Helper function to check network
private fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

    return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
}