package com.vljx.hawkspeed.view.setup

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.databinding.ActivitySetupBinding
import com.vljx.hawkspeed.domain.models.account.Account
import com.vljx.hawkspeed.domain.models.account.Account.Companion.ARG_ACCOUNT
import com.vljx.hawkspeed.view.base.BaseAuthenticatedActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SetupActivity : BaseAuthenticatedActivity<ActivitySetupBinding>(), SetupCallback {

    private lateinit var navController: NavController

    override val activityLayoutId: Int
        get() = R.layout.activity_setup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get the given Account instance, or fail doing so.
        val account: Account = intent.getParcelableExtra(ARG_ACCOUNT)
            ?: throw Exception("Failed to create SetupActivity. An ARG_ACCOUNT is required!")
        // Get the nav controller for this activity.
        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
        // Now, check which destination we need to display for what needs to be solved.
        resolveAccountIssues(account)
    }

    override fun resolveAccountIssues(account: Account) {
        // If no setup or completion required, we're all good to go! Set our result as OK and enter into the intent the Account.
        if(!account.anySetupOrCompletionRequired) {
            // Set result intent and code.
            setResult(RESULT_OK, Intent().apply {
                putExtra(ARG_ACCOUNT, account)
            })
            finish()
        } else {
            // TODO: implement both isVerified and isPasswordVerified handlers.
            val nextDestination: Int = when {
                !account.isVerified -> throw NotImplementedError("isVerified is not implemented.")
                !account.isPasswordVerified -> throw NotImplementedError("isPasswordVerified is not implemented.")
                !account.isProfileSetup -> R.id.destination_setup_profile
                else -> throw NotImplementedError("Account is apparently already resolved.")
            }
            // Now, navigate to the next destination, popping inclusive up until the destination.
            navController.navigate(
                nextDestination,
                null,
                NavOptions.Builder()
                    .setPopUpTo(nextDestination, true)
                    .build()
            )
        }
    }
}