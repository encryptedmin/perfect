package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.SmsMessage

@Dao
interface SmsMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSmsMessage(smsMessage: SmsMessage)

    @Query("SELECT * FROM SmsMessage LIMIT 1")
    suspend fun getSmsMessage(): SmsMessage?
}