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


    fun insertSmsMessage(message: String) {
        val smsMessage = SmsMessage(
            id = 1, // Fixed ID
            message = message,
            timestamp = Date()
        )

        viewModelScope.launch(Dispatchers.IO) {

            smsMessageDao.insert(smsMessage)


            firestoreDb.document("1").set(smsMessage)
                .addOnSuccessListener {

                }
                .addOnFailureListener { e ->

                    e.printStackTrace()
                }
        }
    }


    fun syncSmsMessagesFromFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreDb.get()
                .addOnSuccessListener { documents ->

                    viewModelScope.launch(Dispatchers.IO) {
                        smsMessageDao.deleteAllSmsMessages()


                        for (document in documents) {
                            val message = document.toObject(SmsMessage::class.java)
                            smsMessageDao.insert(message)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                }
        }
    }
}
