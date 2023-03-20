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
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.databinding.FragmentRecordTrackBinding
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.track.Track
import com.vljx.hawkspeed.draft.track.RecordedPointDraft
import com.vljx.hawkspeed.draft.track.TrackDraft
import com.vljx.hawkspeed.presenter.track.RecordTrackPresenter
import com.vljx.hawkspeed.view.base.BaseFollowWorldMapFragment
import com.vljx.hawkspeed.viewmodel.track.RecordTrackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [RecordTrackFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class RecordTrackFragment : BaseFollowWorldMapFragment<FragmentRecordTrackBinding>(),
    RecordTrackPresenter {
    private val recordTrackViewModel: RecordTrackViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecordTrackBinding
        get() = FragmentRecordTrackBinding::inflate

    private var recordedTrackPolyline: Polyline? = null

    override fun getSupportMapFragment(): SupportMapFragment =
        childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

    override fun useRecordedTrackClicked(recordedPoints: List<RecordedPointDraft>) {
        // Set this as the recorded track in view model, this should then change the UI to require track details.
        recordTrackViewModel.useRecordedTrack(recordedPoints)
    }

    override fun recordTrackClicked() {
        recordTrackViewModel.recordTrack()
    }

    override fun stopRecordingClicked() {
        recordTrackViewModel.stopRecording()
    }

    override fun resetRecordingClicked() {
        recordTrackViewModel.resetRecordedTrack()
    }

    override fun createNewTrack(trackDraft: TrackDraft?) {
        if(trackDraft == null) {
            return
        }
       // Now with the track draft, submit this to the server.
        recordTrackViewModel.submitTrack(trackDraft)
    }

    override fun backToRecordingTrack() {
        recordTrackViewModel.backToRecordingTrack()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            mViewBinding.recordTrackViewModel = recordTrackViewModel
            mViewBinding.recordTrackPresenter = this@RecordTrackFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup collections.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Setup a collection on the new track result.
                launch {
                    recordTrackViewModel.newTrackResult.collectLatest { trackResource ->
                        when(trackResource.status) {
                            Resource.Status.SUCCESS -> {
                                // On success, we now have a Track instance.
                                // TODO: newTrackResult status SUCCESS without valid Track instance returned is NOT HANDLED.
                                val track: Track = trackResource.data
                                    ?: throw NotImplementedError("newTrackResult status SUCCESS without valid Track instance returned is NOT HANDLED.")
                                // We can now navigate back to the world map fragment.
                                findNavController().navigate(R.id.action_destination_record_track_to_destination_world_map)
                            }
                            Resource.Status.LOADING -> { }
                            Resource.Status.ERROR -> {
                                /**
                                 * TODO: handle this track resource error.
                                 */
                                throw NotImplementedError("RecordTrackFragment resource ERROR outcome is not handled.")
                            }
                        }
                    }
                }
                // Setup a collection on the recorded points, this will display the track that has been recorded.
                launch {
                    recordTrackViewModel.recordedPoints.collectLatest { recordedPointDrafts ->
                        updateRecordedTrack(recordedPointDrafts)
                    }
                }
                // Setup a collection on the recorded track draft. If this is NOT null, we want to center & lock the viewport on the recorded track.
                launch {
                    recordTrackViewModel.recordedTrackDraft.collectLatest { recordedTrackDraft ->
                        if(recordedTrackDraft != null) {
                            /**
                             * TODO: lock the viewport on this track.
                             */
                        } else {
                            /**
                             * TODO: lock the viewport on the User instead.
                             */
                        }
                    }
                }
            }
        }
    }

    override fun onMarkerClick(p0: Marker): Boolean {
        TODO("Not yet implemented")
    }

    protected override fun newLocationReceived(location: Location) {
        super.newLocationReceived(location)
        googleMap?.apply {
            // Each time we receive a location and map is not null, inform view model about it in case we're recording.
            recordTrackViewModel.locationReceived(location)
        }
    }

    private fun updateRecordedTrack(recordedPointDrafts: List<RecordedPointDraft>?) {
        googleMap?.apply {
            // If recorded point drafts list is null, this means we will remove the polyline from the map.
            if(recordedPointDrafts == null) {
                recordedTrackPolyline?.remove()
                recordedTrackPolyline = null
                return
            }
            // Otherwise, if the list is not null, but polyline is null, we need to create a new one.
            val pointsAsLatLng: List<LatLng> = recordedPointDrafts.map { LatLng(it.latitude, it.longitude) }
            if(recordedTrackPolyline == null) {
                // Create a new polyline options with all recorded point drafts.
                val polylineOptions = PolylineOptions()
                    .addAll(pointsAsLatLng)
                // Now, add this to the map and set the polyline instance.
                recordedTrackPolyline = addPolyline(polylineOptions)
            } else {
                // Otherwise, we have a polyline already. Simply set its points contents.
                recordedTrackPolyline?.points = pointsAsLatLng
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment MakeTrackFragment.
         */
        @JvmStatic
        fun newInstance() =
            RecordTrackFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}