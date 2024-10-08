package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.SmsMessage

@Dao
interface SmsMessageDao {

    @Query("SELECT * FROM SmsMessage LIMIT 1")
    suspend fun getSmsMessage(): SmsMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // or OnConflictStrategy.IGNORE
    suspend fun insert(smsMessage: SmsMessage)
}