package com.odessy.srlaundry.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.odessy.srlaundry.entities.Accounts

@Dao
interface AccountsDao {
    @Insert
    suspend fun insert(account: Accounts)

    @Query("SELECT * FROM accounts WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): Accounts?

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Accounts>
}
