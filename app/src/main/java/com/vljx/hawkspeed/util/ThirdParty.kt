package com.vljx.hawkspeed.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.annotation.DrawableRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds


class ThirdParty {
    companion object {
        fun vectorToBitmap(
            context: Context,
            @DrawableRes drawableResId: Int,
            desiredColour: Color
        ): BitmapDescriptor {
            // Create a vector drawable for the source vector.
            val vectorDrawable = ContextCompat.getDrawable(context, drawableResId)
            // Create a new bitmap for the intrinsic width and height of the vector drawable.
            vectorDrawable!!
            val bitmap = Bitmap.createBitmap(vectorDrawable.intrinsicWidth, vectorDrawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            // Create a new canvas for this bitmap.
            val canvas = Canvas(bitmap)
            // Set bounds of the vector drawable to the canvas width and height.
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            // Set the tint for the vector drawable.
            DrawableCompat.setTint(vectorDrawable, desiredColour.toArgb())
            // Now, draw vector drawable to the canvas.
            vectorDrawable.draw(canvas)
            // Finally, return bitmap descriptor from bitmap we just created.
            return BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}