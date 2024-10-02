package com.odessy.srlaundry.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Promotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PromoViewModel(application: Application) : AndroidViewModel(application) {

    private val promoDao = AppDatabase.getDatabase(application, viewModelScope).promotionDao()

    // Remove the property and use a function instead
    fun getPromo(): LiveData<Promotion?> {
        return promoDao.getPromo()
    }

    // Insert or update promotion settings
    fun insertOrUpdatePromo(promotion: Promotion) {
        viewModelScope.launch(Dispatchers.IO) {
            promoDao.insertOrUpdatePromo(promotion)
        }
    }
}
