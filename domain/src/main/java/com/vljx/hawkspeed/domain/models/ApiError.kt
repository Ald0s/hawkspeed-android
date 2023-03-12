package com.vljx.hawkspeed.domain.models

import android.os.Parcelable
import com.vljx.hawkspeed.domain.base.BaseApiError
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApiError(
    override val severity: String,
    override val name: String,
    override val errorInformation: HashMap<String, String>
): BaseApiError, Parcelable {
    val isGlobal: Boolean
        get() = name == "global-error"
}