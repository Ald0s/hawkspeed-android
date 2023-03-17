package com.vljx.hawkspeed.view.track

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
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
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.databinding.FragmentRecordTrackBinding
import com.vljx.hawkspeed.draft.track.RecordedPointDraft
import com.vljx.hawkspeed.draft.track.TrackDraft
import com.vljx.hawkspeed.models.world.WorldInitial
import com.vljx.hawkspeed.presenter.track.RecordTrackPresenter
import com.vljx.hawkspeed.util.Extension.getEnumExtra
import com.vljx.hawkspeed.view.base.BaseFollowWorldMapFragment
import com.vljx.hawkspeed.view.base.BaseWorldMapFragment
import com.vljx.hawkspeed.viewmodel.track.RecordTrackViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_record_track.*
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.*

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
        // TODO: Now we must get the track's name and description from the User.
        // TODO: Perhaps, when we focus on UI, turn the framelayout into a modal bottom sheet that, when recording, is at say 25% screen, and when we are finalising details,
        // TODO: we set it to 75% screen, lock the map view to the entire track. When this is all done, we will then call submitRecordedTrack() in the RecordTrackViewModel.
        // TODO: Change this.
        recordTrackViewModel.submitTrack(
            TrackDraft(
                "Example Track",
                "Cool track",
                listOf(
                    RecordedPointDraft(0, -37.774328, 145.214102, 0.0f, 70.0f, 1678253040*1000L),
                    RecordedPointDraft(1, -37.774338, 145.214112, 0.0f, 70.0f, 1678253040*1000L),
                    RecordedPointDraft(2, -37.774349, 145.214122, 0.0f, 70.0f, 1678253040*1000L),
                    RecordedPointDraft(3, -37.774359, 145.214132, 0.0f, 70.0f, 1678253040*1000L)
                )
            )
        )
    }

    override fun recordClicked() {
        recordTrackViewModel.record()
    }

    override fun stopClicked() {
        recordTrackViewModel.stop()
    }

    override fun resetClicked() {
        recordTrackViewModel.reset()
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
        // Setup a collection for the recorded track points, which we will show on the world map.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                recordTrackViewModel.recordedPoints.collect { recordedPointDrafts ->
                    updateRecordedTrack(recordedPointDrafts)
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