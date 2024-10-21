package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.SmsMessage

@Dao
interface SmsMessageDao : BaseDao<SmsMessage> {

    @Query("SELECT * FROM SmsMessage")
    suspend fun getAllSmsMessages(): List<SmsMessage>
    @Query("SELECT * FROM SmsMessage LIMIT 1")
    suspend fun getSmsMessage(): SmsMessage?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    override suspend fun insert(entity: SmsMessage)
    @Query("DELETE FROM SmsMessage")
    suspend fun deleteAllSmsMessages()

}