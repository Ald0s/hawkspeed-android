package com.vljx.hawkspeed.ui.component.mapoverlay

import com.google.android.gms.maps.model.LatLng

/**
 * A basic interpolator for generating positional update values between two sets of coordinates based on the given fraction.
 * Source: https://github.com/MigoTiti/Android-Google-Maps-3D-markers/blob/master/app/src/main/java/com/lucasrodrigues/a3dmarkertest/LatLngInterpolator.kt
 */
interface LatLngInterpolator {
    fun interpolate(fraction: Float, latLngFrom: LatLng, latLngTo: LatLng): LatLng

    class Linear {
        companion object: LatLngInterpolator {
            override fun interpolate(fraction: Float, latLngFrom: LatLng, latLngTo: LatLng): LatLng {
                val fractionLatResult = (latLngTo.latitude - latLngFrom.latitude) * fraction + latLngFrom.latitude
                val fractionLongResult = (latLngTo.longitude - latLngFrom.longitude) * fraction + latLngFrom.longitude

                return LatLng(fractionLatResult, fractionLongResult)
            }
        }
    }
}