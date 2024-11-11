package com.odessy.srlaundry.others

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StoreSalesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreSalesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreSalesViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
