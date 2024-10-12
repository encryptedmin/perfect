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


        db = AppDatabase.getDatabase(this, lifecycleScope)


        listView = findViewById(R.id.activeLaundryList)
        finishButton = findViewById(R.id.buttonLaundryFinish)
        backButton = findViewById(R.id.buttonBack)
        newJobOrderButton = findViewById(R.id.buttonNewJobOrder)

        finishButton.isEnabled = false

        loadActiveJobOrders()

        newJobOrderButton.setOnClickListener {
            startActivity(Intent(this@UserLaundry, NewJobOrder::class.java))
        }

        finishButton.setOnClickListener {
            selectedJobOrder?.let { jobOrder ->
                lifecycleScope.launch(Dispatchers.IO) {

                    jobOrder.isActive = false
                    db.jobOrderDao().updateJobOrder(jobOrder)


                    val smsMessage = db.smsMessageDao().getSmsMessage()
                    val message = smsMessage?.message ?: "Your laundry job has been completed!"

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


        backButton.setOnClickListener {
            startActivity(Intent(this@UserLaundry, UserDashboard::class.java))
        }
    }

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

    private fun requestSmsPermission() {
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), SMS_PERMISSION_CODE)
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

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
