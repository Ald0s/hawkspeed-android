package com.vljx.hawkspeed.view.world

import android.content.*
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.WorldService.Companion.ARG_WORLD_STATUS
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.databinding.FragmentWorldMapBinding
import com.vljx.hawkspeed.models.world.Viewport
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.models.world.WorldInitial.Companion.ARG_WORLD_INITIAL
import com.vljx.hawkspeed.presenter.world.WorldMapPresenter
import com.vljx.hawkspeed.util.Extension.getEnumExtra
import com.vljx.hawkspeed.view.base.BaseFragment
import com.vljx.hawkspeed.view.base.BaseWorldMapFragment
import com.vljx.hawkspeed.viewmodel.world.WorldMapViewModel
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

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

    private lateinit var statusChangedReceiver: WorldStatusChangedReceiver

    override fun getSupportMapFragment(): SupportMapFragment =
        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Instantiate the receivers.
        statusChangedReceiver = WorldStatusChangedReceiver()
        arguments?.let {
            // TODO: read the connection response here. This will be a basic snapshot of the world at connection time.
        }
    }

    override fun makeNewTrackClicked() {
        findNavController().navigate(R.id.action_destination_world_coordinator_to_destination_record_track)
    }

    override fun onStart() {
        // Register all receivers for world updates.
        LocalBroadcastManager.getInstance(requireContext()).apply {
            registerReceiver(statusChangedReceiver, IntentFilter(WorldService.ACTION_WORLD_STATUS))
        }
        super.onStart()
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

    override fun onMapReady(p0: GoogleMap) {
        super.onMapReady(p0)
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
        TODO("Not yet implemented")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun handleWorldJoined(initial: WorldInitial) {
        Timber.d("Successfully joined and connected to HawkSpeed world!")
        worldMapViewModel.setLoading(false)
    }

    private inner class WorldStatusChangedReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            try {
                when(val status: WorldService.WorldStatus? = intent?.getEnumExtra<WorldService.WorldStatus>(ARG_WORLD_STATUS)) {
                    WorldService.WorldStatus.CONNECTING -> {
                        // TODO: we can read a status string from here.
                        Timber.d("Connecting to game server...")
                        worldMapViewModel.setLoading(true)
                    }
                    WorldService.WorldStatus.JOINED -> {
                        val worldInitial: WorldInitial = intent.getParcelableExtra(ARG_WORLD_INITIAL)
                            ?: throw NullPointerException("No InitialWorld instance sent from service.")
                        handleWorldJoined(worldInitial)
                    }
                    WorldService.WorldStatus.UPDATE -> {
                        //val worldUpdate: WorldUpdate = intent.getParcelableExtra(WorldService.ARG_WORLD_UPDATE)
                        //    ?: throw NullPointerException("No WorldUpdate instance sent from service.")
                        Timber.d("Received a world update event!")
                    }
                    WorldService.WorldStatus.LOCATION -> {
                        //val location: Location = intent.getParcelableExtra(WorldService.ARG_LOCATION)
                        //    ?: throw NullPointerException("No Location instance sent from service.")
                        Timber.d("Received a location update event!")
                    }
                    WorldService.WorldStatus.ERROR -> {
                        throw NotImplementedError("WorldState ERROR is not implemented!")
                    }
                    WorldService.WorldStatus.LEFT -> {
                        // TODO: we will receive errors that relate to the inability to connect/authenticate with the remote server, but also shutdowns/interruptions to the world connection.
                        Timber.d("Received a WorldService LEFT event!")
                        worldMapViewModel.setLoading(false)
                        // TODO: from here, we can call handleError if need be.
                    }
                    else -> { throw NotImplementedError("No such world service status: $status") }
                }
            } catch(e: Exception) {
                Timber.e(e)
            }
        }
    }

    override fun onStop() {
        // Unregister all receivers for world updates.
        LocalBroadcastManager.getInstance(requireContext()).apply {
            unregisterReceiver(statusChangedReceiver)
        }
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
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