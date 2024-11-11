package com.odessy.srlaundry.others

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.odessy.srlaundry.entities.Transaction
import java.util.*

class StoreSalesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _totalSales = MutableLiveData<Double>()
    val totalSales: LiveData<Double> get() = _totalSales

    private val _salesRecords = MutableLiveData<List<Transaction>>()
    val salesRecords: LiveData<List<Transaction>> get() = _salesRecords

    // Fetch sales data for a specific day
    fun loadDailySales(date: Date) {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)
        fetchSalesData(startOfDay, endOfDay)
    }

    // Fetch sales data for a specific week
    fun loadWeeklySales(date: Date) {
        val startOfWeek = getStartOfWeek(date)
        val endOfWeek = getEndOfWeek(date)
        fetchSalesData(startOfWeek, endOfWeek)
    }

    // Fetch sales data for a specific month
    fun loadMonthlySales(date: Date) {
        val startOfMonth = getStartOfMonth(date)
        val endOfMonth = getEndOfMonth(date)
        fetchSalesData(startOfMonth, endOfMonth)
    }

    // Private function to query Firestore for sales records within a date range
    private fun fetchSalesData(startDate: Date, endDate: Date) {
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", Timestamp(startDate))
            .whereLessThanOrEqualTo("timestamp", Timestamp(endDate))
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { result ->
                val transactions = result.map { document ->
                    document.toObject(Transaction::class.java)
                }
                _salesRecords.value = transactions
                _totalSales.value = transactions.sumOf { it.totalPrice }
            }
            .addOnFailureListener {
                _salesRecords.value = emptyList()
                _totalSales.value = 0.0
            }
    }

    private fun getStartOfDay(date: Date): Date = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun getEndOfDay(date: Date): Date = Calendar.getInstance().apply {
        time = date
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time

    private fun getStartOfWeek(date: Date): Date = Calendar.getInstance().apply {
        time = date
        set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun getEndOfWeek(date: Date): Date = Calendar.getInstance().apply {
        time = date
        set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time

    private fun getStartOfMonth(date: Date): Date = Calendar.getInstance().apply {
        time = date
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    private fun getEndOfMonth(date: Date): Date = Calendar.getInstance().apply {
        time = date
        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }.time
}
