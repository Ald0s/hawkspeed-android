package com.vljx.hawkspeed.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.vljx.hawkspeed.models.ListItemViewModel

/**
 * Huge thanks to the following guide for these cool ideas.
 * https://proandroiddev.com/flexible-recyclerview-adapter-with-mvvm-and-data-binding-74f75caef66a
 */
class DataBindingRecyclerAdapter(
    private val presenterReceiver: Any? = null
): RecyclerView.Adapter<DataBindingViewHolder>() {
    private var currentItems: MutableList<ListItemViewModel> = mutableListOf()
    private val diffCallback: DiffUtil.ItemCallback<ListItemViewModel> = object:
        DiffUtil.ItemCallback<ListItemViewModel>() {
        override fun areItemsTheSame(
            oldItem: ListItemViewModel,
            newItem: ListItemViewModel
        ): Boolean =
            oldItem.areViewModelsTheSame(newItem)

        override fun areContentsTheSame(
            oldItem: ListItemViewModel,
            newItem: ListItemViewModel
        ): Boolean =
            oldItem.areViewModelContentsTheSame(newItem)
    }
    private val asyncListDiffer = AsyncListDiffer(this, diffCallback)
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
        // Get current view model for this postition.
        val listItemViewModel = asyncListDiffer.currentList[position]
        // Bind this view model to the holder and set the presenter.
        holder.bind(listItemViewModel)
        presenterReceiver?.let { p -> holder.bindPresenter(p) }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(latestItems: List<ListItemViewModel>?) {
        if(latestItems == null) {
            currentItems.clear()
        } else {
            currentItems.clear()
            currentItems.addAll(latestItems)
        }
        // Submit the latest items or empty list.
        asyncListDiffer.submitList(currentItems)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int =
        asyncListDiffer.currentList[position].also { item ->
            if(!viewTypeToLayoutId.containsKey(item.viewType)) {
                viewTypeToLayoutId[item.viewType] = item.layoutId
            }
        }.viewType

    override fun getItemCount(): Int = asyncListDiffer.currentList.size
}