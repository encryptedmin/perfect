package com.odessy.srlaundry.others

import androidx.lifecycle.LiveData
import com.odessy.srlaundry.dao.StoreItemDao
import com.odessy.srlaundry.entities.StoreItem

class StoreItemRepository(private val storeItemDao: StoreItemDao) {

    val allStoreItems: LiveData<List<StoreItem>> = storeItemDao.getAllStoreItems()
    suspend fun addOrUpdateStoreItem(storeItem: StoreItem) {
        val existingItem = storeItemDao.getStoreItemByName(storeItem.productName)
        if (existingItem != null) {
            val updatedItem = existingItem.copy(
                price = storeItem.price,
                quantity = existingItem.quantity + storeItem.quantity
            )
            storeItemDao.update(updatedItem)
        } else {
            storeItemDao.insert(storeItem)
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

