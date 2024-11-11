package com.odessy.srlaundry.others

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StoreSalesViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreSalesViewModel::class.java)) {
            return StoreSalesViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
