package com.vljx.hawkspeed.data.models.vehicle.stock

import com.vljx.hawkspeed.domain.base.Paged

data class VehicleModelsPageModel(
    val models: List<VehicleModelModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged