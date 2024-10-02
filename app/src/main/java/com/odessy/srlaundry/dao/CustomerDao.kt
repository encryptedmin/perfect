package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.Customer

@Dao
interface CustomerDao {

    @Insert
    suspend fun insertCustomer(customer: Customer)

    @Query("SELECT * FROM Customer WHERE id = :customerId")
    suspend fun getCustomerById(customerId: Int): Customer?

    @Query("SELECT * FROM Customer WHERE name LIKE '%' || :query || '%'")
    suspend fun searchCustomers(query: String): List<Customer>

    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomers(): List<Customer>

    // New query to update the promo count
    @Query("UPDATE Customer SET promo = :newPromoCount WHERE id = :customerId")
    suspend fun updatePromoCount(customerId: Int, newPromoCount: Int)

    @Update
    suspend fun updateCustomer(customer: Customer)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(customer: Customer)

    @Update
    suspend fun update(customer: Customer)

    @Query("UPDATE Customer SET promo = promo + 1 WHERE id = :customerId")
    suspend fun incrementCustomerPromo(customerId: Int)

    @Query("UPDATE Customer SET promo = :promo WHERE id = :customerId")
    suspend fun updateCustomerPromo(customerId: Int, promo: Int)
}
