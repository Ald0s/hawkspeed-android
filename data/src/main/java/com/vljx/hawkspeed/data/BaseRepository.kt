package com.vljx.hawkspeed.data

import android.app.Application
import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceError
import com.vljx.hawkspeed.domain.ResourceImpl
import com.vljx.hawkspeed.domain.authentication.AuthenticationSession
import com.vljx.hawkspeed.domain.exc.ResourceErrorException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import timber.log.Timber
import javax.inject.Inject

abstract class BaseRepository {
    @Inject
    lateinit var application: Application

    @Inject
    lateinit var authenticationSession: AuthenticationSession

    /**
     * Mediate between remote and local data for a specific resource, but return a flow that will emit updated instances
     * of the target query non-stop instead of simply querying for the resource once.
     */
    fun <DataModel, DomainModel> flowQueryFromCacheNetworkAndCache(
        mapper: Mapper<DataModel, DomainModel>,
        databaseQuery: () -> Flow<DataModel?>,
        networkQuery: suspend () -> Resource<DataModel>,
        cacheResult: suspend (DataModel) -> Unit
    ): Flow<Resource<DomainModel>> = flow {
        // Create a flow for a resource of the domain model, to query the network.
        val networkRequestFlow: Flow<Resource<DomainModel>> = flow {
            // Perform the actual remote query now.
            val remoteModelResource: Resource<DataModel> = networkQuery.invoke()
            if(remoteModelResource.status == Resource.Status.ERROR) {
                // If this is NOT successful, create and emit an error on the basis of this resource's error contents.
                remoteModelResource.resourceError?.let { attemptHandleResourceError(it) }
                emit(ResourceImpl(remoteModelResource.status, null, remoteModelResource.resourceError))
            } else {
                // Otherwise, we potentially have a successful result! We'll therefore get the data model version of this type.
                val remoteModel: DataModel? = remoteModelResource.data
                if(remoteModel == null) {
                    Timber.e("Querying for remote model resource returned NULL!")
                    // TODO: implement something smarter here.
                    throw NotImplementedError()
                }
                // We have a proper data model for the required resource, we will first cache this result.
                cacheResult(remoteModel)
                // Finally, we'll map this to a domain level model, and emit a new successful resource containing this.
                val remote: DomainModel = mapper.mapFromData(remoteModel)
                emit(ResourceImpl.success(remote))
            }
        }
        // Here's where we will invoke our database query, returning a flow for the data model. We'll also map this flow immediately to a domain level resource
        // of the desired model type, with the given mapper.
        val cachedModelQueryFlow: Flow<Resource<DomainModel>> = databaseQuery.invoke()
            .filter { nullableDataModel: DataModel? -> nullableDataModel != null }
            .map { dataModel: DataModel? -> ResourceImpl.success(mapper.mapFromData(dataModel!!)) }
        // Finally, we'll emit all from a merge between the network request flow and the cached query flow.
        emitAll(merge(cachedModelQueryFlow, networkRequestFlow))
    }

    /**
     * A convenience function for querying from remote and caching- the database is never checked for any existing resources. This
     * function should be used by create/update type functions that do not need or want any earlier versions of a resource at all.
     */
    fun <DataModel, DomainModel> flowQueryNetworkAndCache(
        mapper: Mapper<DataModel, DomainModel>,
        networkQuery: suspend () -> Resource<DataModel>,
        cacheResult: suspend (DataModel) -> Unit
    ): Flow<Resource<DomainModel>> = flow {
        try {
            // Emit a loading resource.
            emit(ResourceImpl.loading())

            // We will query our remote source for the result. Do this now.
            val remoteModelResource: Resource<DataModel> = networkQuery.invoke()
            if(remoteModelResource.status == Resource.Status.ERROR) {
                // If this is NOT successful, create and emit an error on the basis of this resource's error contents.
                remoteModelResource.resourceError?.let { attemptHandleResourceError(it) }
                emit(ResourceImpl(remoteModelResource.status, null, remoteModelResource.resourceError))
            } else {
                // Otherwise, we potentially have a successful result! We'll therefore get the data model version of this type.
                val remoteModel: DataModel? = remoteModelResource.data
                if(remoteModel == null) {
                    Timber.e("Querying for remote model resource returned NULL!")
                    // TODO: implement something smarter here.
                    throw NotImplementedError()
                }
                // We have a proper data model for the required resource, we will first cache this result.
                cacheResult(remoteModel)
                // Finally, we'll map this to a domain level model, and emit a new successful resource containing this.
                val remote: DomainModel = mapper.mapFromData(remoteModel)
                emit(ResourceImpl.success(remote))
            }
        } catch(ree: ResourceErrorException) {
            // Whenever we get a resource error exception, we should respond by returning a new Resource of type error for the given resource error.
            emit(ResourceImpl(Resource.Status.ERROR, null, ree.resourceError))
        } catch(e: Exception) {
            throw e
        }
    }

    /**
     * Only perform a remote query for a resource, this will not search for any existing resources in the database,
     * nor will it cache any successful results received.
     */
    fun <DataModel, DomainModel> flowQueryNetworkNoCache(
        mapper: Mapper<DataModel, DomainModel>,
        networkQuery: suspend () -> Resource<DataModel>,
    ): Flow<Resource<DomainModel>> = flow {
        // Emit a loading resource.
        emit(ResourceImpl.loading())

        // We will query our remote source for the result. Do this now.
        val remoteModelResource: Resource<DataModel> = networkQuery.invoke()
        if(remoteModelResource.status == Resource.Status.ERROR) {
            // If this is NOT successful, create and emit an error on the basis of this resource's error contents.
            remoteModelResource.resourceError?.let { attemptHandleResourceError(it) }
            emit(ResourceImpl(remoteModelResource.status, null, remoteModelResource.resourceError))
        } else {
            // Otherwise, we potentially have a successful result! We'll therefore get the data model version of this type.
            val remoteModel: DataModel? = remoteModelResource.data
            if(remoteModel == null) {
                Timber.e("Querying for remote model resource returned NULL!")
                // TODO: implement something smarter here.
                throw NotImplementedError()
            }
            // Finally, we'll map this to a domain level model, and emit a new successful resource containing this.
            val remote: DomainModel = mapper.mapFromData(remoteModel)
            emit(
                ResourceImpl.success(
                    remote
                )
            )
        }
    }

