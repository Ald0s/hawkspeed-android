package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as arrItems
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.vljx.hawkspeed.Extension.toOverviewCameraUpdate
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.domain.enums.TrackType
import com.vljx.hawkspeed.domain.models.race.RaceLeaderboard
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.TrackComment
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.user.User
import com.vljx.hawkspeed.domain.models.world.BoundingBox
import com.vljx.hawkspeed.ui.component.TabItem
import com.vljx.hawkspeed.ui.screens.common.DrawRaceTrack
import com.vljx.hawkspeed.ui.screens.common.Loading
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.screens.common.RaceLeaderboardHeaderItem
import com.vljx.hawkspeed.ui.screens.common.RaceLeaderboardItem
import com.vljx.hawkspeed.ui.theme.HawkSpeedTheme
import com.vljx.hawkspeed.util.ExampleData
import com.vljx.hawkspeed.util.Extension.getActivity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch


const val TAB_OVERVIEW = "overview"
const val TAB_LEADERBOARD = "leaderboard"
const val TAB_REVIEWS = "reviews"

val trackDetailTabs = listOf(
    TabItem(
        TAB_OVERVIEW,
        R.string.track_detail_overview,
        R.drawable.ic_info_circle
    ),
    TabItem(
        TAB_LEADERBOARD,
        R.string.track_detail_leaderboard,
        R.drawable.ic_trophy
    ),
    TabItem(
        TAB_REVIEWS,
        R.string.track_detail_reviews,
        R.drawable.ic_comments
    )
)

