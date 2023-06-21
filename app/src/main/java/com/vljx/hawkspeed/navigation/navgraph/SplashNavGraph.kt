package com.vljx.hawkspeed.navigation.navgraph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.vljx.hawkspeed.navigation.AppDestination
import com.vljx.hawkspeed.navigation.navigatePopUpToInclusive
import com.vljx.hawkspeed.ui.screens.splash.SplashScreen

const val SPLASH_GRAPH_ROUTE = "splash"

object SplashDestination: AppDestination("splash_screen")

fun NavGraphBuilder.splashNavGraph(
    navHostController: NavHostController
) {
    navigation(
        startDestination = SplashDestination.route,
        route = SPLASH_GRAPH_ROUTE
    ) {
        composable(
            SplashDestination.route
        ) { navBackStackEntry ->
            SplashScreen(
                onAuthenticationSuccessful = { account ->
                    navHostController.navigatePopUpToInclusive(
                        AuthenticatedGraphRoute.withArgs(account.userUid)
                    )
                },
                onAuthenticationFailed = { resourceError ->
                    navHostController.navigatePopUpToInclusive(
                        ONBOARD_GRAPH_ROUTE
                    )
                }
            )
        }
    }
}