package com.odessy.srlaundry.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.odessy.srlaundry.entities.StoreItem

@Dao
interface StoreItemDao : BaseDao<StoreItem> {

    @Query("SELECT * FROM store_items")
    suspend fun getAllStoreItemsSync(): List<StoreItem> // Returns items for sync
    @Insert(onConflict = OnConflictStrategy.IGNORE)

    override suspend fun insert(entity: StoreItem)

    @Update
    override suspend fun update(entity: StoreItem)

    @Delete
    override suspend fun delete(entity: StoreItem)

    @Query("SELECT * FROM store_items ORDER BY productName ASC")
    fun getAllStoreItems(): LiveData<List<StoreItem>>

    @Query("SELECT * FROM store_items WHERE productName = :name LIMIT 1")
    suspend fun getStoreItemByName(name: String): StoreItem?

    @Query("SELECT * FROM store_items WHERE productName LIKE :query")
    fun searchStoreItems(query: String): LiveData<List<StoreItem>>

    @Query("SELECT * FROM store_items")
    suspend fun getAllItems(): List<StoreItem>

    @Query("SELECT * FROM store_items WHERE productName = :productName")
    suspend fun getStoreItemByProductName(productName: String): StoreItem?

    @Insert
    suspend fun insertStoreItem(item: StoreItem)

    @Update
    suspend fun updateStoreItem(item: StoreItem)

    @Query("UPDATE store_items SET quantity = :newQuantity WHERE productName = :productName")
    suspend fun updateQuantity(productName: String, newQuantity: Int)


    @Query("SELECT * FROM store_items WHERE quantity < :threshold")
    suspend fun getItemsBelowThreshold(threshold: Int): List<StoreItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(storeItem: StoreItem)

    @Transaction
    fun syncStoreItems(storeItems: List<StoreItem>) {

        deleteAllStoreItems()
        storeItems.forEach { insertOrUpdate(it) }
    }

    @Query("DELETE FROM store_items")
    fun deleteAllStoreItems()

}