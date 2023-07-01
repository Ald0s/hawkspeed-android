package com.vljx.hawkspeed.ui.component

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes

data class TabItem(
    val itemId: String,

    @StringRes
    val titleResId: Int,

    @DrawableRes
    val iconResId: Int
)