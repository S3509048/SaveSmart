package uk.ac.tees.mad.savesmart.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.savesmart.ui.screens.LoginScreen
import uk.ac.tees.mad.savesmart.ui.screens.MainScreen
import uk.ac.tees.mad.savesmart.ui.screens.SignUpScreen
import uk.ac.tees.mad.savesmart.ui.screens.SplashScreen
import uk.ac.tees.mad.savesmart.viewmodel.AuthViewModel

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {


    val navController = rememberNavController()

    val viewModel = hiltViewModel<AuthViewModel>()

    NavHost(
        navController = navController,
        startDestination = Screen.SplashScreen.route
    ) {

        composable(Screen.SplashScreen.route) {
            SplashScreen(
                navigateToMain = {
                    navController.navigate(Screen.MainScreen.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                },
                navigateToLogin = {
                    navController.navigate(Screen.LoginScreen.route) {
                        popUpTo(0) {
                            inclusive = true
                        }
                    }
                }
            )
        }

        composable(Screen.LoginScreen.route) {
            LoginScreen(navController, viewModel = viewModel)
        }

        composable(Screen.SignUpScreen.route) {
            SignUpScreen(navController = navController, viewModel = viewModel)
        }

        composable(Screen.MainScreen.route) {
            MainScreen(
                logout = {
                    viewModel.logout {
                        // call back
                        navController.navigate(Screen.LoginScreen.route) {
                            popUpTo(0) {
                                inclusive = true
                            }
                        }
                    }
                }
            )
        }
    }
}