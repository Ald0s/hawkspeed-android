package com.vljx.hawkspeed.data.pagingsource

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.base.Paged
import timber.log.Timber

abstract class BasePagingSource<PageModel: Paged, Model: Any>: PagingSource<Int, Model>() {
    /**
     * Perform a request for the given page number of model.
     */
    protected abstract suspend fun performRequest(pageNumber: Int): Resource<PageModel>

    /**
     * Parse the page model and return a list of the model types.
     */
    protected abstract suspend fun returnPageFrom(pageModel: PageModel): List<Model>

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Model> {
        try {
            // Get the next page number, if null, just use one.
            val nextPageNumber: Int = params.key ?: 1
            // Now, perform our request, which will return a resource for the type model given above.
            val pageResource: Resource<PageModel> = performRequest(nextPageNumber)
            // If the request was not successful, we will now fail.
            if(pageResource.status != Resource.Status.SUCCESS) {
                // TODO: integrate the resourceError found in pageResource to be thrown with the exception here.
                Timber.e("Failed to load page #$nextPageNumber for paged request, error response type.")
                Timber.e(pageResource.resourceError?.errorSummary)
                throw Exception("Failed to load page #$nextPageNumber for paged request, error response type.")
            }
            // Get the paged model.
            val pageModel: PageModel = pageResource.data!!
            // Otherwise, return a load result for this page.
            val page: List<Model> = returnPageFrom(pageModel)
            return LoadResult.Page(
                data = page,
                prevKey = null,
                nextKey = pageModel.nextPage
            )
        } catch(e: Exception) {
            Timber.e(e)
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Model>): Int? {
        return null
    }
}