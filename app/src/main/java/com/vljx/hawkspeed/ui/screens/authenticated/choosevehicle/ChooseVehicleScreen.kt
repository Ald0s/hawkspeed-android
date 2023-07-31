package com.vljx.hawkspeed.ui.screens.authenticated.choosevehicle

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleMake
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleModel
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleStock
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleType
import com.vljx.hawkspeed.domain.models.vehicle.stock.VehicleYear
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData

@Composable
fun ChooseVehicleScreen(
    onVehicleStockChosen: ((VehicleStock) -> Unit)? = null,

    chooseVehicleViewModel: ChooseVehicleViewModel = hiltViewModel()
) {
    val chooseVehicleUiState by chooseVehicleViewModel.chooseVehicleUiState.collectAsStateWithLifecycle()
    when(chooseVehicleUiState) {
        is ChooseVehicleUiState.Loading ->
            LoadingScreen()
        else ->
            ChooseVehicle(
                chooseVehicleUiState = chooseVehicleUiState,
                onChooseMake = chooseVehicleViewModel::selectMake,
                onChooseType = chooseVehicleViewModel::selectType,
                onChooseModel = chooseVehicleViewModel::selectModel,
                onChooseYear = chooseVehicleViewModel::selectYear,
                onVehicleStockChosen = onVehicleStockChosen
            )
    }
}

@Composable
fun ChooseVehicle(
    chooseVehicleUiState: ChooseVehicleUiState,

    onChooseMake: ((VehicleMake) -> Unit)? = null,
    onChooseType: ((VehicleType) -> Unit)? = null,
    onChooseModel: ((VehicleModel) -> Unit)? = null,
    onChooseYear: ((VehicleYear) -> Unit)? = null,
    onVehicleStockChosen: ((VehicleStock) -> Unit)? = null
) {
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
        ) {
            // Now, decide what we're going to do based on the current choose vehicle UI state.
            when(chooseVehicleUiState) {
                is ChooseVehicleUiState.SelectVehicleMake ->
                    ChooseVehicleMake(
                        selectVehicleMake = chooseVehicleUiState,
                        onChooseMake = onChooseMake
                    )
                is ChooseVehicleUiState.SelectVehicleType ->
                    ChooseVehicleType(
                        selectVehicleType = chooseVehicleUiState,
                        onChooseType = onChooseType
                    )
                is ChooseVehicleUiState.SelectVehicleModel ->
                    ChooseVehicleModel(
                        selectVehicleModel = chooseVehicleUiState,
                        onChooseModel = onChooseModel
                    )
                is ChooseVehicleUiState.SelectVehicleYear ->
                    ChooseVehicleYear(
                        selectVehicleYear = chooseVehicleUiState,
                        onChooseYear = onChooseYear
                    )
                is ChooseVehicleUiState.SelectVehicleStock ->
                    ChooseVehicleStock(
                        selectVehicleStock = chooseVehicleUiState,
                        onVehicleStockChosen = onVehicleStockChosen
                    )
                else -> { /* Loading not handled here. */ }
            }
        }
    }
}

@Composable
fun ChooseVehicleMake(
    selectVehicleMake: ChooseVehicleUiState.SelectVehicleMake,
    onChooseMake: ((VehicleMake) -> Unit)? = null
) {
    // Collect all makes as lazy paging items.
    val makes: LazyPagingItems<VehicleMake> = selectVehicleMake.vehicleMakesFlow.collectAsLazyPagingItems()
    // Now create a column to hold the explanation of whats going on.
    Column(
        modifier = Modifier
            .padding(24.dp)
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(R.string.choose_make),
            style = MaterialTheme.typography.headlineMedium
        )
    }
    // Now create a lazy column and list them all!
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = makes.itemCount,
            key = makes.itemKey { it.makeUid },
            contentType = makes.itemContentType { "MakeItems" }
        ) { index ->
            val make = makes[index]
                ?: throw NotImplementedError()
            VehicleMakeItem(
                vehicleMake = make,
                onClicked = onChooseMake ?: {}
            )
            if(index < makes.itemCount-1) {
                Divider()
            }
        }
    }
}

