package com.vljx.hawkspeed.data

import com.vljx.hawkspeed.data.mapper.Mapper
import com.vljx.hawkspeed.domain.Resource
import com.vljx.hawkspeed.domain.ResourceImpl
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.*
import timber.log.Timber

abstract class BaseRepository {
    /**
     * Open a flow for a particular database resource. This does not query from the network and will only respond when a resource
     * attached to the supplied query function is updated.
     */
    fun <DataModel, DomainModel> fromCache(
        mapper: Mapper<DataModel, DomainModel>,
        databaseQuery: () -> Flow<DataModel?>
    ): Flow<Resource<DomainModel>> = flow {
        // Emit a loading resource.
        emit(ResourceImpl.loading())
        // Invoke our database query to get a flow back.
        val cacheQueryFlow: Flow<DataModel?> = databaseQuery.invoke()
        // Now emit all from the cache query flow, with each item mapped to its domain layer equivalent.
        emitAll(
            cacheQueryFlow
                .filter { nullableDataModel: DataModel? -> nullableDataModel != null }
                .map { dataModel: DataModel? -> ResourceImpl.success(mapper.mapFromData(dataModel!!)) }
        )
    }

    /**
     * Mediate between remote and local data for a specific resource, but return a flow that will emit updated instances
     * of the target query non-stop instead of simply querying for the resource once.
     */
    fun <DataModel, DomainModel> queryWithCacheFlow(
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
    fun <DataModel, DomainModel> queryAndCache(
        mapper: Mapper<DataModel, DomainModel>,
        networkQuery: suspend () -> Resource<DataModel>,
        cacheResult: suspend (DataModel) -> Unit
    ): Flow<Resource<DomainModel>> = flow {
        // Emit a loading resource.
        emit(ResourceImpl.loading())

        // We will query our remote source for the result. Do this now.
        val remoteModelResource: Resource<DataModel> = networkQuery.invoke()
        if(remoteModelResource.status == Resource.Status.ERROR) {
            // If this is NOT successful, create and emit an error on the basis of this resource's error contents.
            Timber.e(
                remoteModelResource.resourceError?.summariseError()
                    ?: throw NullPointerException("Failed to perform networkQuery, and no resource error has been given.")
            )
            emit(
                ResourceImpl(
                    remoteModelResource.status,
                    null,
                    remoteModelResource.resourceError
                )
            )
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
            emit(
                ResourceImpl.success(
                    remote
                )
            )
        }
    }

    /**
     * Only perform a remote query for a resource, this will not search for any existing resources in the database,
     * nor will it cache any successful results received.
     */
    fun <DataModel, DomainModel> queryNoCache(
        mapper: Mapper<DataModel, DomainModel>,
        networkQuery: suspend () -> Resource<DataModel>,
    ): Flow<Resource<DomainModel>> = flow {
        // Emit a loading resource.
        emit(ResourceImpl.loading())

        // We will query our remote source for the result. Do this now.
        val remoteModelResource: Resource<DataModel> = networkQuery.invoke()
        if(remoteModelResource.status == Resource.Status.ERROR) {
            // If this is NOT successful, create and emit an error on the basis of this resource's error contents.
            emit(
                ResourceImpl(
                    remoteModelResource.status,
                    null,
                    remoteModelResource.resourceError
                )
            )
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
}