package com.odessy.srlaundry.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.odessy.srlaundry.entities.Promo

@Dao
interface PromoDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePromo(promo: Promo)


    @Query("SELECT * FROM Promo LIMIT 1")
    fun getPromo(): LiveData<Promo?>


    @Query("DELETE FROM Promo")
    suspend fun deleteAllPromos()


    @Query("UPDATE Promo SET serviceFrequency = :serviceFrequency WHERE id = :promoId")
    suspend fun updateServiceFrequency(promoId: Int, serviceFrequency: Int)


    @Query("UPDATE Promo SET isPromoActive = :isPromoActive WHERE id = :promoId")
    suspend fun setPromoActive(promoId: Int, isPromoActive: Boolean)
}