@Composable
fun ChooseVehicleType(
    selectVehicleType: ChooseVehicleUiState.SelectVehicleType,
    onChooseType: ((VehicleType) -> Unit)? = null
) {
    // Collect all types as lazy paging items.
    val types: LazyPagingItems<VehicleType> = selectVehicleType.vehicleTypesFlow.collectAsLazyPagingItems()
    // Now create a column to hold the explanation of whats going on.
    Column(
        modifier = Modifier
            .padding(24.dp)
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(R.string.choose_type),
            style = MaterialTheme.typography.headlineMedium
        )
    }
    // Now create a lazy column and list them all!
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = types.itemCount,
            key = types.itemKey { it.typeId },
            contentType = types.itemContentType { "TypeItems" }
        ) { index ->
            val type = types[index]
                ?: throw NotImplementedError()
            VehicleTypeItem(
                vehicleMake = selectVehicleType.vehicleMake,
                vehicleType = type,
                onClicked = onChooseType ?: {}
            )
            if(index < types.itemCount-1) {
                Divider()
            }
        }
    }
}

@Composable
fun ChooseVehicleModel(
    selectVehicleModel: ChooseVehicleUiState.SelectVehicleModel,
    onChooseModel: ((VehicleModel) -> Unit)? = null
) {
    // Collect all models as lazy paging items.
    val models: LazyPagingItems<VehicleModel> = selectVehicleModel.vehicleModelsFlow.collectAsLazyPagingItems()
    // Now create a column to hold the explanation of whats going on.
    Column(
        modifier = Modifier
            .padding(24.dp)
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(R.string.choose_model),
            style = MaterialTheme.typography.headlineMedium
        )
    }
    // Now create a lazy column and list them all!
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = models.itemCount,
            key = models.itemKey { it.modelUid },
            contentType = models.itemContentType { "ModelItems" }
        ) { index ->
            val model = models[index]
                ?: throw NotImplementedError()
            VehicleModelItem(
                vehicleMake = selectVehicleModel.vehicleMake,
                vehicleType = selectVehicleModel.vehicleType,
                vehicleModel = model,
                onClicked = onChooseModel ?: {}
            )
            if(index < models.itemCount-1) {
                Divider()
            }
        }
    }
}

@Composable
fun ChooseVehicleYear(
    selectVehicleYear: ChooseVehicleUiState.SelectVehicleYear,
    onChooseYear: ((VehicleYear) -> Unit)? = null
) {
    // Collect all years as lazy paging items.
    val years: LazyPagingItems<VehicleYear> = selectVehicleYear.vehicleYearsFlow.collectAsLazyPagingItems()
    // Now create a column to hold the explanation of whats going on.
    Column(
        modifier = Modifier
            .padding(24.dp)
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(R.string.choose_year),
            style = MaterialTheme.typography.headlineMedium
        )
    }
    // Now create a lazy column and list them all!
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = years.itemCount,
            key = years.itemKey { it.year },
            contentType = years.itemContentType { "YearItems" }
        ) { index ->
            val year = years[index]
                ?: throw NotImplementedError()
            VehicleYearItem(
                vehicleMake = selectVehicleYear.vehicleMake,
                vehicleType = selectVehicleYear.vehicleType,
                vehicleModel = selectVehicleYear.vehicleModel,
                vehicleYear = year,
                onClicked = onChooseYear ?: {}
            )
            if(index < years.itemCount-1) {
                Divider()
            }
        }
    }
}

@Composable
fun ChooseVehicleStock(
    selectVehicleStock: ChooseVehicleUiState.SelectVehicleStock,
    onVehicleStockChosen: ((VehicleStock) -> Unit)? = null
) {
    // Collect all vehicle stocks as lazy paging items.
    val vehicleStocks: LazyPagingItems<VehicleStock> = selectVehicleStock.vehicleStocksFlow.collectAsLazyPagingItems()
    // Now create a column to hold the explanation of whats going on.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Text(
            text = stringResource(R.string.choose_vehicle_stock),
            style = MaterialTheme.typography.headlineMedium
        )
    }
    // Now create a lazy column and list them all!
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = vehicleStocks.itemCount,
            key = vehicleStocks.itemKey { it.vehicleStockUid },
            contentType = vehicleStocks.itemContentType { "VehicleStockItems" }
        ) { index ->
            val vehicleStock = vehicleStocks[index]
                ?: throw NotImplementedError()
            VehicleStockItem(
                vehicleStock = vehicleStock,
                onClicked = onVehicleStockChosen ?: {}
            )
            if(index < vehicleStocks.itemCount-1) {
                Divider()
            }
        }
    }
}

