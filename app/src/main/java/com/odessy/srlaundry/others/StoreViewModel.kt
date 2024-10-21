package com.odessy.srlaundry.others

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.StoreItem
import com.odessy.srlaundry.entities.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val storeItemDao = AppDatabase.getDatabase(application, viewModelScope).storeItemDao()
    private val transactionDao = AppDatabase.getDatabase(application, viewModelScope).transactionDao()
    private val storeItemsCollection = FirebaseFirestore.getInstance().collection("store_items")
    private val transactionsCollection = FirebaseFirestore.getInstance().collection("transactions")
    val allStoreItems: LiveData<List<StoreItem>> = storeItemDao.getAllStoreItems()
    fun searchStoreItems(query: String): LiveData<List<StoreItem>> {
        return storeItemDao.searchStoreItems("%$query%")
    }
    fun addOrUpdateStoreItem(storeItem: StoreItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                storeItemDao.insertOrUpdate(storeItem)
                storeItemsCollection.document(storeItem.productName).set(storeItem)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun updateQuantity(productName: String, newQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                storeItemDao.updateQuantity(productName, newQuantity)
                storeItemsCollection.document(productName).update("quantity", newQuantity)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    suspend fun getStoreItemByName(productName: String): StoreItem? {
        return withContext(Dispatchers.IO) {
            storeItemDao.getStoreItemByName(productName)
        }
    }
    suspend fun checkLowStock(threshold: Int): List<StoreItem> {
        return withContext(Dispatchers.IO) {
            storeItemDao.getItemsBelowThreshold(threshold)
        }
    }
    fun addTransaction(storeItem: StoreItem, quantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transaction = Transaction(
                    productName = storeItem.productName,
                    quantity = quantity,
                    totalPrice = storeItem.price * quantity,
                    timestamp = Date()
                )
                transactionDao.insertTransaction(transaction)
                transactionsCollection.add(transaction)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun fetchAndSyncStoreItemsFromFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val snapshot = storeItemsCollection.get().await()
                val storeItems = snapshot.toObjects(StoreItem::class.java)
                storeItems.forEach { storeItem ->
                    storeItemDao.insertOrUpdate(storeItem)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
