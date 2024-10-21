package com.odessy.srlaundry.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.odessy.srlaundry.R
import com.odessy.srlaundry.others.SmsMessageViewModel

class ModifySms : AppCompatActivity() {
    private lateinit var smsMessageViewModel: SmsMessageViewModel
    private lateinit var smsMessageEditText: EditText
    private lateinit var confirmButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_sms)
        smsMessageViewModel = ViewModelProvider(this).get(SmsMessageViewModel::class.java)
        smsMessageEditText = findViewById(R.id.smsMessageEditText)
        confirmButton = findViewById(R.id.confirmButton)
        cancelButton = findViewById(R.id.cancelButton)
        confirmButton.setOnClickListener {
            val smsMessage = smsMessageEditText.text.toString()
            if (smsMessage.isNotEmpty()) {
                smsMessageViewModel.insertSmsMessage(smsMessage)
                Toast.makeText(this, "SMS Message Saved", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
        cancelButton.setOnClickListener {
            finish()
        }
    }
}