package com.odessy.srlaundry

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.odessy.srlaundry.dao.SmsMessageDao
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.SmsMessage
import kotlinx.coroutines.launch

class SmsMessageViewModel(application: Application) : AndroidViewModel(application) {

    private val smsMessageDao: SmsMessageDao

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope) // Pass viewModelScope here
        smsMessageDao = db.smsMessageDao()
    }

    fun insertSmsMessage(message: String) {
        viewModelScope.launch {
            val smsMessage = SmsMessage(message = message)
            smsMessageDao.insertSmsMessage(smsMessage)
        }
    }

    fun getSmsMessage(): LiveData<SmsMessage?> {
        val smsMessageLiveData = MutableLiveData<SmsMessage?>()
        viewModelScope.launch {
            smsMessageLiveData.value = smsMessageDao.getSmsMessage()
        }
        return smsMessageLiveData
    }
}

