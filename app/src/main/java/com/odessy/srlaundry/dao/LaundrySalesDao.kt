package com.odessy.srlaundry.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.odessy.srlaundry.entities.LaundrySales
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface LaundrySalesDao : BaseDao<LaundrySales> {

    @Query("SELECT * FROM laundry_sales")
    fun getAllLaundrySales(): List<LaundrySales>

    @Query("SELECT * FROM laundry_sales WHERE id = :id")
    suspend fun getSalesById(id: Int): LaundrySales?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaundrySale(laundrySales: LaundrySales)

    @Query("SELECT * FROM laundry_sales WHERE transactionDate BETWEEN :startDate AND :endDate ORDER BY transactionDate DESC")
    fun getSalesBetweenDates(startDate: Date, endDate: Date): Flow<List<LaundrySales>>

    @Query("SELECT * FROM laundry_sales ORDER BY transactionDate DESC")
    fun getAllSales(): Flow<List<LaundrySales>>
}