@Composable
fun VehicleMakeItem(
    vehicleMake: VehicleMake,
    onClicked: (VehicleMake) -> Unit
) {
    Surface {
        Row(
            modifier = Modifier
                .clickable {
                    onClicked(vehicleMake)
                }
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            // TODO: make logo image here.
            Text(
                text = vehicleMake.makeName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VehicleTypeItem(
    vehicleMake: VehicleMake,
    vehicleType: VehicleType,
    onClicked: (VehicleType) -> Unit
) {
    Surface {
        Row(
            modifier = Modifier
                .clickable {
                    onClicked(vehicleType)
                }
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = vehicleType.typeName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun VehicleModelItem(
    vehicleMake: VehicleMake,
    vehicleType: VehicleType,
    vehicleModel: VehicleModel,
    onClicked: (VehicleModel) -> Unit
) {
    Surface {
        Row(
            modifier = Modifier
                .clickable {
                    onClicked(vehicleModel)
                }
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Text(
                    text = vehicleMake.makeName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = vehicleModel.modelName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun VehicleYearItem(
    vehicleMake: VehicleMake,
    vehicleType: VehicleType,
    vehicleModel: VehicleModel,
    vehicleYear: VehicleYear,
    onClicked: (VehicleYear) -> Unit
) {
    Surface {
        Row(
            modifier = Modifier
                .clickable {
                    onClicked(vehicleYear)
                }
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${vehicleMake.makeName} ${vehicleModel.modelName}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${vehicleYear.year}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun VehicleStockItem(
    vehicleStock: VehicleStock,
    onClicked: (VehicleStock) -> Unit
) {
    Surface {
        Column(
            modifier = Modifier
                .clickable {
                    onClicked(vehicleStock)
                }
                .padding(24.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Text(
                        text = vehicleStock.title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Row {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .weight(1f)
                ) {
                    Text(text = vehicleStock.engineInformation)
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .weight(1f)
                ) {
                    Text(text = vehicleStock.transmissionInformation)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewVehicleMakeItem(

) {
    val vehicleStock = ExampleData.getExampleVehicle().vehicleStock
    HawkSpeedTheme {
        VehicleMakeItem(
            vehicleMake = vehicleStock.make,
            onClicked = {}
        )
    }
}

@Preview
@Composable
fun PreviewVehicleTypeItem(

) {
    val vehicleStock = ExampleData.getExampleVehicle().vehicleStock
    HawkSpeedTheme {
        VehicleTypeItem(
            vehicleMake = vehicleStock.make,
            vehicleType = vehicleStock.model.type,
            onClicked = {}
        )
    }
}

@Preview
@Composable
fun PreviewVehicleModelItem(

) {
    val vehicleStock = ExampleData.getExampleVehicle().vehicleStock
    HawkSpeedTheme {
        VehicleModelItem(
            vehicleMake = vehicleStock.make,
            vehicleType = vehicleStock.model.type,
            vehicleModel = vehicleStock.model,
            onClicked = {}
        )
    }
}

@Preview
@Composable
fun PreviewVehicleYearItem(

) {
    val vehicleStock = ExampleData.getExampleVehicle().vehicleStock
    HawkSpeedTheme {
        VehicleYearItem(
            vehicleMake = vehicleStock.make,
            vehicleType = vehicleStock.model.type,
            vehicleModel = vehicleStock.model,
            vehicleYear = VehicleYear(vehicleStock.year),
            onClicked = {}
        )
    }
}

@Preview
@Composable
fun PreviewVehicleStockItem(

) {
    val vehicleStock = ExampleData.getExampleVehicle().vehicleStock
    HawkSpeedTheme {
        VehicleStockItem(
            vehicleStock = vehicleStock,
            onClicked = {}
        )
    }
}