package com.vljx.hawkspeed.view.track

import android.location.Location
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.databinding.FragmentRaceTrackBinding
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.domain.models.track.Track.Companion.ARG_TRACK
import com.vljx.hawkspeed.domain.models.track.TrackPath
import com.vljx.hawkspeed.domain.models.track.TrackPoint
import com.vljx.hawkspeed.models.track.RaceCountdownState
import com.vljx.hawkspeed.models.track.RaceState
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.presenter.track.RaceTrackPresenter
import com.vljx.hawkspeed.view.base.BaseFollowWorldMapFragment
import com.vljx.hawkspeed.viewmodel.track.RaceTrackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [RaceTrackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class RaceTrackFragment : BaseFollowWorldMapFragment<FragmentRaceTrackBinding>(), RaceTrackPresenter {
    private val raceTrackViewModel: RaceTrackViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRaceTrackBinding
        get() = FragmentRaceTrackBinding::inflate

    override fun getSupportMapFragment(): SupportMapFragment =
        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

    private var raceTrackStartPoint: Marker? = null
    private var raceTrackPolyline: Polyline? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Get the track passed through, raise if none given.
            raceTrackViewModel.selectTrack(
                it.getParcelable(ARG_TRACK)
                    ?: throw NotImplementedError("Failed to create RaceTrackFragment - no track given!")
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            mViewBinding.raceTrackViewModel = raceTrackViewModel
            mViewBinding.raceTrackPresenter = this@RaceTrackFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect the actual race state.
                launch {
                    raceTrackViewModel.raceState.collectLatest { raceState ->
                        when(raceState) {
                            is RaceState.Racing -> {
                                // Race has begun! Inform the server!
                                raceStarted(raceState)
                            }
                            else -> { }
                        }
                    }
                }
                // Collect the countdown state.
                launch {
                    raceTrackViewModel.raceCountdownState.collectLatest { raceCountdownState ->
                        // TODO: display this somehow.
                        when(raceCountdownState) {
                            is RaceCountdownState.GetReady -> {
                                Timber.d("Race started... GET READY!")
                            }
                            is RaceCountdownState.OnCount -> {
                                Timber.d("In ${raceCountdownState.currentSecond} ...")
                            }
                            is RaceCountdownState.Go -> {
                                Timber.d("GO GO GO!")
                            }
                            else -> { }
                        }
                    }
                }
                // Collect the selected track.
                launch {
                    raceTrackViewModel.selectedTrack.collectLatest { track ->
                        drawStartPoint(track.startPoint)
                    }
                }
                // Collect the selected track's path.
                launch {
                    raceTrackViewModel.selectedTrackPath.collectLatest { trackPath ->
                        drawRaceTrack(trackPath)
                    }
                }
            }
        }
    }

    override fun onMapReady(p0: GoogleMap) {
        super.onMapReady(p0)
    }

    override fun newLocationReceived(location: Location) {
        super.newLocationReceived(location)
        // Each time the location is updated, send it to the view model.
        raceTrackViewModel.updateLocation(location)
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        TODO("Not yet implemented")
    }

    private fun raceStarted(racing: RaceState.Racing) {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // With this state, let the world service know of our intent. This will succeed silently, or will fail via exception.
                mWorldService.startNewRace(racing.trackUid, racing.countdownStartedAt)
            } catch(e: Exception) {
                // TODO: catch & handle FalseStartException here!
                throw NotImplementedError("Implement handler(s) for raceStarted in RaceTrackFragment!")
            }
        }
    }

    private fun drawStartPoint(trackPoint: TrackPoint) {
        // Only proceed with this if google map is not null.
        googleMap?.apply {
            // Create a latlng object for the position.
            val startLatLng = LatLng(trackPoint.latitude, trackPoint.longitude)
            // Now, if marker exists, simply change the position. Otherwise, add it.
            if(raceTrackStartPoint != null) {
                raceTrackStartPoint!!.position = startLatLng
            } else {
                // Create the marker.
                val markerOptions = MarkerOptions()
                    .position(startLatLng)
                raceTrackStartPoint = addMarker(markerOptions)
            }
        }
    }

    private fun drawRaceTrack(trackPath: TrackPath) {
        // Only proceed with this if google map is not null.
        googleMap?.apply {
            // Get a list of lat longs for the points.
            val positions: List<LatLng> = trackPath.points.map { trackPoint ->
                LatLng(trackPoint.latitude, trackPoint.longitude)
            }
            // If the polyline is null, create it. Otherwise, simply update the points.
            if(raceTrackPolyline != null) {
                raceTrackPolyline!!.points = positions
            } else {
                val raceTrackOptions = PolylineOptions()
                    .addAll(positions)
                raceTrackPolyline = addPolyline(raceTrackOptions)
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment RaceTrackFragment.
         */
        @JvmStatic
        fun newInstance(track: Track) =
            RaceTrackFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(ARG_TRACK, track)
                }
            }
    }
}