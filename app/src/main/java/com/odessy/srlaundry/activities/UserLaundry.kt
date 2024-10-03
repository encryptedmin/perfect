package com.odessy.srlaundry.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.R
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.JobOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

class UserLaundry : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var finishButton: Button
    private lateinit var backButton: Button
    private lateinit var newJobOrderButton: Button
    private var selectedJobOrder: JobOrder? = null

    private lateinit var db: AppDatabase
    private val SMS_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_laundry)

        // Initialize database with lifecycleScope
        db = AppDatabase.getDatabase(this, lifecycleScope)

        // Find views
        listView = findViewById(R.id.activeLaundryList)
        finishButton = findViewById(R.id.buttonLaundryFinish)
        backButton = findViewById(R.id.buttonBack)
        newJobOrderButton = findViewById(R.id.buttonNewJobOrder)

        // Initially disable the "Finish Laundry" button
        finishButton.isEnabled = false

        // Load active job orders
        loadActiveJobOrders()

        // Set up button to start a new job order
        newJobOrderButton.setOnClickListener {
            startActivity(Intent(this@UserLaundry, NewJobOrder::class.java))
        }

        // Set up "Finish Laundry" button functionality
        finishButton.setOnClickListener {
            selectedJobOrder?.let { jobOrder ->
                lifecycleScope.launch(Dispatchers.IO) {
                    // Mark the job as inactive
                    jobOrder.isActive = false
                    db.jobOrderDao().updateJobOrder(jobOrder)

                    // Fetch the SMS message from the database
                    val smsMessage = db.smsMessageDao().getSmsMessage() // Fetch the message from SmsMessage entity
                    val message = smsMessage?.message ?: "Your laundry job has been completed!" // Default message if not found

                    // Check and request SMS permission
                    if (ContextCompat.checkSelfPermission(this@UserLaundry, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                        sendSmsNotification(jobOrder.customerPhone, message)
                    } else {
                        requestSmsPermission()
                    }

                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@UserLaundry, "Laundry Finished!", Toast.LENGTH_SHORT).show()
                        finishButton.isEnabled = false
                        loadActiveJobOrders()
                    }
                }
            }
        }

        // Set up "Back" button functionality
        backButton.setOnClickListener {
            startActivity(Intent(this@UserLaundry, UserDashboard::class.java))
        }
    }

    // Load active job orders from the database
    private fun loadActiveJobOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            val activeJobs = db.jobOrderDao().getActiveJobOrders()

            withContext(Dispatchers.Main) {
                if (activeJobs.isNotEmpty()) {
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                    val jobOrderDetails = activeJobs.map { jobOrder ->
                        val laundryType = jobOrder.laundryType
                        val formattedDate = dateFormat.format(Date(jobOrder.createdDate))
                        "${jobOrder.customerName} | $laundryType | Loads: ${jobOrder.loads} | Price: ₱${jobOrder.totalPrice} | Date: $formattedDate"
                    }

                    val adapter = ArrayAdapter(this@UserLaundry, android.R.layout.simple_list_item_1, jobOrderDetails)
                    listView.adapter = adapter

                    listView.setOnItemClickListener { _, _, position, _ ->
                        selectedJobOrder = activeJobs[position]
                        finishButton.isEnabled = true
                    }
                } else {
                    Toast.makeText(this@UserLaundry, "No active laundry jobs", Toast.LENGTH_SHORT).show()
                    listView.adapter = null
                }
            }
        }
    }

    // Send SMS notification
    private fun sendSmsNotification(phoneNumber: String?, message: String) {
        if (!phoneNumber.isNullOrEmpty()) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to send SMS: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
        }
    }

    // Request SMS permission
    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
    }

    // Handle the result of the permission request
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you can now send the SMS
                selectedJobOrder?.let { jobOrder ->
                    lifecycleScope.launch(Dispatchers.IO) {
                        val smsMessage = db.smsMessageDao().getSmsMessage()
                        val message = smsMessage?.message ?: "Your laundry job has been completed!"
                        sendSmsNotification(jobOrder.customerPhone, message)
                    }
                }
            } else {
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
