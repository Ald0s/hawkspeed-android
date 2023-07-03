package com.vljx.hawkspeed.data.source.vehicle

import com.vljx.hawkspeed.data.models.vehicle.OurVehiclesModel
import com.vljx.hawkspeed.domain.Resource

interface VehicleRemoteData {
    /**
     * Query the current User's list of Vehicles.
     */
    suspend fun queryOurVehicles(): Resource<OurVehiclesModel>
}