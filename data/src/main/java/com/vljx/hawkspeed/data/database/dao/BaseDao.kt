package com.vljx.hawkspeed.data.database.dao

import androidx.room.*

/*
Special thanks to yeonseok.seo
https://stackoverflow.com/a/50736568
 */
@Dao
abstract class BaseDao<T> {
    /**
     * Insert an object in the database.
     *
     * @param obj the object to be inserted.
     * @return The SQLite row id
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: T): Long

    /**
     * Insert an array of objects in the database.
     *
     * @param obj the objects to be inserted.
     * @return The SQLite row ids
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insert(obj: List<T>?): List<Long>

    /**
     * Update an object from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    abstract suspend fun update(obj: T)

    /**
     * Update an array of objects from the database.
     *
     * @param obj the object to be updated
     */
    @Update
    abstract suspend fun update(obj: List<T>?)

    /**
     * Delete an object from the database
     *
     * @param obj the object to be deleted
     */
    @Delete
    abstract suspend fun delete(obj: T)

    @Transaction
    open suspend fun upsert(obj: T) {
        val id = insert(obj)
        if (id == -1L) {
            update(obj)
        }
    }

    @Transaction
    open suspend fun upsert(objList: List<T>) {
        val insertResult = insert(objList)
        val updateList: MutableList<T> = ArrayList()
        for (i in insertResult.indices) {
            if (insertResult[i] == (-1).toLong()) {
                updateList.add(objList[i])
            }
        }
        if (updateList.isNotEmpty()) {
            update(updateList)
        }
    }
}