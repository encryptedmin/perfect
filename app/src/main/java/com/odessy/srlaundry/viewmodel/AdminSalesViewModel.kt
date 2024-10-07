package com.odessy.srlaundry.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.entities.LaundrySales
import java.util.*

class AdminSalesViewModel : ViewModel() {

    enum class FilterType {
        DAILY, WEEKLY, MONTHLY
    }

    private val firestore = FirebaseFirestore.getInstance()
    private val _salesRecords = MutableLiveData<List<LaundrySales>>()
    val salesRecords: LiveData<List<LaundrySales>> = _salesRecords

    fun fetchSalesData(filterType: FilterType) {
        val (startDate, endDate) = getDateRange(filterType)

        firestore.collection("laundry_sales")
            .whereGreaterThanOrEqualTo("transactionDate", startDate)
            .whereLessThanOrEqualTo("transactionDate", endDate)
            .get()
            .addOnSuccessListener { result ->
                val salesList = result.toObjects(LaundrySales::class.java)
                if (salesList.isNullOrEmpty()) {
                    Log.d("AdminSalesViewModel", "No sales records found for selected period.")
                }
                _salesRecords.value = salesList
            }
            .addOnFailureListener { exception: Exception ->
                Log.e("AdminSalesViewModel", "Error fetching sales data: ${exception.message}", exception)
            }
    }

    private fun getDateRange(filterType: FilterType): Pair<Date, Date> {
        val calendar = Calendar.getInstance()
        val endDate = calendar.time
        val startDate: Date = when (filterType) {
            FilterType.DAILY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                calendar.time
            }
            FilterType.WEEKLY -> {
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.time
            }
            FilterType.MONTHLY -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.time
            }
        }
        return Pair(startDate, endDate)
    }
}
