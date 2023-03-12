package com.vljx.hawkspeed.view.base

import android.os.Bundle
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.authentication.AuthenticationState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

abstract class BaseAuthenticatedActivity<ViewBindingCls: ViewDataBinding>: BaseActivity<ViewBindingCls>() {
    @Inject
    lateinit var authenticationSession: AuthenticationSession

    /*private val globalApiReceiver = GlobalApiReceiver(object: GlobalProcedure {
        override fun accountRequiresVerification(errorInformation: HashMap<String, String>) {
            TODO("Not yet implemented")
        }

        override fun passwordRequiresVerification(errorInformation: HashMap<String, String>) {
            TODO("Not yet implemented")
        }

        override fun socialProfileRequiresSetup(errorInformation: HashMap<String, String>) {
            TODO("Not yet implemented")
        }

        override fun gameConfigurationRequired(errorInformation: HashMap<String, String>) {
            TODO("Not yet implemented")
        }
    })*/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Register the API receiver.
        //registerReceiver(globalApiReceiver, IntentFilter(ACTION_GLOBAL_API_ERROR))

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Setup a collection for the current account flow in account authentication session. This will allow us to determine whether the
                // user has logged out, changed user etc.
                launch {
                    authenticationSession.authenticationState.collectLatest { authenticationState ->
                        when(authenticationState) {
                            is AuthenticationState.Authenticated -> {
                                // User is authenticated, may have changed.
                                Timber.d("User ${authenticationState.userName} is AUTHENTICATED!")
                            }
                            is AuthenticationState.NotAuthenticated -> {
                                // User is no longer authenticated, there may be a good reason why.
                                Timber.w("Activity authentication changed to unauthenticated, closing down now.")
                                handleStateUnauthenticated(authenticationState)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * A handle for whenever the state changes to unauthenticated. This can potentially result in a shutdown and exit of the current
     * view or collection of views.
     */
    private fun handleStateUnauthenticated(authenticationState: AuthenticationState.NotAuthenticated) {
        // No specifically handled error. Just finish with reason unauthenticated.
        setResult(RESULT_UNAUTHENTICATED)
        // Now, check if we have a special case.
        /*when(authenticationState.apiError?.name) {
            "account-issue" -> {
                // There are some special cases for device issue.
                when(authenticationState.apiError?.errorInformation?.get("error-code")) {
                    "disabled" -> {
                        // The user's account is disabled, so set the result to unauthenticated because disabled.
                        setResult(RESULT_UNAUTHENTICATED_DISABLED)
                    }
                }
            }
        }*/
        // Always finish after setting actual result.
        finish()
    }

    override fun onStop() {
        // Unregister our receiver here.
        // TODO: this can be overriden and disabled on subclasses if it is necessary for the receiver to remain registered.
        //unregisterReceiver(globalApiReceiver)
        super.onStop()
    }

    companion object {
        const val RESULT_UNAUTHENTICATED_DISABLED = -11
        const val RESULT_UNAUTHENTICATED = -10
    }
}