@Composable
fun TrackDetailScreen(
    trackDetailViewModel: TrackDetailViewModel = hiltViewModel()
) {
    // Collect the track detail UI state.
    val trackDetailUiState by trackDetailViewModel.trackDetailUiState.collectAsState()
    // Call the track detail composable with the latest state.
    TrackDetail(
        trackDetailUiState = trackDetailUiState,
        leaderboardFlow = trackDetailViewModel.leaderboard,
        commentFlow = trackDetailViewModel.comments,
        componentActivity = LocalContext.current.getActivity()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackDetail(
    trackDetailUiState: TrackDetailUiState,
    leaderboardFlow: Flow<PagingData<RaceLeaderboard>>,
    commentFlow: Flow<PagingData<TrackComment>>,
    componentActivity: ComponentActivity? = null
) {
    // Set up a scaffold. For all the content.
    Scaffold(
        modifier = Modifier
    ) { paddingValues ->
        // Setup a new column to take from the scaffold padding.
        Column(
            modifier = Modifier
                .padding(paddingValues)
        ) {
            // Base contents on the latest track detail UI state.
            when(trackDetailUiState) {
                is TrackDetailUiState.GotTrackDetail -> {
                    val track = trackDetailUiState.track
                    val trackPath = trackDetailUiState.trackPath
                    // Set up a new row to contain the center aligned content.
                    Row(
                        modifier = Modifier
                            .padding(top = 32.dp, bottom = 32.dp)
                    ) {
                        // Setup a new column to hold the centered information; that is, the actual track path overview and the
                        // track title and creator info.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Set up the track path overview here. We want to provide a modifier that will size the box appropriately.
                            TrackPathOverview(
                                track = track,
                                trackPath = trackPath,
                                componentActivity = componentActivity
                            )
                            // Now, the title of the track.
                            Text(
                                text = track.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                    // Set up a row here for the remainder of the UI- the tab host and its left aligned content.
                    Row {
                        Column(modifier = Modifier.fillMaxSize()) {
                            TrackTabHost(
                                track = trackDetailUiState.track,
                                leaderboardFlow = leaderboardFlow,
                                commentFlow = commentFlow
                            )
                        }
                    }
                }
                is TrackDetailUiState.Loading -> {
                    Loading()
                }
                is TrackDetailUiState.Failed -> {
                    // TODO: some failed indicator here.
                    throw NotImplementedError()
                }
            }
        }
    }
}

@Composable
fun TrackPathOverview(
    track: Track,
    trackPath: TrackPath,
    modifier: Modifier = Modifier,
    componentActivity: ComponentActivity? = null
) {
    // The track path overview's camera position state.
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(track.startPoint.latitude, track.startPoint.longitude),
            15f
        )
    }
    var isMapLoaded by remember { mutableStateOf<Boolean>(false) }

    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        ),
        modifier = modifier
            .height(300.dp)
            .fillMaxWidth(0.6f)
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            var uiSettings by remember {
                mutableStateOf(MapUiSettings(
                    compassEnabled = false,
                    myLocationButtonEnabled = false,
                    indoorLevelPickerEnabled = false,
                    mapToolbarEnabled = false,
                    rotationGesturesEnabled = false,
                    scrollGesturesEnabled = false,
                    tiltGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    zoomControlsEnabled = false
                ))
            }
            var mapProperties by remember {
                mutableStateOf(MapProperties(
                    isBuildingEnabled = false,
                    isIndoorEnabled = false,
                    isMyLocationEnabled = false,
                    minZoomPreference = 3.0f,
                    maxZoomPreference = 21.0f,
                    mapStyleOptions = componentActivity?.let { activity ->
                        MapStyleOptions.loadRawResourceStyle(
                            activity,
                            R.raw.worldstyle
                        )
                    }
                ))
            }
            // We will draw a Google Map composable, with position locked on the track's path above. And some padding, too.
            GoogleMap(
                modifier = Modifier
                    .fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onMapLoaded = {
                    // When map is loaded, cause a change to the camera such that we move to the track path's bounds.
                    val boundingBox: BoundingBox = trackPath.getBoundingBox()
                    cameraPositionState.move(boundingBox.toOverviewCameraUpdate())
                    isMapLoaded = true
                }
            ) {
                // Draw the race track.
                DrawRaceTrack(
                    track = track,
                    trackPath = trackPath
                )
            }
            // If map is not yet loaded, overlay an animated visibility over the top.
            if(!isMapLoaded) {
                AnimatedVisibility(
                    modifier = Modifier
                        .fillMaxSize(),
                    visible = true,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.background)
                            .wrapContentSize()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TrackTabHost(
    track: Track,
    leaderboardFlow: Flow<PagingData<RaceLeaderboard>>,
    commentFlow: Flow<PagingData<TrackComment>>,
    modifier: Modifier = Modifier,

    onLeaderboardEntryClicked: ((RaceLeaderboard) -> Unit)? = null
) {
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = modifier
    ) {
        TabRow(
            selectedTabIndex = pagerState.currentPage
        ) {
            trackDetailTabs.forEachIndexed { index, tabItem ->
                Tab(
                    selected = index == pagerState.currentPage,
                    text = { Text(text = stringResource(id = tabItem.titleResId)) },
                    icon = { Icon(painter = painterResource(id = tabItem.iconResId), "") },
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    }
                )
            }
        }
        HorizontalPager(
            modifier = Modifier,
            pageCount = trackDetailTabs.size,
            state = pagerState
        ) {
            when(trackDetailTabs[pagerState.currentPage].itemId) {
                TAB_OVERVIEW -> TrackOverview(
                    track = track
                )
                TAB_LEADERBOARD -> TrackLeaderboard(
                    leaderboardFlow = leaderboardFlow,
                    onLeaderboardEntryClicked = onLeaderboardEntryClicked
                )
                TAB_REVIEWS -> TrackReviews(
                    track = track,
                    commentFlow = commentFlow
                )
            }
        }
    }
}

@Composable
fun TrackOverview(
    track: Track,
    modifier: Modifier = Modifier
) {
    // Remember a scroll state.
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(24.dp)
            .verticalScroll(scrollState)
            .fillMaxSize()
    ) {
        // Create a description title and place the description.
        Text(
            text = stringResource(id = R.string.track_description_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
        Text(text = track.description)

        Spacer(modifier = Modifier.height(24.dp))

        // Create a text for the track type's name and description.
        Text(
            text = stringResource(id = when(track.trackType) {
                TrackType.SPRINT -> R.string.track_type_sprint
                TrackType.CIRCUIT -> R.string.track_type_circuit
            }),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
        Text(text = stringResource(id = when (track.trackType) {
            TrackType.SPRINT -> R.string.track_type_sprint_description
            TrackType.CIRCUIT -> R.string.track_type_circuit_description
        }))

        Spacer(modifier = Modifier.height(24.dp))

        // Create a text for the track author's username.
        Text(
            text = stringResource(id = R.string.track_author_title),
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .padding(bottom = 8.dp)
        )
        Text(text = track.owner.userName)
    }
}

@Composable
fun TrackLeaderboard(
    leaderboardFlow: Flow<PagingData<RaceLeaderboard>>,
    modifier: Modifier = Modifier,

    onLeaderboardEntryClicked: ((RaceLeaderboard) -> Unit)? = null
) {
    // Collect as lazy paging items.
    val leaderboard: LazyPagingItems<RaceLeaderboard> = leaderboardFlow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        // Render our leaderboard header item here.
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
        // Render all items in the list.
        items(
            count = leaderboard.itemCount,
            key = leaderboard.itemKey { it },
            contentType = leaderboard.itemContentType { "LeaderboardItems" }
        ) { index ->
            val raceLeaderboard = leaderboard[index]
                ?: throw NotImplementedError()
            RaceLeaderboardItem(
                raceLeaderboard = raceLeaderboard,
                onViewAttemptClicked = onLeaderboardEntryClicked
            )
            Spacer(modifier = Modifier.height(12.dp))
        }
        // Handle edge states like refreshing, end of pagination reached and no items located.
        when {
            leaderboard.loadState.refresh is LoadState.NotLoading -> {
                // No longer refreshing.
            }
            leaderboard.loadState.source.refresh is LoadState.NotLoading &&
                    leaderboard.loadState.append.endOfPaginationReached &&
                    leaderboard.itemCount == 0 -> {
                // Nothing to show. Create a placeholder.
                item {
                    TrackDetailPaginationPlaceholder(
                        stringResId = R.string.track_detail_no_leaderboard,
                        modifier = Modifier
                            .fillParentMaxSize()
                    )
                }
            }
            else -> {

            }
        }
    }
}

@Composable
fun TrackReviews(
    track: Track,
    commentFlow: Flow<PagingData<TrackComment>>,
    modifier: Modifier = Modifier
) {
    // Collect as lazy paging items.
    val comments: LazyPagingItems<TrackComment> = commentFlow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items(
            count = comments.itemCount,
            key = comments.itemKey { it },
            contentType = comments.itemContentType { "CommentItems" }
        ) { index ->
            val comment = comments[index]
                ?: throw NotImplementedError()
            Text(text = comment.text)

            Divider()
        }

        when {
            comments.loadState.refresh is LoadState.NotLoading -> {
                // No longer refreshing.
            }
            comments.loadState.source.refresh is LoadState.NotLoading &&
                    comments.loadState.append.endOfPaginationReached &&
                    comments.itemCount == 0 -> {
                // Nothing to show. Create a placeholder.
                item {
                    TrackDetailPaginationPlaceholder(
                        stringResId = R.string.track_detail_no_comments,
                        modifier = Modifier
                            .fillParentMaxSize()
                    )
                }
            }
            else -> {

            }
        }
    }
}

@Composable
fun TrackDetailPaginationPlaceholder(
    @StringRes stringResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(id = stringResId)
        )
    }
}

