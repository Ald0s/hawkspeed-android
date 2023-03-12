package com.vljx.hawkspeed.view.onboard

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
import com.vljx.hawkspeed.databinding.FragmentRegisterBinding
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.requests.RegisterLocalAccountRequest
import com.vljx.hawkspeed.presenter.onboard.RegisterPresenter
import com.vljx.hawkspeed.view.base.BaseFragment
import com.vljx.hawkspeed.viewmodel.onboard.RegisterViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class RegisterFragment : BaseFragment<FragmentRegisterBinding>(), RegisterPresenter {
    private val registerViewModel: RegisterViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRegisterBinding
        get() = FragmentRegisterBinding::inflate

    override fun registerClicked(registrationRequest: RegisterLocalAccountRequest) {
        registerViewModel.attemptRegistration(registrationRequest)
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
        return super.onCreateView(inflater, container, savedInstanceState).also {
            mViewBinding.registerViewModel = registerViewModel
            mViewBinding.registerPresenter = this@RegisterFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup a collection on the registration result.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                registerViewModel.registrationResult.collectLatest { registrationResource ->
                    when(registrationResource.status) {
                        Resource.Status.SUCCESS -> {
                            // TODO: here, we must go to an informative screen letting user know they have successfully registered, or a preliminary 'please verify' page.
                            throw NotImplementedError("SUCCESS registration is not implemented.")
                        }
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.ERROR -> {
                            // TODO: implement error handle when registration fails.
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
         * @return A new instance of fragment RegisterFragment.
         */
        @JvmStatic
        fun newInstance() =
            RegisterFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}