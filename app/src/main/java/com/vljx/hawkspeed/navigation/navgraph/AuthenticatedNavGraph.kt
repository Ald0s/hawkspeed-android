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
import com.vljx.hawkspeed.ui.screens.authenticated.setuptrack.SetupTrackDetailScreen
import com.vljx.hawkspeed.ui.screens.authenticated.verify.VerifyAccountScreen
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapScreen

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

/**
 * The main destination to be found at the start of the authentication nav graph. This screen will begin the process of checking that the incoming account is setup,
 * valid and capable of correctly using the HawkSpeed systems; and rectifying issues where present.
 */
object AuthenticationMainDestination: AppDestination("authentication_main_screen")

/**
 * This is the setup account screen; to be used if the account requires setup. This requires a User's UID.
 */
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

/**
 * This is the verify account screen; to be used if the account requires verification. This requires a User's UID.
 */
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

/**
 * The World Map screen.
 */
object WorldMapDestination: AppDestination("world_map_destination")

/**
 * Viewing a User's detail.
 */
object UserDetailDestination: AppDestination("user_detail_destination") {
    const val userUidArg = "userUid"
    val routeWithArgs = "$route/{${userUidArg}}"
    val arguments = listOf(
        navArgument(userUidArg) {
            type = NavType.StringType
            nullable = false
        }
    )
}

/**
 * Viewing a Track's detail.
 */
object TrackDetailDestination: AppDestination("track_detail_destination") {
    const val trackUidArg = "trackUid"
    const val viewLeaderboard = "shouldViewLeaderboard"
    const val wantsToComment = "wantsToComment"
    val routeWithArgs = "$route/{${trackUidArg}}/{${viewLeaderboard}}/{${wantsToComment}}"

    val arguments = listOf(
        navArgument(trackUidArg) {
            type = NavType.StringType
            nullable = false
        },
        navArgument(viewLeaderboard) {
            type = NavType.BoolType
            nullable = false
        },
        navArgument(wantsToComment) {
            type = NavType.BoolType
            nullable = false
        }
    )
}

/**
 * Finalising the setup of a track, by entering its detail such as name and description.
 */
object SetupTrackDetailDestination: AppDestination("setup_track_detail_destination") {
    const val trackDraftIdArg = "trackDraftId"
    val routeWithArgs = "$route/{${trackDraftIdArg}}"

    val arguments = listOf(
        navArgument(trackDraftIdArg) {
            type = NavType.LongType
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
                onAuthenticatedAndSetup = {
                    // When we've been authenticated and confirmed setup, navigate from authenticated main screen to the world map.
                    navHostController.navigate(
                        WorldMapDestination.route
                    )
                },
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

        composable(
            route = WorldMapDestination.route
        ) { navBackStackEntry ->
            // We'll pass the nav host controller down to the world map screen, as this screen will from now on become the
            // central point of access.
            WorldMapScreen(
                onViewCurrentProfileClicked = { currentUserUid ->
                    // Navigate to User detail, with the current User's UID.
                    navHostController.navigate(
                        UserDetailDestination.withArgs(currentUserUid)
                    )
                },
                onViewUserDetail = { user ->
                    // Navigate to User detail, with the given user's UID.
                    navHostController.navigate(
                        UserDetailDestination.withArgs(user.userUid)
                    )
                },
                onViewTrackDetail = { track ->
                    // Navigate to track detail, with the given track's UID, and false for both viewing leaderboard and wanting to comment.
                    navHostController.navigate(
                        TrackDetailDestination.withArgs(track.trackUid, false, false)
                    )
                },
                onViewTrackComments = { track, wantsToComment ->
                    // Navigate to track detail, with the given track's UID, false for viewing leaderboard but wantsToComment for wanting to comment.
                    navHostController.navigate(
                        TrackDetailDestination.withArgs(track.trackUid, false, wantsToComment)
                    )
                },
                onViewTrackLeaderboard = { track ->
                    // Navigate to track detail, with the given track's UID, true for viewing leaderboard and false for wants to comment.
                    navHostController.navigate(
                        TrackDetailDestination.withArgs(track.trackUid, true, false)
                    )
                },
                onSetupTrackDetailsClicked = { trackDraftId ->
                    // Navigate to setup track detail, with the given Id.
                    navHostController.navigate(
                        SetupTrackDetailDestination.withArgs(trackDraftId)
                    )
                }
            )
        }

        composable(
            route = UserDetailDestination.routeWithArgs,
            arguments = UserDetailDestination.arguments
        ) { navBackStackEntry ->
            // TODO: show the user detail screen.
        }

        composable(
            route = TrackDetailDestination.routeWithArgs,
            arguments = TrackDetailDestination.arguments
        ) { navBackStackEntry ->
            // TODO: show the track detail screen.
        }

        composable(
            route = SetupTrackDetailDestination.routeWithArgs,
            arguments = SetupTrackDetailDestination.arguments
        ) { navBackStackEntry ->
            // Show the setup track detail screen.
            SetupTrackDetailScreen(
                onTrackCreated = { trackWithPath ->
                    // TODO: when we have successfully created a path, we now want to navigate all the way back to world map.
                    throw NotImplementedError("onTrackCreated not yet properly implemented.")
                }
            )
        }
    }
}