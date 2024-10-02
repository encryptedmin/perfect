package com.odessy.srlaundry.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.odessy.srlaundry.entities.Promotion

@Dao
interface PromotionDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePromo(promotion: Promotion)


    @Query("SELECT * FROM Promotion LIMIT 1")
    fun getPromo(): LiveData<Promotion?>


    @Query("DELETE FROM Promotion")
    suspend fun deleteAllPromos()


    @Query("UPDATE Promotion SET serviceFrequency = :serviceFrequency WHERE id = :promoId")
    suspend fun updateServiceFrequency(promoId: Int, serviceFrequency: Int)


    @Query("UPDATE Promotion SET isPromoActive = :isPromoActive WHERE id = :promoId")
    suspend fun setPromoActive(promoId: Int, isPromoActive: Boolean)

    @Query("SELECT * FROM Promotion WHERE isPromoActive = 1 LIMIT 1")
    suspend fun getActivePromotion(): Promotion?
}
