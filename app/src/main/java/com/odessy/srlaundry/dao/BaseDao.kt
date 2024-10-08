package com.odessy.srlaundry.dao

import androidx.room.*

interface BaseDao<T> {

    @Insert
    suspend fun insert(entity: T)

    @Update
    suspend fun update(entity: T)

    @Delete
    suspend fun delete(entity: T)


    @Insert
    suspend fun insertAll(entities: List<T>)


    @Delete
    suspend fun deleteAll(entities: List<T>)
}
