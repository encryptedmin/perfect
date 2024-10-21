package com.odessy.srlaundry.others

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.StoreItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StoreItemsViewModel(application: Application) : AndroidViewModel(application) {
    private val storeItemDao = AppDatabase.getDatabase(application, viewModelScope).storeItemDao()
    val allStoreItems: LiveData<List<StoreItem>> = storeItemDao.getAllStoreItems()
    private val firestoreDb = FirebaseFirestore.getInstance().collection("store_items")
    fun addOrUpdateStoreItem(storeItem: StoreItem) {
        viewModelScope.launch(Dispatchers.IO) {
            storeItemDao.insertOrUpdate(storeItem)
            firestoreDb.document(storeItem.productName).set(storeItem)
        }
    }
    fun delete(storeItem: StoreItem) {
        viewModelScope.launch(Dispatchers.IO) {
            storeItemDao.delete(storeItem)
            firestoreDb.document(storeItem.productName).delete()
        }
    }
    fun searchStoreItems(query: String): LiveData<List<StoreItem>> {
        return storeItemDao.searchStoreItems(query)
    }
    fun syncStoreItems() {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreDb.get().addOnSuccessListener { result ->
                val storeItemsFromFirestore = result.toObjects(StoreItem::class.java)
                viewModelScope.launch(Dispatchers.IO) {
                    storeItemDao.syncStoreItems(storeItemsFromFirestore)
                }
            }.addOnFailureListener { e ->

            }
        }
    }
}
