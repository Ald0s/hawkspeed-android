package com.vljx.hawkspeed.view.onboard

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.vljx.hawkspeed.R
import com.vljx.hawkspeed.databinding.ActivityOnboardBinding
import com.vljx.hawkspeed.view.base.BaseActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OnboardActivity : BaseActivity<ActivityOnboardBinding>() {
    override val activityLayoutId: Int
        get() = R.layout.activity_onboard

    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Save the nav controller.
        navController = (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).navController
    }
}