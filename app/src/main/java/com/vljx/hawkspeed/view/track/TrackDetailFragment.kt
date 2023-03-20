package com.vljx.hawkspeed.view.track

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.vljx.hawkspeed.databinding.FragmentTrackDetailBinding
import com.vljx.hawkspeed.view.base.BaseFragment
import com.vljx.hawkspeed.viewmodel.track.TrackDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [TrackDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class TrackDetailFragment : BaseFragment<FragmentTrackDetailBinding>() {
    private val trackDetailViewModel: TrackDetailViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentTrackDetailBinding
        get() = FragmentTrackDetailBinding::inflate

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

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment TrackDetailFragment.
         */
        @JvmStatic
        fun newInstance() =
            TrackDetailFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}