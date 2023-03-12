package com.vljx.hawkspeed.view.world

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.databinding.FragmentWorldErrorBinding
import com.vljx.hawkspeed.view.base.BaseFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [WorldErrorFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class WorldErrorFragment : BaseFragment<FragmentWorldErrorBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWorldErrorBinding
        get() = FragmentWorldErrorBinding::inflate

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
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment WorldErrorFragment.
         */
        @JvmStatic
        fun newInstance() =
            WorldErrorFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}