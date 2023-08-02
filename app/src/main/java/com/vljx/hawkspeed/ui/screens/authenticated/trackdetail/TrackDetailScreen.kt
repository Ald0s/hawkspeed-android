package com.vljx.hawkspeed.ui.screens.authenticated.trackdetail

import androidx.activity.ComponentActivity
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
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
import com.vljx.hawkspeed.ui.screens.common.LoadingScreen
import com.vljx.hawkspeed.ui.screens.common.RaceLeaderboardItem
import com.vljx.hawkspeed.ui.screens.common.TrackSubtitle
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
    onViewUserDetail: ((User) -> Unit)? = null,
    onViewRaceLeaderboardDetail: ((RaceLeaderboard) -> Unit)? = null,

    trackDetailViewModel: TrackDetailViewModel = hiltViewModel()
) {
    // Collect the track detail UI state.
    val trackDetailUiState by trackDetailViewModel.trackDetailUiState.collectAsStateWithLifecycle()
    when(trackDetailUiState) {
        is TrackDetailUiState.GotTrackDetail ->
            TrackDetail(
                gotTrackDetail = trackDetailUiState as TrackDetailUiState.GotTrackDetail,
                leaderboardFlow = trackDetailViewModel.leaderboard,
                commentFlow = trackDetailViewModel.comments,
                onViewUserDetail = onViewUserDetail,
                onViewRaceLeaderboardDetail = onViewRaceLeaderboardDetail,
                onUpvoteClicked = trackDetailViewModel::upvoteTrack,
                onDownvoteClicked = trackDetailViewModel::downvoteTrack,
                componentActivity = LocalContext.current.getActivity()
            )
        is TrackDetailUiState.Loading ->
            LoadingScreen()
        is TrackDetailUiState.Failed ->
            // TODO: some failed indicator here.
            throw NotImplementedError()
    }
}

@Composable
fun TrackDetail(
    gotTrackDetail: TrackDetailUiState.GotTrackDetail,
    leaderboardFlow: Flow<PagingData<RaceLeaderboard>>,
    commentFlow: Flow<PagingData<TrackComment>>,

    onViewUserDetail: ((User) -> Unit)? = null,
    onViewRaceLeaderboardDetail: ((RaceLeaderboard) -> Unit)? = null,
    onUpvoteClicked: ((Track) -> Unit)? = null,
    onDownvoteClicked: ((Track) -> Unit)? = null,
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
            val track = gotTrackDetail.track
            val trackPath = gotTrackDetail.trackPath
            // Set up a new row to contain the center aligned content.
            Row(
                modifier = Modifier
                    .padding(top = 32.dp, bottom = 16.dp)
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
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                    // Setup a track subtitle here.
                    TrackSubtitle(
                        track = track
                    )
                }
            }
            // Set up a row here for the remainder of the UI- the tab host and its left aligned content.
            Row {
                Column(modifier = Modifier.fillMaxSize()) {
                    TrackTabHost(
                        track = gotTrackDetail.track,
                        gotTrackRating = gotTrackDetail.gotTrackRating,
                        leaderboardFlow = leaderboardFlow,
                        commentFlow = commentFlow,
                        onViewUserDetail = onViewUserDetail,
                        onLeaderboardEntryClicked = onViewRaceLeaderboardDetail,
                        onUpvoteClicked = onUpvoteClicked,
                        onDownvoteClicked = onDownvoteClicked
                    )
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
            .height(200.dp)
            .fillMaxWidth(0.4f)
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
    gotTrackRating: TrackRatingUiState.GotTrackRating,

    leaderboardFlow: Flow<PagingData<RaceLeaderboard>>,
    commentFlow: Flow<PagingData<TrackComment>>,
    modifier: Modifier = Modifier,

    onViewUserDetail: ((User) -> Unit)? = null,
    onLeaderboardEntryClicked: ((RaceLeaderboard) -> Unit)? = null,
    onUpvoteClicked: ((Track) -> Unit)? = null,
    onDownvoteClicked: ((Track) -> Unit)? = null
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
                    track = track,
                    onAuthorClicked = onViewUserDetail
                )
                TAB_LEADERBOARD -> TrackLeaderboard(
                    leaderboardFlow = leaderboardFlow,
                    onLeaderboardEntryClicked = onLeaderboardEntryClicked
                )
                TAB_REVIEWS -> TrackReviews(
                    track = track,
                    gotTrackRating = gotTrackRating,
                    commentFlow = commentFlow,
                    onUpvoteClicked = onUpvoteClicked,
                    onDownvoteClicked = onDownvoteClicked
                )
            }
        }
    }
}

