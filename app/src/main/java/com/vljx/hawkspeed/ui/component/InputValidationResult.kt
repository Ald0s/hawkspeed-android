package com.vljx.hawkspeed.ui.component

import androidx.annotation.StringRes

data class InputValidationResult(
    val isValid: Boolean,
    @StringRes
    val errorId: Int? = null
)