package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.LaundryPrice

@Dao
interface LaundryPriceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaundryPrice(laundryPrice: LaundryPrice)


    @Query("SELECT * FROM laundryPrice")
    suspend fun getLaundryPrice(): LaundryPrice?


    @Update
    suspend fun updatePrices(laundryPrice: LaundryPrice)

}
