package com.vljx.hawkspeed.data.models.vehicle.stock

import com.vljx.hawkspeed.domain.base.Paged

data class VehicleYearsPageModel(
    val years: List<VehicleYearModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged