package com.odessy.srlaundry.dao

import androidx.room.*
import com.odessy.srlaundry.entities.Customer

@Dao
interface CustomerDao {
    @Insert
    suspend fun insertCustomer(customer: Customer)

    @Query("SELECT * FROM Customer WHERE id = :customerId")
    suspend fun getCustomerById(customerId: Int): Customer?

    @Query("SELECT * FROM Customer")
    suspend fun getAllCustomers(): List<Customer>
}
