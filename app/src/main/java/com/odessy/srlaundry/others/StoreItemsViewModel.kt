package com.odessy.srlaundry.others
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.StoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoreItemsViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: StoreItemRepository
    val allStoreItems: LiveData<List<StoreItem>>

    init {
        val storeItemDao = AppDatabase.getDatabase(application, viewModelScope).storeItemDao()
        repository = StoreItemRepository(storeItemDao)
        allStoreItems = repository.allStoreItems
    }

    // Add or update a store item
    fun addOrUpdateStoreItem(storeItem: StoreItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.addOrUpdateStoreItem(storeItem)
    }

    // Delete a store item
    fun delete(storeItem: StoreItem) = viewModelScope.launch(Dispatchers.IO) {
        repository.delete(storeItem)
    }

    // Search for store items
    fun searchStoreItems(query: String): LiveData<List<StoreItem>> {
        return repository.searchStoreItems(query)
    }
}