package com.vljx.hawkspeed.ui.component.mapoverlay

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.google.maps.android.compose.CameraPositionState

/**
 * The basic map overlay composable. Essentially, draw a box matching the size of the parent (the google map container) and then allow content
 * to be drawn within that scope, which will provide essential calculations to map features.
 */
@Composable
fun MapOverlay(
    cameraPositionState: CameraPositionState,
    content: @Composable MapOverlayScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MapOverlayScope(
            cameraPositionState = cameraPositionState,
            density = LocalDensity.current
        ).content()
    }
}