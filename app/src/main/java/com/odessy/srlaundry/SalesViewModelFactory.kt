package com.odessy.srlaundry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.odessy.srlaundry.dao.JobOrderDao
import com.odessy.srlaundry.database.AppDatabase
class SalesViewModelFactory(private val jobOrderDao: JobOrderDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            return SalesViewModel(jobOrderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
