package com.vljx.hawkspeed.adapter

import androidx.databinding.ViewDataBinding
import androidx.databinding.library.baseAdapters.BR
import androidx.recyclerview.widget.RecyclerView
import com.vljx.hawkspeed.models.ListItemViewModel

class DataBindingViewHolder(
    val binding: ViewDataBinding
): RecyclerView.ViewHolder(binding.root) {
    fun bind(itemViewModel: ListItemViewModel) {
        throw NotImplementedError()
        //binding.setVariable(BR.itemViewModel, itemViewModel)
        //binding.executePendingBindings()
    }

    fun bindPresenter(presenterObj: Any) {
        throw NotImplementedError()
        //binding.setVariable(BR.itemPresenter, presenterObj)
    }
}