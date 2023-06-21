package com.vljx.hawkspeed.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.vljx.hawkspeed.navigation.navgraph.SPLASH_GRAPH_ROUTE
import com.vljx.hawkspeed.navigation.navgraph.authenticatedNavGraph
import com.vljx.hawkspeed.navigation.navgraph.onboardNavGraph
import com.vljx.hawkspeed.navigation.navgraph.splashNavGraph

const val ROOT_GRAPH_ROUTE = "root"

@Composable
fun SetupNavGraph(
    navHostController: NavHostController
) {
    NavHost(
        navController = navHostController,
        startDestination = SPLASH_GRAPH_ROUTE,
        route = ROOT_GRAPH_ROUTE
    ) {
        splashNavGraph(navHostController = navHostController)
        onboardNavGraph(navHostController = navHostController)
        authenticatedNavGraph(navHostController = navHostController)
    }
}