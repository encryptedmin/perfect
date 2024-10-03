package com.odessy.srlaundry.others

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoreSalesViewModel(private val context: Context) : ViewModel() {

    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> = _transactions

    fun loadTransactions(fromDate: Long? = null, toDate: Long? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(context, viewModelScope) // Pass context here
            val transactions = if (fromDate != null && toDate != null) {
                db.transactionDao().getTransactionsBetweenDates(fromDate, toDate)
            } else {
                db.transactionDao().getAllTransactions()
            }
            _transactions.postValue(transactions)
        }
    }
}
