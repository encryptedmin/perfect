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
import kotlinx.coroutines.withContext
import java.util.*

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val storeItemDao = AppDatabase.getDatabase(application, viewModelScope).storeItemDao()
    private val transactionDao = AppDatabase.getDatabase(application, viewModelScope).transactionDao()

    private val storeItemsCollection = FirebaseFirestore.getInstance().collection("store_items")
    private val transactionsCollection = FirebaseFirestore.getInstance().collection("transactions")

    // LiveData for observing all store items in Room
    val allStoreItems: LiveData<List<StoreItem>> = storeItemDao.getAllStoreItems()

    // Function to search store items by query
    fun searchStoreItems(query: String): LiveData<List<StoreItem>> {
        return storeItemDao.searchStoreItems("%$query%")
    }

    // Add or update a store item in both Room and Firestore
    fun addOrUpdateStoreItem(storeItem: StoreItem) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Insert or update in Room
                storeItemDao.insertOrUpdate(storeItem)

                // Sync with Firestore
                storeItemsCollection.document(storeItem.productName).set(storeItem)
                    .addOnSuccessListener {
                        // Log success or notify the UI if necessary
                    }
                    .addOnFailureListener { e ->
                        // Handle the error in Firestore sync
                        e.printStackTrace() // You can also log this or show a message in UI
                    }
            } catch (e: Exception) {
                // Handle any Room database issues
                e.printStackTrace()
            }
        }
    }

    // Update item quantity in both Room and Firestore
    fun updateQuantity(productName: String, newQuantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Update quantity in Room
                storeItemDao.updateQuantity(productName, newQuantity)

                // Sync updated quantity with Firestore
                storeItemsCollection.document(productName).update("quantity", newQuantity)
                    .addOnSuccessListener {
                        // Log success or notify the UI if necessary
                    }
                    .addOnFailureListener { e ->
                        // Handle the error in Firestore sync
                        e.printStackTrace() // You can also log this or show a message in UI
                    }
            } catch (e: Exception) {
                // Handle any Room database issues
                e.printStackTrace()
            }
        }
    }

    // Check if there are any low-stock items below a certain threshold
    suspend fun checkLowStock(threshold: Int): List<StoreItem> {
        return withContext(Dispatchers.IO) {
            storeItemDao.getItemsBelowThreshold(threshold)
        }
    }

    // Insert a transaction and sync with Firestore
    fun addTransaction(storeItem: StoreItem, quantity: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val transaction = Transaction(
                    productName = storeItem.productName,
                    quantity = quantity,
                    totalPrice = storeItem.price * quantity,
                    timestamp = Date()
                )

                // Insert the transaction into Room
                transactionDao.insertTransaction(transaction)

                // Sync the transaction with Firestore
                transactionsCollection.add(transaction)
                    .addOnSuccessListener {
                        // Log success or notify the UI if necessary
                    }
                    .addOnFailureListener { e ->
                        // Handle the error in Firestore sync
                        e.printStackTrace() // You can also log this or show a message in UI
                    }
            } catch (e: Exception) {
                // Handle any Room database issues
                e.printStackTrace()
            }
        }
    }

    // Sync store items from Firestore to Room (useful when starting the app or refreshing)
    fun fetchAndSyncStoreItemsFromFirestore() {
        storeItemsCollection.get()
            .addOnSuccessListener { result ->
                viewModelScope.launch(Dispatchers.IO) {
                    for (document in result) {
                        val storeItem = document.toObject(StoreItem::class.java)
                        storeItemDao.insertOrUpdate(storeItem) // Sync with Room
                    }
                }
            }
            .addOnFailureListener { e ->
                // Handle failure here
                e.printStackTrace()
            }
    }
}
