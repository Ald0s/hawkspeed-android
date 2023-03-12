package com.vljx.hawkspeed.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import com.vljx.hawkspeed.models.ListItemViewModel

class DataBindingPagingRecyclerAdapter(
    private val presenterReceiver: Any? = null
): PagingDataAdapter<ListItemViewModel, DataBindingViewHolder>(
    ListItemViewModelComparator
) {
    private val viewTypeToLayoutId: MutableMap<Int, Int> = mutableMapOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataBindingViewHolder =
        DataBindingViewHolder(
            DataBindingUtil.inflate(
                LayoutInflater.from(parent.context),
                viewTypeToLayoutId[viewType] ?: throw Exception("No such layout Id for viewType: $viewType found!"),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: DataBindingViewHolder, position: Int) {
        getItem(position)?.let { itemViewModel ->
            holder.bind(itemViewModel)
            presenterReceiver?.let { p -> holder.bindPresenter(p) }
        }
    }

    override fun getItemViewType(position: Int): Int =
        getItem(position)?.also { item ->
            if(!viewTypeToLayoutId.containsKey(item.viewType)) {
                viewTypeToLayoutId[item.viewType] = item.layoutId
            }
        }?.viewType
            ?: -1

    object ListItemViewModelComparator : DiffUtil.ItemCallback<ListItemViewModel>() {
        override fun areItemsTheSame(oldItem: ListItemViewModel, newItem: ListItemViewModel): Boolean {
            return oldItem.areViewModelsTheSame(newItem)
        }

        override fun areContentsTheSame(oldItem: ListItemViewModel, newItem: ListItemViewModel): Boolean {
            return oldItem.areViewModelContentsTheSame(newItem)
        }
    }
}