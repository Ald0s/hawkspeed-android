package com.vljx.hawkspeed.ui.screens.authenticated.choosevehicle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vljx.hawkspeed.data.di.qualifier.IODispatcher
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleType
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleYear
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleMakes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleModels
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleStocks
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleTypes
import com.vljx.hawkspeed.domain.requestmodels.vehicle.stock.RequestVehicleYears
import com.vljx.hawkspeed.domain.usecase.vehicle.stock.PageVehicleMakesUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.stock.PageVehicleModelsUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.stock.PageVehicleStocksUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.stock.PageVehicleTypesUseCase
import com.vljx.hawkspeed.domain.usecase.vehicle.stock.PageVehicleYearsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ChooseVehicleViewModel @Inject constructor(
    private val pageVehicleMakesUseCase: PageVehicleMakesUseCase,
    private val pageVehicleTypesUseCase: PageVehicleTypesUseCase,
    private val pageVehicleModelsUseCase: PageVehicleModelsUseCase,
    private val pageVehicleYearsUseCase: PageVehicleYearsUseCase,
    private val pageVehicleStocksUseCase: PageVehicleStocksUseCase,

    private val savedStateHandle: SavedStateHandle,
    @IODispatcher
    private val ioDispatcher: CoroutineDispatcher
): ViewModel() {
    /**
     * A mutable state flow for the currently selected make.
     */
    private val mutableMake: MutableStateFlow<VehicleMake?> = MutableStateFlow(null)

    /**
     * A mutable state flow for the currently selected type.
     */
    private val mutableType: MutableStateFlow<VehicleType?> = MutableStateFlow(null)

    /**
     * A mutable state flow for the currently selected model.
     */
    private val mutableModel: MutableStateFlow<VehicleModel?> = MutableStateFlow(null)

    /**
     * A mutable state flow for the currently selected year.
     */
    private val mutableYear: MutableStateFlow<VehicleYear?> = MutableStateFlow(null)

    /**
     * Emit the appropriate choose vehicle UI state after deciding the artefact the User is currently after, given selected values above.
     */
    val chooseVehicleUiState: StateFlow<ChooseVehicleUiState> =
        combine(
            mutableMake,
            mutableType,
            mutableModel,
            mutableYear
        ) { make, type, model, year ->
            when {
                /**
                 * We have all the artefacts. Present the User with a flow for paging all vehicle stocks from that make, type, model and year.
                 */
                make != null && type != null && model != null && year != null ->
                    ChooseVehicleUiState.SelectVehicleStock(
                        make,
                        type,
                        model,
                        year,
                        pageVehicleStocksUseCase(RequestVehicleStocks(make.makeUid, type.typeId, model.modelUid, year.year))
                    )
                /**
                 * We have a make, type and a model. Present the User with a flow for paging all vehicle years from that make, type and model.
                 */
                make != null && type != null && model != null ->
                    ChooseVehicleUiState.SelectVehicleYear(
                        make,
                        type,
                        model,
                        pageVehicleYearsUseCase(RequestVehicleYears(make.makeUid, type.typeId, model.modelUid))
                    )
                /**
                 * We have a make and a type. Present the User with a flow for paging all vehicle models from that type and make.
                 */
                make != null && type != null->
                    ChooseVehicleUiState.SelectVehicleModel(
                        make,
                        type,
                        pageVehicleModelsUseCase(RequestVehicleModels(make.makeUid, type.typeId))
                    )
                /**
                 * We have just a vehicle make. Present the User with a flow for paging all vehicle types from that make.
                 */
                make != null ->
                    ChooseVehicleUiState.SelectVehicleType(
                        make,
                        pageVehicleTypesUseCase(RequestVehicleTypes(make.makeUid))
                    )
                /**
                 * We have no vehicle artefacts selected at all. Present the User with a flow for paging all vehicle makes.
                 */
                else ->
                    ChooseVehicleUiState.SelectVehicleMake(
                        pageVehicleMakesUseCase(RequestVehicleMakes())
                    )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ChooseVehicleUiState.Loading)

    init {
        val makeUid: String? = savedStateHandle[ARG_MAKE_UID]
        val typeId: String? = savedStateHandle[ARG_TYPE_ID]
        val modelUid: String? = savedStateHandle[ARG_MODEL_UID]
        val year: Int? = savedStateHandle[ARG_YEAR]

        if(makeUid != null) {
            selectArguments(
                makeUid,
                typeId,
                modelUid,
                year
            )
        }
    }

    /**
     * Set the current selected make. Calling this will clear all values below.
     */
    fun selectMake(make: VehicleMake) {
        mutableYear.value = null
        mutableModel.value = null
        mutableType.value = null
        mutableMake.value = make
    }

    /**
     * Set the current selected type. Calling this will clear all values below.
     */
    fun selectType(type: VehicleType) {
        mutableYear.value = null
        mutableModel.value = null
        mutableType.value = type
    }

    /**
     * Set the current selected model. Calling this will clear all values below.
     */
    fun selectModel(model: VehicleModel) {
        mutableYear.value = null
        mutableModel.value = model
    }

    /**
     * Set the current selected year. Calling this will clear all values below.
     */
    fun selectYear(year: VehicleYear) {
        mutableYear.value = year
    }

    /**
     * Set parameters that were previously selected.
     */
    private fun selectArguments(
        makeUid: String,
        typeId: String? = null,
        modelUid: String? = null,
        year: Int? = null
    ) {
        /**
         * TODO: we must get these UID/ID representations from where they are to objects, so that we can then emit these as they are to our mutable flows above.
         */
        throw NotImplementedError()
    }

    companion object {
        const val ARG_MAKE_UID = "makeUid"
        const val ARG_TYPE_ID = "typeId"
        const val ARG_MODEL_UID = "modelUid"
        const val ARG_YEAR = "year"
        const val ARG_VEHICLE_STOCK_UID = "vehicleStockUid"
    }
}