    /**
     * Open a flow for a particular database resource. This does not query from the network and will only respond when a resource
     * attached to the supplied query function is updated.
     */
    fun <DataModel, DomainModel> flowFromCache(
        mapper: Mapper<DataModel, DomainModel>,
        databaseQuery: () -> Flow<DataModel?>
    ): Flow<DomainModel?> = flow {
        // Invoke our database query to get a flow back.
        val cacheQueryFlow: Flow<DataModel?> = databaseQuery.invoke()
        // Now emit all from the cache query flow, with each item mapped to its domain layer equivalent.
        emitAll(
            cacheQueryFlow
                .map { dataModel: DataModel? ->
                    dataModel?.run { mapper.mapFromData(dataModel) }
                }
        )
    }

    /**
     * A once-off query that will execute the given network query, receiving back the result. The result will be automatically cached,
     * then mapped to a domain model and returned in a resource. This is also the suspending version.
     */
    suspend fun <DataModel, DomainModel> queryNetworkAndCache(
        mapper: Mapper<DataModel, DomainModel>,
        networkQuery: suspend () -> Resource<DataModel>,
        cacheResult: suspend (DataModel) -> Unit
    ): Resource<DomainModel> {
        // We will query our remote source for the result. Do this now.
        val remoteModelResource: Resource<DataModel> = networkQuery.invoke()
        if(remoteModelResource.status == Resource.Status.ERROR) {
            // If this is NOT successful, create and emit an error on the basis of this resource's error contents.
            remoteModelResource.resourceError?.let { attemptHandleResourceError(it) }
            return ResourceImpl(remoteModelResource.status, null, remoteModelResource.resourceError)
        } else {
            // Otherwise, we potentially have a successful result! We'll therefore get the data model version of this type.
            val remoteModel: DataModel? = remoteModelResource.data
            if (remoteModel == null) {
                Timber.e("Querying for remote model resource returned NULL!")
                // TODO: implement something smarter here.
                throw NotImplementedError()
            }
            // We have a proper data model for the required resource, we will first cache this result.
            cacheResult(remoteModel)
            // Finally, we'll map this to a domain level model, and emit a new successful resource containing this.
            val remote: DomainModel = mapper.mapFromData(remoteModel)
            return ResourceImpl.success(remote)
        }
    }

    /**
     * A once-off query that will execute the given network query, receiving back the result. The result will be returned in a resource.
     * This is also the suspending version.
     */
    suspend fun <DataModel, DomainModel> queryNetworkNoCache(
        mapper: Mapper<DataModel, DomainModel>,
        networkQuery: suspend () -> Resource<DataModel>
    ): Resource<DomainModel> {
        // We will query our remote source for the result. Do this now.
        val remoteModelResource: Resource<DataModel> = networkQuery.invoke()
        if(remoteModelResource.status == Resource.Status.ERROR) {
            // If this is NOT successful, create and emit an error on the basis of this resource's error contents.
            remoteModelResource.resourceError?.let { attemptHandleResourceError(it) }
            return ResourceImpl(remoteModelResource.status, null, remoteModelResource.resourceError)
        } else {
            // Otherwise, we potentially have a successful result! We'll therefore get the data model version of this type.
            val remoteModel: DataModel? = remoteModelResource.data
            if (remoteModel == null) {
                Timber.e("Querying for remote model resource returned NULL!")
                // TODO: implement something smarter here.
                throw NotImplementedError()
            }
            // Finally, we'll map this to a domain level model, and emit a new successful resource containing this.
            val remote: DomainModel = mapper.mapFromData(remoteModel)
            return ResourceImpl.success(remote)
        }
    }

    /**
     *
     */
    private suspend fun attemptHandleResourceError(resourceError: ResourceError) {
        Timber.w(resourceError.errorSummary)
        // Check if this resource error is an API type error.
        if(resourceError is ResourceError.ApiError) {
            // Now, if it is global, we are obliged to broadcast it.
            if(resourceError.apiErrorWrapper.isGlobal) {
                if(!this::application.isInitialized) {
                    // If application is not set, throw an exception.
                    throw Exception("Failed to broadcast global API error, injected field 'application' is not initialised.")
                }
                Timber.d("Received GLOBAL api error. Broadcasting it now... ($resourceError)")
                // TODO: implement broadcast global error here.
                // Instantiate a new Intent with the global api error action and add the api error itself as the arg.
                //val globalApiErrorIntent = Intent(ACTION_GLOBAL_API_ERROR).apply {
                //    putExtra(ARG_GLOBAL_API_ERROR, apiError)
                //}
                // Now, use application to broadcast this intent.
                //application.sendBroadcast(globalApiErrorIntent)
            }
            // There are certain instructions to undertake whenever we receive an API error of any kind.
            when(resourceError.httpStatusCode) {
                401 -> {
                    // An unauthorised error has been encountered. We'll clear our account.
                    Timber.w("Remote data request resulted in an HTTP 401; clearing account with reason: ${resourceError.apiErrorWrapper.errorInformation.get("error-code")}")
                    authenticationSession.clearCurrentAccount(resourceError.apiErrorWrapper)
                }
            }
        }
    }
}