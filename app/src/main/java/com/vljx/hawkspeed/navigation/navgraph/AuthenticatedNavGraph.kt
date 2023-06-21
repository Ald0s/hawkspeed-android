package com.vljx.hawkspeed.navigation.navgraph

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.vljx.hawkspeed.navigation.AppDestination
import com.vljx.hawkspeed.ui.screens.authenticated.authenticatedmain.AuthenticatedMainScreen
import com.vljx.hawkspeed.ui.screens.authenticated.setup.SetupAccountScreen
import com.vljx.hawkspeed.ui.screens.authenticated.verify.VerifyAccountScreen

const val AUTHENTICATED_GRAPH_ROUTE = "authenticated"

object AuthenticatedGraphRoute: AppDestination(AUTHENTICATED_GRAPH_ROUTE) {
    const val userUidArg = "userUid"
    val routeWithArgs = "${route}/{${userUidArg}}"
    val arguments = listOf(
        navArgument(userUidArg) {
            type = NavType.StringType
            nullable = false
        }
    )
}

// The main destination to be found at the start of the authentication nav graph. This screen will begin the process of checking that the incoming account is setup,
// valid and capable of correctly using the HawkSpeed systems; and rectifying issues where present.
object AuthenticationMainDestination: AppDestination("authentication_main_screen")

// This is the setup account screen; to be used if the account requires setup. This requires a User's UID.
object SetupAccountDestination: AppDestination("setup_account_destination") {
    const val userUidArg = "userUid"
    val routeWithArgs = "$route/{${userUidArg}}"
    val arguments = listOf(
        navArgument(userUidArg) {
            type = NavType.StringType
            nullable = false
        }
    )
}

// This is the verify account screen; to be used if the account requires verification. This requires a User's UID.
object VerifyAccountDestination: AppDestination("verify_account_destination") {
    const val userUidArg = "userUid"
    val routeWithArgs = "$route/{${userUidArg}}"
    val arguments = listOf(
        navArgument(userUidArg) {
            type = NavType.StringType
            nullable = false
        }
    )
}

fun NavGraphBuilder.authenticatedNavGraph(
    navHostController: NavHostController
) {
    navigation(
        startDestination = AuthenticationMainDestination.route,
        route = AuthenticatedGraphRoute.routeWithArgs,
        arguments = AuthenticatedGraphRoute.arguments
    ) {
        composable(
            route = AuthenticationMainDestination.route
        ) { navBackStackEntry ->
            AuthenticatedMainScreen(
                onVerificationRequired = { userUid ->
                    // Navigate toward the verify account screen.
                    navHostController.navigate(
                        VerifyAccountDestination.withArgs(userUid)
                    )
                },
                onSetupRequired = { userUid ->
                    // Navigate toward the setup account screen.
                    navHostController.navigate(
                        SetupAccountDestination.withArgs(userUid)
                    )
                },
                onLostAuthentication = {
                    navHostController.navigate(
                        ONBOARD_GRAPH_ROUTE
                    ) {
                        popUpTo(ONBOARD_GRAPH_ROUTE) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = VerifyAccountDestination.routeWithArgs,
            arguments = VerifyAccountDestination.arguments
        ) { navBackStackEntry ->
            VerifyAccountScreen(
                onAccountVerified = { account ->
                    // Account now verified. Navigate back to authenticated main screen to continue setup.
                    navHostController.navigate(
                        AuthenticatedGraphRoute.withArgs(account.userUid)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = SetupAccountDestination.routeWithArgs,
            arguments = SetupAccountDestination.arguments
        ) { navBackStackEntry ->
            SetupAccountScreen(
                onAccountSetup = { account ->
                    // Account is now setup, navigate back to authenticated main screen to continue setup.
                    navHostController.navigate(
                        AuthenticatedGraphRoute.withArgs(account.userUid)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}