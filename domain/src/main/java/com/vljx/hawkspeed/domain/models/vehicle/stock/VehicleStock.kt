package com.vljx.hawkspeed.domain.models.vehicle.stock

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class VehicleStock(
    val vehicleStockUid: String,
    val make: VehicleMake,
    val model: VehicleModel,
    val year: Int,
    val version: String?,
    val badge: String?,
    val motorType: String,
    val displacement: Int,
    val induction: String?,
    val fuelType: String?,
    val power: Int?,
    val electricType: String?,
    val transmissionType: String?,
    val numGears: Int?
): Parcelable {
    /**
     * The title of this vehicle stock.
     */
    val title: String
        get() = "$year ${make.makeName} ${model.modelName}"

    /**
     * Return a display version of the engine information.
     */
    val engineInformation: String
        get() = when(motorType) {
            "piston" ->
                "${displacementLitres}L $fuelTypeInformation $induction"
            /**
             * TODO: support electric motor types.
             */
            "electric" -> throw NotImplementedError("Electric motor types not yet supported")
            else -> throw NotImplementedError("Unrecognised motor type; $motorType")
        }

    /**
     * Return a display version of the transmission information.
     */
    val transmissionInformation: String
        get() = when(transmissionType) {
            "A" -> "Auto"
            "M" -> "$numGears speed M/T"
            else -> throw NotImplementedError("Unrecognised transmission type; $transmissionType")
        }

    /**
     * Return displacement in litres formatted in decimal as a string.
     */
    val displacementLitres: String
        get() = (displacement / 1000f).let { litres ->
            String.format("%.1f", litres)
        }

    /**
     * Return the fuel type.
     */
    val fuelTypeInformation: String
        get() = when(fuelType) {
            "P" -> "Petrol"
            "D" -> "Diesel"
            else -> throw NotImplementedError("Unrecognised fuel type: $fuelType")
        }
}