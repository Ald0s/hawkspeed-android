package com.vljx.hawkspeed.view.track

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.adapter.DataBindingPagingRecyclerAdapter
import com.vljx.hawkspeed.databinding.FragmentTrackLeaderboardBinding
import com.vljx.hawkspeed.domain.models.track.Track.Companion.ARG_TRACK
import com.vljx.hawkspeed.view.base.BaseFragment
import com.vljx.hawkspeed.viewmodel.track.TrackLeaderboardViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [TrackLeaderboardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class TrackLeaderboardFragment : BaseFragment<FragmentTrackLeaderboardBinding>() {
    private val trackLeaderboardViewModel: TrackLeaderboardViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTrackLeaderboardBinding
        get() = FragmentTrackLeaderboardBinding::inflate

    private val dataBindingPagingRecyclerAdapter = DataBindingPagingRecyclerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            // Select the track in the view model, or raise an exception if none given.
            trackLeaderboardViewModel.selectTrack(
                it.getParcelable(ARG_TRACK)
                    ?: throw NotImplementedError("Failed to create TrackLeaderboardFragment - no Track was given.")
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).apply {
            mViewBinding.trackLeaderboardViewModel = trackLeaderboardViewModel
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup a collection for the track's leaderboard.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                trackLeaderboardViewModel.leaderboard.collectLatest { pagingData ->
                    dataBindingPagingRecyclerAdapter.submitData(pagingData)
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment RaceLeaderboardFragment.
         */
        @JvmStatic
        fun newInstance() =
            TrackLeaderboardFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}