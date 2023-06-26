package com.vljx.hawkspeed.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.navigation.NavHostController

abstract class AppDestination(
    // The route we must navigate to in order to reach this destination.
    val route: String
) {
    // The route's title; as a string resource Id. If given, this will be displayed on whatever title screen is available (unless overridden.)
    @get:StringRes
    val title: Int? = null

    @get:DrawableRes
    val icon: Int? = null

    /**
     * Construct a destination toward the implementing destination with a consideration for arguments.
     */
    fun withArgs(vararg args: Any): String {
        return buildString {
            append(route)
            args.forEach { arg ->
                append("/$arg")
            }
        }
    }
}

fun NavHostController.navigatePopUpToInclusive(routeId: String) =
    this.navigate(routeId) {
        this@navigatePopUpToInclusive.graph.route?.let { currentGraphRoute ->
            popUpTo(currentGraphRoute) {
                inclusive = true
            }
        }
    }