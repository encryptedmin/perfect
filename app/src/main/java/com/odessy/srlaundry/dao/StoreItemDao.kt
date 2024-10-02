package com.odessy.srlaundry.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.odessy.srlaundry.entities.StoreItem

@Dao
interface StoreItemDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE) // Use IGNORE to prevent conflicts during insert
    suspend fun insert(storeItem: StoreItem)

    @Update
    suspend fun update(storeItem: StoreItem) // Update method

    @Delete
    suspend fun delete(storeItem: StoreItem)

    @Query("SELECT * FROM store_items ORDER BY productName ASC")
    fun getAllStoreItems(): LiveData<List<StoreItem>>

    @Query("SELECT * FROM store_items WHERE productName = :name LIMIT 1") // Adjusted for direct match
    suspend fun getStoreItemByName(name: String): StoreItem?

    @Query("SELECT * FROM store_items WHERE productName LIKE :query")
    fun searchStoreItems(query: String): LiveData<List<StoreItem>>

    @Query("SELECT * FROM store_items")
    suspend fun getAllItems(): List<StoreItem>

    @Query("UPDATE store_items SET quantity = :newQuantity WHERE id = :itemId")
    suspend fun updateQuantity(itemId: Int, newQuantity: Int)
}