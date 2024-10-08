package com.odessy.srlaundry.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.odessy.srlaundry.entities.Transaction

@Dao
interface TransactionDao : BaseDao<Transaction> {

    @Query("SELECT * FROM transactions")
    fun getAllTransactions(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Int): Transaction?
    @Insert
    suspend fun insertTransaction(transaction: Transaction)
    
    @Query("SELECT * FROM transactions WHERE timestamp BETWEEN :fromDate AND :toDate")
    suspend fun getTransactionsBetweenDates(fromDate: Long, toDate: Long): List<Transaction>
}
