package com.odessy.srlaundry.others

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StoreViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StoreViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return StoreViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
