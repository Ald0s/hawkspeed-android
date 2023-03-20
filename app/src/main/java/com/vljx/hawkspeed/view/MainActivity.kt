package com.vljx.hawkspeed.view

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.WorldService
import com.vljx.hawkspeed.data.socket.WorldSocketSession
import com.vljx.hawkspeed.databinding.ActivityMainBinding
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.Account.Companion.ARG_ACCOUNT
import com.vljx.hawkspeed.view.base.BaseAuthenticatedActivity
import com.vljx.hawkspeed.view.setup.SetupActivity
import com.vljx.hawkspeed.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : BaseAuthenticatedActivity<ActivityMainBinding>() {
    private val mainViewModel: MainViewModel by viewModels()

    @Inject
    lateinit var worldSocketSession: WorldSocketSession

    override val activityLayoutId: Int
        get() = R.layout.activity_main

    private lateinit var navController: NavController

    private lateinit var worldService: WorldService
    private var isServiceBound: Boolean = false

    private val serviceConnection = object: ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, service: IBinder?) {
            val worldServiceBinder = service as WorldService.WorldServiceBinder
            worldService = worldServiceBinder.getService()
            isServiceBound = true
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isServiceBound = false
        }
    }

    private val solveAccountIssuesResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { resultIntent ->
        if(resultIntent.resultCode != RESULT_OK) {
            // TODO: Should not be able to exit out of this activity.
            throw NotImplementedError("Premature exit out of SetupActivity is not yet handled at all.")
        }
        val resultingAccount: Account = resultIntent.data?.getParcelableExtra(ARG_ACCOUNT)
            ?: throw NotImplementedError("Solving account issues failed since RESULT_OK was given, but no resulting Account was given!")
        // Now, ensure we've solved all issues.
        if(resultingAccount.anySetupOrCompletionRequired) {
            // TODO: handle if setup activity has somehow closed, but has not solved the issues required.
            throw NotImplementedError("sovleAccountIssueResult was closed but account issues are not yet resolved.")
        }
        // Set account valid, so it can be used.
        accountValid()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the nav controller.
        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        // Now, we need to valid the incoming account, to ensure there are no further issues that require resolving. So we won't start the foreground service straight away.
        // Get the account from our intent, raising an exception if none given.
        val account: Account = intent.getParcelableExtra(ARG_ACCOUNT)
            ?: throw NullPointerException("Could not start main activity - no account provided!")
        Timber.d("Started main activity with account $account")
        // With a new account given, we must first check whether there is any setup or other work like that to be done. If any work is required, we'll launch the setup activity now.
        if (account.anySetupOrCompletionRequired) {
            // One or more setup procedures is required, so now launch the setup activity with the given account; the handler defined above will deal with the outcome of this activity,
            // including whether or not to allow user to proceed to inner app.
            solveAccountIssuesResult.launch(Intent(this, SetupActivity::class.java).apply {
                putExtra(ARG_ACCOUNT, account)
            })
        } else {
            // Account assumed valid, we can now start the service and move from loading to the world coordinator fragment.
            accountValid()
        }
    }

    /**
     * On start, we bind to the service to gain a service connection.
     */
    override fun onStart() {
        super.onStart()
        // We'll always bind to the service on start, but the service is not doing much, and will not receive any start commands.
        bindService(
            Intent(this, WorldService::class.java),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    /**
     * On service, we differentiate between a finishing stop call and a non-finishing stop call. If the activity is finishing, the world service will also be stopped.
     * Otherwise, for all other purposes, we will merely unbind from the service, as its assumed the activity will come back and this activity (for now) does not require
     * active updates from the world service for any other reason.
     */
    override fun onDestroy() {
        if(isFinishing) {
            // If activity is finishing, we can stop the service completely.
            // Only actually communicate with the service if it is bound, otherwise log a warning.
            if(isServiceBound) {
                Timber.d("MainActivity has been asked to finish. We will now stop the world service as well.")
                worldService.stopWorldService()
            } else {
                Timber.w("MainActivity has been asked to finish, but we are not bound to the world service. We will not be able to gracefully shutdown the service.")
            }
        }
        // Unbind our connection to the service, as well.
        unbindService(serviceConnection)
        super.onDestroy()
    }

    /**
     *
     */
    private fun accountValid() {
        // Build an intent for, and start the world service now within the activity.
        val worldServiceIntent = Intent(this, WorldService::class.java)
        startForegroundService(worldServiceIntent)
    }
}