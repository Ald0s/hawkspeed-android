package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.domain.models.world.PlayerPositionWithOrientation
import com.vljx.hawkspeed.util.ThirdParty

@Deprecated("Non-overlay functions are no longer used. Please use mapoverlay.")
@Composable
fun DrawCurrentPlayer(
    newPlayerPositionWithOrientation: PlayerPositionWithOrientation,
    oldPlayerPositionWithOrientation: PlayerPositionWithOrientation?,
    isFollowing: Boolean = false
) {
    /**
     * TODO: if new player position and old player position are both given, and are different, animate a change between old player
     * TODO position and new player position.
     */
    Marker(
        state = MarkerState(
            position = LatLng(
                newPlayerPositionWithOrientation.position.latitude,
                newPlayerPositionWithOrientation.position.longitude
            )
        ),
        // TODO: we must further adjust angle of icon
        rotation = newPlayerPositionWithOrientation.orientation.rotation,
        icon = ThirdParty.vectorToBitmap(LocalContext.current, R.drawable.ic_car_side, MaterialTheme.colorScheme.primary)
    )
}