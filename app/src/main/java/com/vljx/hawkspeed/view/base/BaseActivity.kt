package com.vljx.hawkspeed.view.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding

abstract class BaseActivity<ViewBindingCls: ViewDataBinding>: AppCompatActivity() {
    protected lateinit var viewBinding: ViewBindingCls
    protected abstract val activityLayoutId: Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, activityLayoutId)
        viewBinding.lifecycleOwner = this
    }
}