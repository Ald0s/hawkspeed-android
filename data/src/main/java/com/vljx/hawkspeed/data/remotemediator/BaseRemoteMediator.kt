package com.vljx.hawkspeed.data.remotemediator

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.vljx.hawkspeed.data.database.AppDatabase
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.base.Paged
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
open class BaseRemoteMediator<EntityModel: Any, DataModel: Paged>(
    private val appDatabase: AppDatabase,
    private val remoteQuery: suspend (loadKey: Int) -> Resource<DataModel>,
    private val upsertQuery: suspend (dataModel: DataModel) -> Unit,
    private val clearAllQuery: suspend (dataModel: DataModel) -> Unit
): RemoteMediator<Int, EntityModel>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, EntityModel>
    ): MediatorResult {
        return try {
            val loadKey = when(loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    // Get number of pages loaded.
                    val numPagesLoaded = state.pages.count()
                    // Get last item or null, if this is null, end of pagination reached.
                    if(state.lastItemOrNull() == null) {
                        return MediatorResult.Success(
                            endOfPaginationReached = true
                        )
                    }
                    // Load key for next page is number of pages loaded now + 1.
                    numPagesLoaded + 1
                }
            }

            // Make a call to the remote resource for the data model.
            val modelResource: Resource<DataModel> = remoteQuery.invoke(loadKey ?: 1)
            // If the request is not successful, throw a remote mediator resource exception.
            if(modelResource.status != Resource.Status.SUCCESS) {
                Timber.e("Failed to call remote query in mediator! A resource error occurred! Summary:\n${modelResource.resourceError?.errorSummary}")
                throw RemoteMediatorResourceException(modelResource.resourceError!!)
            }
            // Otherwise, get the data model itself.
            val model: DataModel = modelResource.data!!
            // Start a transaction with the database.
            appDatabase.withTransaction {
                if(loadType == LoadType.REFRESH) {
                    // If refreshing, clear all current entries from database.
                    clearAllQuery.invoke(model)
                }
                // Finally, call the upsert suspending function to cache the results.
                upsertQuery.invoke(model)
            }
            // Submit a success, if there is no next key received from the server, we will assume pagination is finished.
            MediatorResult.Success(
                endOfPaginationReached = model.nextPage == null
            )
        } catch(e: RemoteMediatorResourceException) {
            MediatorResult.Error(e)
        } catch(e: IOException) {
            MediatorResult.Error(e)
        } catch(e: HttpException) {
            MediatorResult.Error(e)
        }
    }
}