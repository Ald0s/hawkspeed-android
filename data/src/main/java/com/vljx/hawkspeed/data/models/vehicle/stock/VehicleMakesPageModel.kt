package com.vljx.hawkspeed.data.models.vehicle.stock

import com.vljx.hawkspeed.domain.base.Paged

data class VehicleMakesPageModel(
    val makes: List<VehicleMakeModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged