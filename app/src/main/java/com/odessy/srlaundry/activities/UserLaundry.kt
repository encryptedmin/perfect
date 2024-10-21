package com.odessy.srlaundry.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
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
    private lateinit var searchBar: EditText
    private var selectedJobOrder: JobOrder? = null
    private var selectedJobOrderView: View? = null

    private lateinit var db: AppDatabase

    private var jobOrderList = listOf<JobOrder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_laundry)

        db = AppDatabase.getDatabase(this, lifecycleScope)

        listView = findViewById(R.id.activeLaundryList)
        finishButton = findViewById(R.id.buttonLaundryFinish)
        backButton = findViewById(R.id.buttonBack)
        newJobOrderButton = findViewById(R.id.buttonNewJobOrder)
        searchBar = findViewById(R.id.searchBar)

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

                    withContext(Dispatchers.Main) {
                        sendSmsViaIntent(jobOrder.customerPhone, message)
                        Toast.makeText(this@UserLaundry, "Laundry marked as finished!", Toast.LENGTH_SHORT).show()
                        finishButton.isEnabled = false
                        resetSelectedJobOrderView()
                        loadActiveJobOrders()
                    }
                }
            }
        }

        backButton.setOnClickListener {
            startActivity(Intent(this@UserLaundry, UserDashboard::class.java))
        }

        searchBar.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val query = s.toString().lowercase(Locale.getDefault())
                filterJobOrders(query)  // Filter job orders by search term
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun loadActiveJobOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            jobOrderList = db.jobOrderDao().getActiveJobOrders()

            withContext(Dispatchers.Main) {
                if (jobOrderList.isNotEmpty()) {
                    updateJobOrderList(jobOrderList)
                } else {
                    Toast.makeText(this@UserLaundry, "No active laundry jobs", Toast.LENGTH_SHORT).show()
                    listView.adapter = null
                }
            }
        }
    }

    private fun filterJobOrders(query: String) {
        val filteredList = jobOrderList.filter { it.customerName.lowercase(Locale.getDefault()).contains(query) }
        updateJobOrderList(filteredList)
    }

    private fun updateJobOrderList(jobOrders: List<JobOrder>) {
        val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        val jobOrderDetails = jobOrders.map { jobOrder ->
            val laundryType = jobOrder.laundryType
            val formattedDate = dateFormat.format(Date(jobOrder.createdDate))
            "${jobOrder.customerName} | $laundryType | Loads: ${jobOrder.loads} | Price: ₱${jobOrder.totalPrice} | Date: $formattedDate"
        }

        val adapter = ArrayAdapter(this@UserLaundry, android.R.layout.simple_list_item_1, jobOrderDetails)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, view, position, _ ->
            selectedJobOrder = jobOrders[position]
            finishButton.isEnabled = true

            resetSelectedJobOrderView()
            selectedJobOrderView = view
            view.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200))
        }
    }

    private fun resetSelectedJobOrderView() {
        selectedJobOrderView?.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
        selectedJobOrderView = null
    }

    // Send SMS using the default messaging app
    private fun sendSmsViaIntent(phoneNumber: String?, message: String) {
        if (!phoneNumber.isNullOrEmpty()) {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:$phoneNumber")  // Sets the recipient phone number
                putExtra("sms_body", message)  // Pre-fills the message body
            }
            try {
                startActivity(smsIntent)
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to open SMS app: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, "Invalid phone number", Toast.LENGTH_SHORT).show()
        }
    }
}
