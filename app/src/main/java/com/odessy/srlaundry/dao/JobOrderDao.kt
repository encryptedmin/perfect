package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.JobOrder

@Dao
interface JobOrderDao {

    @Insert
    suspend fun insertJobOrder(jobOrder: JobOrder)

    @Update
    suspend fun updateJobOrder(jobOrder: JobOrder)

    @Delete
    suspend fun deleteJobOrder(jobOrder: JobOrder)

    @Query("SELECT * FROM JobOrder WHERE id = :id")
    suspend fun getJobOrderById(id: Int): JobOrder?

    @Query("SELECT * FROM JobOrder")
    suspend fun getAllJobOrders(): List<JobOrder>
}
