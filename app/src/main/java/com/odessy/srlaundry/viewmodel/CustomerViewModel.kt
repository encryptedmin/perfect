package com.odessy.srlaundry.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.liveData
import com.odessy.srlaundry.database.AppDatabase
import kotlinx.coroutines.Dispatchers

class CustomerViewModel(private val database: AppDatabase) : ViewModel() {

    val customers = liveData(Dispatchers.IO) {
        emit(database.customerDao().getAllCustomers())
    }
}

