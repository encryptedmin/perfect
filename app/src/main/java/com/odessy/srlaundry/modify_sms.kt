package com.odessy.srlaundry

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider

class modify_sms : AppCompatActivity() {
    private lateinit var smsMessageViewModel: SmsMessageViewModel
    private lateinit var smsMessageEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_sms)

        // Initialize the ViewModel
        smsMessageViewModel = ViewModelProvider(this).get(SmsMessageViewModel::class.java)

        smsMessageEditText = findViewById(R.id.smsMessageEditText)
        confirmButton = findViewById(R.id.confirmButton)
        cancelButton = findViewById(R.id.cancelButton)

        // Confirm button click listener
        confirmButton.setOnClickListener {
            val smsMessage = smsMessageEditText.text.toString()
            if (smsMessage.isNotEmpty()) {
                smsMessageViewModel.insertSmsMessage(smsMessage)
                Toast.makeText(this, "SMS Message Saved", Toast.LENGTH_SHORT).show()
                finish() // Close the activity
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }

        // Cancel button click listener
        cancelButton.setOnClickListener {
            finish() // Close the activity
        }
    }
}