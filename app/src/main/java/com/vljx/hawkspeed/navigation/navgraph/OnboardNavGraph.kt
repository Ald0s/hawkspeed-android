package com.vljx.hawkspeed.navigation.navgraph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.vljx.hawkspeed.navigation.AppDestination
import com.vljx.hawkspeed.navigation.navigatePopUpToInclusive
import com.vljx.hawkspeed.ui.screens.onboard.login.LoginScreen
import com.vljx.hawkspeed.ui.screens.onboard.register.RegisterScreen

const val ONBOARD_GRAPH_ROUTE = "onboard"

object LoginDestination: AppDestination("login_screen")
object RegisterDestination: AppDestination("register_screen")

fun NavGraphBuilder.onboardNavGraph(
    navHostController: NavHostController
) {
    navigation(
        startDestination = LoginDestination.route,
        route = ONBOARD_GRAPH_ROUTE
    ) {
        composable(
            route = LoginDestination.route
        ) { navBackStackEntry ->
            LoginScreen(
                onLoginSuccessful = { account ->
                    navHostController.navigatePopUpToInclusive(
                        AuthenticatedGraphRoute.withArgs(account.userUid)
                    )
                },
                onRegisterLocalAccountClicked = {
                    navHostController.navigate(
                        RegisterDestination.route
                    )
                }
            )
        }

        composable(
            route = RegisterDestination.route
        ) { navBackStackEntry ->
            RegisterScreen(
                onRegistered = { registration ->
                    navHostController.navigate(
                        RegisterDestination.route
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}