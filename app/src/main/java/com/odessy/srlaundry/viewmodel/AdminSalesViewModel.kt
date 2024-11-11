package com.odessy.srlaundry.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.odessy.srlaundry.entities.LaundrySales
import java.util.*

class AdminSalesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val _salesRecords = MutableLiveData<List<LaundrySales>>()
    val salesRecords: LiveData<List<LaundrySales>> get() = _salesRecords

    fun fetchSalesData(startDate: Date, endDate: Date) {
        Log.d("FirestoreQuery", "Fetching data from $startDate to $endDate")
        db.collection("laundry_sales")
            .whereGreaterThanOrEqualTo("transactionDate", Timestamp(startDate))
            .whereLessThanOrEqualTo("transactionDate", Timestamp(endDate))
            .orderBy("transactionDate", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val salesList = documents.map { doc ->

                    LaundrySales(
                        id = 0,
                        transactionDate = doc.getTimestamp("transactionDate")?.toDate() ?: Date(),
                        laundryType = doc.getString("laundryType") ?: "",
                        weight = doc.getDouble("weight") ?: 0.0,
                        loads = doc.getLong("loads")?.toInt() ?: 0,
                        addOnDetergent = doc.getLong("addOnDetergent")?.toInt() ?: 0,
                        addOnFabricConditioner = doc.getLong("addOnFabricConditioner")?.toInt() ?: 0,
                        addOnBleach = doc.getLong("addOnBleach")?.toInt() ?: 0,
                        totalPrice = doc.getDouble("totalPrice") ?: 0.0
                    )
                }
                _salesRecords.value = salesList
                Log.d("Firestore", "Fetched ${salesList.size} records.")
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Error fetching data: ", exception)
            }
    }

    fun getStartOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time
    }

    fun getEndOfDay(date: Date): Date {
        return Calendar.getInstance().apply {
            time = date
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.time
    }
}
