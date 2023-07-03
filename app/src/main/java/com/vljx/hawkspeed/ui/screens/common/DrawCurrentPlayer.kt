package com.vljx.hawkspeed.ui.screens.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.world.PlayerPosition
import com.vljx.hawkspeed.util.ThirdParty

@Composable
fun DrawCurrentPlayer(
    playerPosition: PlayerPosition
) {
    Marker(
        state = MarkerState(
            position = LatLng(
                playerPosition.latitude,
                playerPosition.longitude
            )
        ),
        rotation = playerPosition.rotation,
        icon = ThirdParty.vectorToBitmap(LocalContext.current, R.drawable.ic_car_side, MaterialTheme.colorScheme.primary)
    )
}