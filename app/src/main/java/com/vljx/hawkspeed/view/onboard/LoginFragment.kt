package com.vljx.hawkspeed.view.onboard

import android.content.Intent
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
import com.vljx.hawkspeed.databinding.FragmentLoginBinding
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.Account.Companion.ARG_ACCOUNT
import com.vljx.hawkspeed.domain.requests.LoginRequest
import com.vljx.hawkspeed.presenter.onboard.LoginPresenter
import com.vljx.hawkspeed.view.MainActivity
import com.vljx.hawkspeed.view.base.BaseFragment
import com.vljx.hawkspeed.viewmodel.onboard.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class LoginFragment : BaseFragment<FragmentLoginBinding>(), LoginPresenter {
    private val loginViewModel: LoginViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLoginBinding
        get() = FragmentLoginBinding::inflate

    override fun loginClicked(loginRequest: LoginRequest) {
        loginViewModel.login(loginRequest)
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
            mViewBinding.loginViewModel = loginViewModel
            mViewBinding.loginPresenter = this@LoginFragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup a collection for the login result. On success, we should move to the main activity.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                loginViewModel.loginResult.collectLatest { loginResource ->
                    when(loginResource.status) {
                        Resource.Status.SUCCESS -> {
                            // Get the Account.
                            val account: Account = loginResource.data
                                ?: throw NotImplementedError("Failed to complete login process - account was not given by loginResource (LoginFragment)")
                            // Here, we have successfully logged in, and this should also be recorded to the database. With the account object given, we will launch
                            // the MainActivity. This activity will handle any account setup issues.
                            val mainActivityIntent = Intent(requireContext(), MainActivity::class.java).apply {
                                putExtra(ARG_ACCOUNT, account)
                            }
                            // Start the activity.
                            startActivity(mainActivityIntent)
                        }
                        Resource.Status.LOADING -> {

                        }
                        Resource.Status.ERROR -> handleLoginError(loginResource.resourceError!!)
                    }
                }
            }
        }
        // Now, check our current login, this will trigger either the login successful or login error clauses above.
        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.attemptCheckLogin()
        }
    }

    private fun handleLoginError(resourceError: ResourceError) {
        when(resourceError.apiError?.errorInformation?.get("error-code")) {
            "bad-auth-header" -> {
                // This means there is no previous sessions or logins from this device.
                return
            }
        }
        // TODO: implement error handling when login result emits ERROR.
        throw NotImplementedError()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @return A new instance of fragment LoginFragment.
         */
        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}