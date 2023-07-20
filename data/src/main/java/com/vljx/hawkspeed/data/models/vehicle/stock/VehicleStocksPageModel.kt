package com.vljx.hawkspeed.data.models.vehicle.stock

import com.vljx.hawkspeed.domain.base.Paged

data class VehicleStocksPageModel(
    val vehicles: List<VehicleStockModel>,
    override val thisPage: Int,
    override val nextPage: Int?
): Paged