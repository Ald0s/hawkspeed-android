package com.vljx.hawkspeed.view.base

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.vljx.hawkspeed.adapter.DataBindingPagingRecyclerAdapter
import com.vljx.hawkspeed.models.ListItemViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

abstract class BaseViewListFragment<ViewBindingCls: ViewBinding>: BaseFragment<ViewBindingCls>() {
    /**
     * Provide a flow for the paging data's flow. This is required.
     */
    protected abstract val pageViewModelFlow: Flow<PagingData<ListItemViewModel>>

    /**
     * Provide a paging recycler adapter for the view model type we wish to page, so custom presenters can be attached. This is required.
     */
    protected abstract val dataBindingPagingRecyclerAdapter: DataBindingPagingRecyclerAdapter

    /**
     * Provide a function that will setup the desired recycler view and return it as per specifications. This is required.
     */
    protected abstract fun setupPagingRecyclerView(viewBindingCls: ViewBindingCls): RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Setup the paging recycler view and return it.
        setupPagingRecyclerView(mViewBinding).apply {
            adapter = dataBindingPagingRecyclerAdapter
        }
        // Setup a collection for the view model flow here, and submit all to the data binding adapter.
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                pageViewModelFlow.collectLatest { pagingData ->
                    dataBindingPagingRecyclerAdapter.submitData(pagingData)
                }
            }
        }
        // Setup a load state listener to update the UI.
        dataBindingPagingRecyclerAdapter.apply {
            addLoadStateListener { loadState ->
                if(loadState.refresh is LoadState.NotLoading) {
                    // TODO: No longer refreshing.
                }
                if(
                    loadState.source.refresh is LoadState.NotLoading &&
                    loadState.append.endOfPaginationReached &&
                    dataBindingPagingRecyclerAdapter.itemCount == 0
                ) {
                    // TODO: placeholder needed.
                } else {
                    // TODO: no placeholder needed.
                }
            }
        }
    }
}