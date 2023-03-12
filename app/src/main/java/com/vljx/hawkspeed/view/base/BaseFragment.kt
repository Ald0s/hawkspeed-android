package com.vljx.hawkspeed.view.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding

abstract class BaseFragment<ViewBindingCls: ViewBinding>: Fragment() {
    private var _binding: ViewBindingCls? = null
    protected val mViewBinding: ViewBindingCls
        get() = _binding!!

    /**
     * Provide the static function that will inflate the required view binding for this fragment.
     * This will be called within onCreateView.
     */
    protected abstract val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> ViewBindingCls

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = bindingInflater(inflater, container, false)
        if(_binding != null && _binding is ViewDataBinding) {
            // Set lifecycle owner for this data binding to this fragment.
            (_binding as ViewDataBinding).lifecycleOwner = viewLifecycleOwner
        }
        return mViewBinding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}