package com.odessy.srlaundry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.odessy.srlaundry.dao.JobOrderDao
import kotlinx.coroutines.CoroutineScope

class SalesViewModelFactory(
    private val jobOrderDao: JobOrderDao,
    private val scope: CoroutineScope
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SalesViewModel(jobOrderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
