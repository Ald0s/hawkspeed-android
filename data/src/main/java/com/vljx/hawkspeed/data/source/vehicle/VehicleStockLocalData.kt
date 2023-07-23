package com.vljx.hawkspeed.data.source.vehicle

import com.vljx.hawkspeed.data.models.vehicle.stock.VehicleStockModel
import kotlinx.coroutines.flow.Flow

interface VehicleStockLocalData {
    /**
     * Select a vehicle stock by its UID from cache.
     */
    fun selectVehicleStockByUid(vehicleStockUid: String): Flow<VehicleStockModel?>

    /**
     * Upsert a list of vehicle stocks.
     */
    suspend fun upsertVehicleStocks(vehicleStocks: List<VehicleStockModel>)

    /**
     * Upsert a single vehicle stock.
     */
    suspend fun upsertVehicleStock(vehicleStock: VehicleStockModel)

    /**
     * Clear all vehicle stocks.
     */
    suspend fun clearVehicleStocks()
}