package com.odessy.srlaundry.others

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.SmsMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class SmsMessageViewModel(application: Application) : AndroidViewModel(application) {

    private val smsMessageDao = AppDatabase.getDatabase(application, viewModelScope).smsMessageDao()
    private val firestoreDb = FirebaseFirestore.getInstance().collection("sms_messages")

    // Insert SMS message into Room and Firestore
    fun insertSmsMessage(message: String) {
        val smsMessage = SmsMessage(
            message = message,
            timestamp = Date()
        )

        viewModelScope.launch(Dispatchers.IO) {
            // Insert into Room (local database)
            smsMessageDao.insert(smsMessage)

            // Sync with Firestore (cloud database)
            firestoreDb.add(smsMessage)
                .addOnSuccessListener {
                    // Message successfully written to Firestore
                }
                .addOnFailureListener { e ->
                    // Handle Firestore failure
                    e.printStackTrace()
                }
        }
    }
}
