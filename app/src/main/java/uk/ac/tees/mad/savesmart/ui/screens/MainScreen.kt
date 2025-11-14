package uk.ac.tees.mad.savesmart.ui.screens

import android.R.attr.type
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    logout: () -> Unit,
) {
    val bottomNavController = rememberNavController()
    val navBackStackEntry = bottomNavController.currentBackStackEntryAsState()
    val currentRoute =
        navBackStackEntry.value?.destination?.route ?: BottomNavScreen.Dashboard.route


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

        },
//        floatingActionButton = {
//            if (currentRoute == BottomNavScreen.Dashboard.route) {
//                FloatingActionButton(
//                    onClick = {
//                        bottomNavController.navigate(BottomNavScreen.AddSavings.route)
//                    }
//                ) {
//                    Icon(
//                        imageVector = Icons.Default.Add,
//                        contentDescription = "Add Report"
//                    )
//                }
//            }
//        }
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
                ProfileScreen(onLogout =logout)
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
                    goalId = goalId
                )
            }
        }
    }

}