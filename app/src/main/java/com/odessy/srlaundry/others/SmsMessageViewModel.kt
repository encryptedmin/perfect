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

// SmsMessageViewModel.kt
class SmsMessageViewModel(application: Application) : AndroidViewModel(application) {

    private val smsMessageDao = AppDatabase.getDatabase(application, viewModelScope).smsMessageDao()
    private val firestoreDb = FirebaseFirestore.getInstance().collection("sms_messages")


    fun insertSmsMessage(message: String) {
        val smsMessage = SmsMessage(
            id = 1, // Fixed ID
            message = message,
            timestamp = Date()
        )

        viewModelScope.launch(Dispatchers.IO) {
            // Insert into Room (local database)
            smsMessageDao.insert(smsMessage)

            // Sync with Firestore (cloud database)
            firestoreDb.document("1").set(smsMessage)
                .addOnSuccessListener {
                    // Message successfully written to Firestore
                }
                .addOnFailureListener { e ->
                    // Handle Firestore failure
                    e.printStackTrace()
                }
        }
    }


    fun syncSmsMessagesFromFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreDb.get()
                .addOnSuccessListener { documents ->
                    // Delete all local messages before sync
                    viewModelScope.launch(Dispatchers.IO) {
                        smsMessageDao.deleteAllSmsMessages()

                        // Insert Firestore messages into Room
                        for (document in documents) {
                            val message = document.toObject(SmsMessage::class.java)
                            smsMessageDao.insert(message)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace() // Handle Firestore failure
                }
        }
    }
}