@Preview
@Composable
fun PreviewTrackDetail(

) {
    HawkSpeedTheme {
        TrackDetail(
            trackDetailUiState = TrackDetailUiState.GotTrackDetail(
                track = ExampleData.getExampleTrack(
                    description = "One of the better tracks in the Melbourne area. A few hairpins and other sharp turns. Caution! Speed is very limited here."
                ),
                trackPath = ExampleData.getExampleTrackPath(),
            ),
            leaderboardFlow = emptyFlow<PagingData<RaceLeaderboard>>(),
            commentFlow = emptyFlow<PagingData<TrackComment>>(),
        )
    }
}

@Preview
@Composable
fun PreviewTrackTabHost(

) {
    HawkSpeedTheme {
        TrackTabHost(
            track = ExampleData.getExampleTrack(),
            leaderboardFlow = emptyFlow<PagingData<RaceLeaderboard>>(),
            commentFlow = emptyFlow<PagingData<TrackComment>>(),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
        )
    }
}

@Preview
@Composable
fun PreviewTrackOverview(

) {

}

@Preview
@Composable
fun PreviewTrackLeaderboard(

) {
    HawkSpeedTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TrackLeaderboard(
                leaderboardFlow = flow {
                    emit(
                        PagingData.from(
                            ExampleData.getExampleLeaderboard()
                        )
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun PreviewTrackReviews(

) {

}

@Preview
@Composable
fun PreviewTrackDetailPaginationPlaceholder(

) {
    HawkSpeedTheme {
        TrackDetailPaginationPlaceholder(
            stringResId = R.string.track_detail_no_leaderboard,
            modifier = Modifier
                .fillMaxSize()
        )
    }
}