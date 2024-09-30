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

    @Query("SELECT * FROM JobOrder WHERE isActive = 1")
    suspend fun getActiveJobOrders(): List<JobOrder> // Fetch only active job orders
    // Total income within a date range
    @Query("SELECT SUM(totalPrice) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalIncome(startDate: Long, endDate: Long): Double

    // Total number of regular loads (based on loads column)
    @Query("SELECT SUM(loads) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalLoads(startDate: Long, endDate: Long): Int

    // Total number of detergent add-ons
    @Query("SELECT SUM(addOnDetergent) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalDetergentAddons(startDate: Long, endDate: Long): Int

    // Total number of fabric conditioner add-ons
    @Query("SELECT SUM(addOnFabricConditioner) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalFabricConditionerAddons(startDate: Long, endDate: Long): Int

    // Total number of bleach add-ons
    @Query("SELECT SUM(addOnBleach) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalBleachAddons(startDate: Long, endDate: Long): Int
}
    


