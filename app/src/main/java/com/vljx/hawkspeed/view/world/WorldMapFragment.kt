package com.vljx.hawkspeed.view.world

import android.annotation.SuppressLint
import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.databinding.FragmentWorldMapBinding
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.models.world.Viewport
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.presenter.world.WorldMapPresenter
import com.vljx.hawkspeed.view.base.BaseWorldMapFragment
import com.vljx.hawkspeed.viewmodel.world.WorldMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 * Use the [WorldMapFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class WorldMapFragment : BaseWorldMapFragment<FragmentWorldMapBinding>(), WorldMapPresenter {
    // TODO: we use activityViewModels instead of viewModels otherwise, when we navigate, this is reset.
    private val worldMapViewModel: WorldMapViewModel by activityViewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWorldMapBinding
        get() = FragmentWorldMapBinding::inflate

    override fun getSupportMapFragment(): SupportMapFragment =
        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

    // Map a track hash, to a pair of the track and its marker.
    private val trackMap: MutableMap<Track, Marker> = mutableMapOf()
    // Map a track hash, to a pair of the track and its polyline.
    private val trackPathMap: MutableMap<Track, Polyline> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Instantiate the receivers.
        arguments?.let {
            // TODO: read the connection response here. This will be a basic snapshot of the world at connection time.
        }
    }

    override fun makeNewTrackClicked() {
        findNavController().navigate(R.id.action_destination_world_coordinator_to_destination_record_track)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            mViewBinding.worldMapViewModel = worldMapViewModel
            mViewBinding.worldMapPresenter = this@WorldMapFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Start collection of world objects from the database.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Collect indication that we can actually race a track.
                launch {
                    worldMapViewModel.canRaceOn.collectLatest { track ->
                        if(track != null) {
                            Timber.d("We are able to race the following track; $track")
                            /**
                             * TODO: this is where we can display a pop up UI of some description offering a race for this Player.
                             * For example, display a modal bottom sheet dialog, which is collapsed at say, 15% of the screen, and will popup to review info about the trace and
                             * offer the UI on which we can accept an offer to race.
                             */
                        } else {
                            Timber.d("We are not able to race any track currently.")
                            /**
                             * TODO: clear any UI created by the track-not-none handler.
                             */
                        }
                    }
                }
                // Collect all tracks.
                launch {
                    worldMapViewModel.tracksWithPaths.collectLatest { latestTracksWithPaths ->
                        /**
                         * TODO: improve this greatly.
                         * We should relocate this entire logic pattern to a separate component, and for each entity.
                         */
                        googleMap?.apply {
                            Timber.d("There are now ${latestTracksWithPaths.size} cached tracks to display on map.")
                            // We will now update the tracks polylines.
                            // TODO: should viewmodel actually map tracks to MarkerOptions, which we then use to create/update existing tracks?
                            // For each track in latest tracks, we'll update markers.
                            latestTracksWithPaths.forEach { trackWithPath ->
                                val markerOptions: MarkerOptions = MarkerOptions()
                                    .position(LatLng(trackWithPath.track.startPoint.latitude, trackWithPath.track.startPoint.longitude))
                                // Get the current track -> marker pair from the map.
                                val existingTrackMarker: Marker? = trackMap[trackWithPath.track]
                                // If this is null, create one and add it. Otherwise, update it.
                                if(existingTrackMarker == null) {
                                    trackMap[trackWithPath.track] = googleMap!!.addMarker(markerOptions)
                                        ?: throw NotImplementedError("Marker could not be added, and this is not handled.")
                                } else {
                                    existingTrackMarker.position = markerOptions.position
                                }

                                val existingTrackPolyline: Polyline? = trackPathMap[trackWithPath.track]
                                if(trackWithPath.path != null) {
                                    val polylineOptions: PolylineOptions = PolylineOptions()
                                        .addAll(trackWithPath.path!!.points.map { LatLng(it.latitude, it.longitude) })
                                    // If this is null, create one and add it. Otherwise, update it.
                                    if(existingTrackPolyline == null) {
                                        trackPathMap[trackWithPath.track] = googleMap!!.addPolyline(polylineOptions)
                                    } else {
                                        existingTrackPolyline.points = polylineOptions.points
                                    }
                                } else existingTrackPolyline?.remove()
                            }
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        super.onMapReady(p0)
        p0.isMyLocationEnabled = true
        // Immediately use the bound service to join the world.
        arguments?.let {
            // TODO: we can read variables from the arguments here that can be used when joining the world.
        }
        // Get the current viewport.
        val viewport: Viewport = getCurrentViewport()
            ?: throw NotImplementedError("onMapReady failed! We could not get the current viewport!")
        Timber.d("Map is now READY! We will now request that the world service initiate the connection procedure.")
        // Join the world when the view is created, with the bounding box and current zoom.
        mWorldService.joinWorld(viewport)
    }

    /**
     * A marker has been clicked on the map. Usually, this means the user has requested a preview version of whatever type has been clicked.
     * For example, if the user has tapped a Player's marker, a player preview model fragment must be shown that shows the player's user in
     * summary, as well as their current vehicle.
     */
    override fun onMarkerClick(p0: Marker): Boolean {
        trackMap.asIterable().forEach {
            if(it.value == p0) {
                worldMapViewModel.getTrackPath(it.key)
                return true
            }
        }
        return false
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment WorldMapFragment.
         */
        @JvmStatic
        fun newInstance() =
            WorldMapFragment().apply {
                arguments = Bundle().apply {
                    // TODO: this should receive the join response.
                }
            }
    }
}