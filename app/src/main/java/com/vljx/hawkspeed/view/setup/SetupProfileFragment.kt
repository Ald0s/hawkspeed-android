package com.vljx.hawkspeed.view.setup

import android.content.Context
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
import com.vljx.hawkspeed.databinding.FragmentSetupProfileBinding
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.requests.SetupProfileRequest
import com.vljx.hawkspeed.presenter.setup.SetupProfilePresenter
import com.vljx.hawkspeed.view.base.BaseFragment
import com.vljx.hawkspeed.viewmodel.setup.SetupProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [SetupProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class SetupProfileFragment : BaseFragment<FragmentSetupProfileBinding>(), SetupProfilePresenter {
    private val setupProfileViewModel: SetupProfileViewModel by viewModels()

    private var setupCallback: SetupCallback? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSetupProfileBinding
        get() = FragmentSetupProfileBinding::inflate

    override fun setupProfileClicked(setupProfileRequest: SetupProfileRequest) {
        setupProfileViewModel.submitProfile(setupProfileRequest)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is SetupCallback) {
            setupCallback = context
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return super.onCreateView(inflater, container, savedInstanceState).also {
            mViewBinding.setupProfileViewModel = setupProfileViewModel
            mViewBinding.setupProfilePresenter = this@SetupProfileFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup a collection for the setup result.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                setupProfileViewModel.setupProfileResult.collectLatest { accountResource ->
                    when(accountResource.status) {
                        Resource.Status.SUCCESS -> {
                            // When we have successfully set the User's profile up, get the account from the resource.
                            val account: Account = accountResource.data
                                ?: throw NotImplementedError("Failed to set User's profile up, no Account was returned in SUCCESS resource.")
                            // We will call our resolve account issues once again for the activity to decide the outcome.
                            setupCallback?.resolveAccountIssues(account)
                        }
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.ERROR -> {
                            // TODO: setup handler on error when setting profile up.
                            throw NotImplementedError()
                        }
                    }
                }
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment SetupProfileFragment.
         */
        @JvmStatic
        fun newInstance() =
            SetupProfileFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}