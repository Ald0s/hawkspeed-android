package com.vljx.hawkspeed.data.models.vehicle.stock

import com.vljx.hawkspeed.domain.base.Paged

data class VehicleTypesPageModel(
    val types: List<VehicleTypeModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged