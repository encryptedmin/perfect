package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.SmsMessage

@Dao
interface SmsMessageDao : BaseDao<SmsMessage> {

    @Query("SELECT * FROM SmsMessage")
    suspend fun getAllSmsMessages(): List<SmsMessage>

    @Query("SELECT * FROM SmsMessage LIMIT 1")
    suspend fun getSmsMessage(): SmsMessage?

    @Insert(onConflict = OnConflictStrategy.REPLACE) // or OnConflictStrategy.IGNORE
    override suspend fun insert(entity: SmsMessage)
}