package com.odessy.srlaundry.others

import androidx.lifecycle.LiveData
import com.odessy.srlaundry.dao.StoreItemDao
import com.odessy.srlaundry.entities.StoreItem

class StoreItemRepository(private val storeItemDao: StoreItemDao) {

    val allStoreItems: LiveData<List<StoreItem>> = storeItemDao.getAllStoreItems()

    // Method to add or update a store item
    suspend fun addOrUpdateStoreItem(storeItem: StoreItem) {
        val existingItem = storeItemDao.getStoreItemByName(storeItem.productName)
        if (existingItem != null) {
            // Update the existing item's price and quantity
            val updatedItem = existingItem.copy(
                price = storeItem.price, // Update price
                quantity = existingItem.quantity + storeItem.quantity // Update quantity
            )
            storeItemDao.update(updatedItem) // Use update method
        } else {
            // Insert new item if it doesn't exist
            storeItemDao.insert(storeItem) // Use insert method
        }
    }

    suspend fun insert(storeItem: StoreItem) {
        storeItemDao.insert(storeItem)
    }

    suspend fun delete(storeItem: StoreItem) {
        storeItemDao.delete(storeItem)
    }

    fun searchStoreItems(query: String): LiveData<List<StoreItem>> {
        return storeItemDao.searchStoreItems(query)
    }
}
