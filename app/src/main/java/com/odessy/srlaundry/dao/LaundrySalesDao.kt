package com.odessy.srlaundry.dao
import androidx.room.*
import com.odessy.srlaundry.entities.JobOrder

@Dao
interface LaundrySalesDao
{
    @Insert
    suspend fun insertJobOrder(jobOrder: JobOrder)

    @Update
    suspend fun updateJobOrder(jobOrder: JobOrder)

    @Delete
    suspend fun deleteJobOrder(jobOrder: JobOrder)

    @Query("SELECT * FROM JobOrder WHERE id = :id")
    suspend fun getJobOrderById(id: Int): JobOrder?

    @Query("SELECT * FROM JobOrder WHERE isActive = 1")
    suspend fun getActiveJobOrders(): List<JobOrder>

    @Query("SELECT SUM(totalPrice) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalIncome(startDate: Long, endDate: Long): Double

    @Query("SELECT SUM(loads) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalLoads(startDate: Long, endDate: Long): Int

    @Query("SELECT SUM(addOnDetergent) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalDetergentAddons(startDate: Long, endDate: Long): Int

    @Query("SELECT SUM(addOnFabricConditioner) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalFabricConditionerAddons(startDate: Long, endDate: Long): Int

    @Query("SELECT SUM(addOnBleach) FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0")
    suspend fun getTotalBleachAddons(startDate: Long, endDate: Long): Int

    @Query("SELECT laundryType AS laundryType, SUM(totalPrice) AS totalIncome FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0 GROUP BY laundryType")
    suspend fun getTotalIncomeByLaundryType(startDate: Long, endDate: Long): List<LaundryIncome>

    @Query("SELECT laundryType AS laundryType, SUM(loads) AS totalLoads FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0 GROUP BY laundryType")
    suspend fun getTotalLoadsByLaundryType(startDate: Long, endDate: Long): List<LaundryLoad>

    @Query("SELECT laundryType AS laundryType, SUM(addOnDetergent) AS totalDetergent, SUM(addOnFabricConditioner) AS totalFabricConditioner, SUM(addOnBleach) AS totalBleach FROM JobOrder WHERE createdDate BETWEEN :startDate AND :endDate AND isActive = 0 GROUP BY laundryType")
    suspend fun getAddonsByLaundryType(startDate: Long, endDate: Long): List<LaundryAddons>
}