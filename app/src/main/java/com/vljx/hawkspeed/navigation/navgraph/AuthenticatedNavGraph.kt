package com.vljx.hawkspeed.navigation.navgraph

import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.vljx.hawkspeed.navigation.AppDestination
import com.vljx.hawkspeed.ui.screens.authenticated.authenticatedmain.AuthenticatedMainScreen
import com.vljx.hawkspeed.ui.screens.authenticated.choosevehicle.ChooseVehicleScreen
import com.vljx.hawkspeed.ui.screens.authenticated.choosevehicle.ChooseVehicleViewModel.Companion.ARG_VEHICLE_STOCK_UID
import com.vljx.hawkspeed.ui.screens.authenticated.leaderboarddetail.RaceLeaderboardDetailScreen
import com.vljx.hawkspeed.ui.screens.authenticated.setup.SetupAccountScreen
import com.vljx.hawkspeed.ui.screens.authenticated.setuptrack.SetupTrackDetailScreen
import com.vljx.hawkspeed.ui.screens.authenticated.trackdetail.TrackDetailScreen
import com.vljx.hawkspeed.ui.screens.authenticated.userdetail.UserDetailScreen
import com.vljx.hawkspeed.ui.screens.authenticated.vehicledetail.VehicleDetailScreen
import com.vljx.hawkspeed.ui.screens.authenticated.verify.VerifyAccountScreen
import com.vljx.hawkspeed.ui.screens.authenticated.world.WorldMapScreen
import timber.log.Timber

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
 * This is the choose vehicle screen; to be used to select a new vehicle stock for use.
 */
object ChooseVehicleDestination: AppDestination("choose_vehicle_destination") {
    /**
     * TODO: there's a few arguments to define here; existing make UID, type Id, model UID, year etc.
     */
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
 * Viewing a Vehicle's detail.
 */
object VehicleDetailDestination: AppDestination("vehicle_detail_destination") {
    const val userUidArg = "userUid"
    const val vehicleUidArg = "vehicleUid"
    val routeWithArgs = "$route/{${userUidArg}}/{${vehicleUidArg}}"
    val arguments = listOf(
        navArgument(userUidArg) {
            type = NavType.StringType
            nullable = false
        },
        navArgument(vehicleUidArg) {
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
    val routeWithArgs = "$route/{${trackUidArg}}"

    val arguments = listOf(
        navArgument(trackUidArg) {
            type = NavType.StringType
            nullable = false
        }
    )
}

/**
 * Viewing a race leaderboard entry's detail.
 */
object RaceLeaderboardDetailDestination: AppDestination("race_leaderboard_detail_destination") {
    const val raceUidArg = "raceUid"
    const val trackUidArg = "trackUid"
    val routeWithArgs = "$route/{${raceUidArg}}/{${trackUidArg}}"

    val arguments = listOf(
        navArgument(raceUidArg) {
            type = NavType.StringType
            nullable = false
        },
        navArgument(trackUidArg) {
            type = NavType.StringType
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
                },
                onChooseVehicleClicked = {
                    // When choose vehicle is requested, navigate to the ChooseVehicleScreen.
                    navHostController.navigate(
                        ChooseVehicleDestination.route
                    )
                },
                navHostController = navHostController
            )
        }

        composable(
            route = ChooseVehicleDestination.route
        ) { navBackStackEntry ->
            ChooseVehicleScreen(
                onVehicleStockChosen = { vehicleStock ->
                    // Now set the chosen vehicle stock UID in saved state handle.
                    navHostController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set(ARG_VEHICLE_STOCK_UID, vehicleStock.vehicleStockUid)
                    // Pop back stack to return to previous screen.
                    navHostController.popBackStack()
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
                onViewVehiclesClicked = { userUid -> /* TODO: view all vehicles for this user. */ },
                onViewTracksClicked = { userUid -> /* TODO: view all tracks for this user. */ },
                onSettingsClicked = { /* TODO: navigate to our settings. */ },

                onViewUserDetail = { user ->
                    // Navigate to User detail, with the given user's UID.
                    navHostController.navigate(
                        UserDetailDestination.withArgs(user.userUid)
                    )
                },
                onViewTrackDetail = { track ->
                    // Navigate to track detail, with the given track's UID.
                    navHostController.navigate(
                        TrackDetailDestination.withArgs(track.trackUid)
                    )
                },
                onViewRaceLeaderboardDetail = { raceLeaderboard ->
                    // Navigate to race leaderboard detail, with the given race leaderboard's race and track UIDs.
                    navHostController.navigate(
                        RaceLeaderboardDetailDestination.withArgs(raceLeaderboard.raceUid, raceLeaderboard.trackUid)
                    )
                },
                onSetupTrackDetails = { trackDraftId ->
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
            // Show the user detail screen.
            UserDetailScreen()
        }

        composable(
            route = TrackDetailDestination.routeWithArgs,
            arguments = TrackDetailDestination.arguments
        ) { navBackStackEntry ->
            // Show the track detail screen.
            TrackDetailScreen(
                onViewUserDetail = { user ->
                    // Navigate to the user detail screen.
                    navHostController.navigate(
                        UserDetailDestination.withArgs(user.userUid)
                    )
                },
                onViewRaceLeaderboardDetail = { raceLeaderboard ->
                    // Navigate to the leaderboard detail.
                    navHostController.navigate(
                        RaceLeaderboardDetailDestination.withArgs(raceLeaderboard.raceUid, raceLeaderboard.trackUid)
                    )
                }
            )
        }

        composable(
            route = VehicleDetailDestination.routeWithArgs,
            arguments = VehicleDetailDestination.arguments
        ) { navBackStackEntry ->
            // Show the vehicle detail screen.
            VehicleDetailScreen(
                onViewUserDetail = { user ->
                    // Navigate to the user detail screen.
                    navHostController.navigate(
                        UserDetailDestination.withArgs(user.userUid)
                    )
                }
            )
        }

        composable(
            route = RaceLeaderboardDetailDestination.routeWithArgs,
            arguments = RaceLeaderboardDetailDestination.arguments
        ) { navBackStackEntry ->
            // Show the race leaderboard detail screen.
            RaceLeaderboardDetailScreen(
                onViewUserDetailClicked = { user ->
                    // Navigate to the user detail screen.
                    navHostController.navigate(
                        UserDetailDestination.withArgs(user.userUid)
                    )
                },
                onViewTrackDetailClicked = { track ->
                    // Navigate to track detail, with the given track's UID.
                    navHostController.navigate(
                        TrackDetailDestination.withArgs(track.trackUid)
                    )
                },
                onViewVehicleDetailClicked = { vehicle ->
                    // Navigate to vehicle detail for the vehicle's UID and owner's UID.
                    navHostController.navigate(
                        VehicleDetailDestination.withArgs(vehicle.user.userUid, vehicle.vehicleUid)
                    )
                }
            )
        }

        composable(
            route = SetupTrackDetailDestination.routeWithArgs,
            arguments = SetupTrackDetailDestination.arguments
        ) { navBackStackEntry ->
            // Show the setup track detail screen.
            SetupTrackDetailScreen(
                onTrackCreated = { trackWithPath ->
                    // TODO: when we have successfully created a path, we now want to navigate all the way back to world map.
                    throw NotImplementedError("onTrackCreated in SetupTrackDetailDestination composable not yet properly implemented.")
                }
            )
        }
    }
}