@Composable
fun TrackOverview(
    track: Track,
    modifier: Modifier = Modifier,
    onAuthorClicked: ((User) -> Unit)? = null
) {
    // Remember a scroll state.
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .padding(32.dp)
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
        Text(
            modifier = Modifier
                .clickable {
                    // If the author's username is clicked, view the user's detail.
                    onAuthorClicked?.invoke(track.owner)
                },
            text = track.owner.userName
        )
    }
}

@Composable
fun TrackLeaderboard(
    leaderboardFlow: Flow<PagingData<RaceLeaderboard>>,
    modifier: Modifier = Modifier,

    onLeaderboardEntryClicked: ((RaceLeaderboard) -> Unit)? = null
) {
    Surface {
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
}

@Composable
fun TrackReviews(
    track: Track,
    gotTrackRating: TrackRatingUiState.GotTrackRating,
    commentFlow: Flow<PagingData<TrackComment>>,
    modifier: Modifier = Modifier,

    onUpvoteClicked: ((Track) -> Unit)? = null,
    onDownvoteClicked: ((Track) -> Unit)? = null
) {
    Surface {
        // Collect as lazy paging items.
        val comments: LazyPagingItems<TrackComment> = commentFlow.collectAsLazyPagingItems()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            item {
                TrackRating(
                    gotTrackRating = gotTrackRating,
                    onUpvoteClicked = {
                        onUpvoteClicked?.invoke(track)
                    },
                    onDownvoteClicked = {
                        onDownvoteClicked?.invoke(track)
                    }
                )
            }

            item {

            }

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
}

@Composable
fun TrackRating(
    gotTrackRating: TrackRatingUiState.GotTrackRating,

    onUpvoteClicked: (() -> Unit)? = null,
    onDownvoteClicked: (() -> Unit)? = null
) {
    // A mutable boolean for disabling the rating buttons on tap.
    var ratingButtonsEnabled by remember { mutableStateOf(false) }

    // First, a section for upvoting/downvoting the track.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(top = 24.dp, bottom = 24.dp)
            .fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.rate_this_track),
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Row {
            // A column for the upvote.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
            ) {
                IconButton(
                    modifier = Modifier
                        .size(64.dp),
                    enabled = ratingButtonsEnabled,
                    onClick = {
                        onUpvoteClicked?.let {
                            it.invoke()
                            ratingButtonsEnabled = false
                        }
                    }
                ) {
                    Image(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(64.dp),
                        painter = painterResource(id = when(gotTrackRating.yourRating) {
                            true -> R.drawable.thumbs_up_solid
                            else -> R.drawable.thumbs_up
                        }),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                        contentDescription = "like"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // The current count of upvotes.
                Text(
                    text = "${gotTrackRating.numPositiveVotes}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(56.dp))
            // A column for the downvote.
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .wrapContentSize()
            ) {
                IconButton(
                    modifier = Modifier
                        .size(64.dp),
                    enabled = ratingButtonsEnabled,
                    onClick = {
                        onDownvoteClicked?.let {
                            it.invoke()
                            ratingButtonsEnabled = false
                        }
                    }
                ) {
                    Image(
                        modifier = Modifier
                            .padding(8.dp)
                            .size(64.dp),
                        painter = painterResource(id = when(gotTrackRating.yourRating) {
                            false -> R.drawable.thumbs_down_solid
                            else -> R.drawable.thumbs_down
                        }),
                        colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.tertiary),
                        contentDescription = "dislike"
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                // The current count of downvotes.
                Text(
                    text = "${gotTrackRating.numNegativeVotes}",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
    LaunchedEffect(key1 = gotTrackRating.yourRating, block = {
        // Every time your rating changes, we'll set the buttons to enabled.
        ratingButtonsEnabled = true
    })
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
            gotTrackDetail = TrackDetailUiState.GotTrackDetail(
                track = ExampleData.getExampleTrack(
                    description = "One of the better tracks in the Melbourne area. A few hairpins and other sharp turns. Caution! Speed is very limited here."
                ),
                trackPath = ExampleData.getExampleTrackPath(),
                gotTrackRating = TrackRatingUiState.GotTrackRating(
                    trackUid = ExampleData.getExampleTrack().trackUid,
                    numPositiveVotes = 3,
                    numNegativeVotes = 1,
                    yourRating = true
                )
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
            gotTrackRating = TrackRatingUiState.GotTrackRating(
                trackUid = ExampleData.getExampleTrack().trackUid,
                numPositiveVotes = 3,
                numNegativeVotes = 1,
                yourRating = true
            ),
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
    HawkSpeedTheme {
        TrackReviews(
            track = ExampleData.getExampleTrack(),
            gotTrackRating = TrackRatingUiState.GotTrackRating(
                trackUid = ExampleData.getExampleTrack().trackUid,
                numPositiveVotes = 3,
                numNegativeVotes = 1,
                yourRating = true
            ),
            commentFlow = emptyFlow()
        )
    }
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