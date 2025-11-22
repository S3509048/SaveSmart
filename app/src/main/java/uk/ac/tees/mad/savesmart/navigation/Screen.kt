package uk.ac.tees.mad.savesmart.navigation

sealed class Screen(val route:String){
    object SplashScreen:Screen("splash")
    object SignUpScreen:Screen("sign_up")
    object LoginScreen:Screen("login")
    object MainScreen:Screen("main")
}