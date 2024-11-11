package com.odessy.srlaundry.others

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.entities.Transaction
import java.util.*

class StoreSalesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()

    private val _totalSales = MutableLiveData<Double>()
    val totalSales: LiveData<Double> = _totalSales

    private val _salesRecords = MutableLiveData<List<Transaction>>()
    val salesRecords: LiveData<List<Transaction>> = _salesRecords

    // Load daily sales
    fun loadDailySales(date: Date) {
        val startOfDay = getStartOfDay(date)
        val endOfDay = getEndOfDay(date)
        fetchSalesData(startOfDay, endOfDay)
    }

    // Load weekly sales
    fun loadWeeklySales(startDate: Date, endDate: Date) {
        fetchSalesData(startDate, endDate)
    }

    // Load monthly sales
    fun loadMonthlySales(year: Int, month: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)
        val startDate = calendar.time
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = calendar.time
        fetchSalesData(startDate, endDate)
    }

    private fun fetchSalesData(startDate: Date, endDate: Date) {
        db.collection("transactions")
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .get()
            .addOnSuccessListener { result ->
                val transactions = result.map { document ->
                    document.toObject(Transaction::class.java)
                }
                _salesRecords.value = transactions
                _totalSales.value = transactions.sumOf { it.totalPrice }
            }
    }

    private fun getStartOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.time
    }

    private fun getEndOfDay(date: Date): Date {
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.time
    }
}
