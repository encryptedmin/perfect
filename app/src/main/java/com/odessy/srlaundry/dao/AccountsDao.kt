package com.odessy.srlaundry.dao

import androidx.room.Dao
import androidx.room.Query
import com.odessy.srlaundry.entities.Accounts
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.google.firebase.firestore.FirebaseFirestore

@Dao
interface AccountsDao : BaseDao<Accounts> {

    @Query("SELECT * FROM accounts WHERE username = :username")
    suspend fun getAccountByUsername(username: String): Accounts?

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Accounts>

    @Query("SELECT * FROM accounts WHERE username = :username AND password = :password")
    suspend fun login(username: String, password: String): Accounts?

    suspend fun insertAndSync(account: Accounts) {
        insert(account)


        withContext(Dispatchers.IO) {
            val firestoreDb = FirebaseFirestore.getInstance()
            firestoreDb.collection("users").document(account.username).set(account)
        }
    }
}
