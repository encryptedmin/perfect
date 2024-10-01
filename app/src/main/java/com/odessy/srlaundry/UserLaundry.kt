package com.odessy.srlaundry

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.JobOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.content.Intent
import java.text.SimpleDateFormat
import java.util.*

class UserLaundry : AppCompatActivity() {

    private lateinit var listView: ListView
    private lateinit var finishButton: Button
    private lateinit var backButton: Button
    private lateinit var newJobOrderButton: Button
    private var selectedJobOrder: JobOrder? = null

    private lateinit var db: AppDatabase

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
            // Redirect to new job order activity
            startActivity(Intent(this@UserLaundry, NewJobOrder::class.java))
        }

        // Set up "Finish Laundry" button functionality
        finishButton.setOnClickListener {
            selectedJobOrder?.let { jobOrder ->
                lifecycleScope.launch(Dispatchers.IO) {
                    // Mark the job as inactive
                    jobOrder.isActive = false
                    db.jobOrderDao().updateJobOrder(jobOrder)

                    withContext(Dispatchers.Main) {
                        // Show a toast message
                        Toast.makeText(this@UserLaundry, "Laundry Finished!", Toast.LENGTH_SHORT).show()

                        // Refresh the list of active job orders
                        finishButton.isEnabled = false // Disable the button again
                        loadActiveJobOrders() // Refresh the list to remove finished jobs
                    }
                }
            }
        }

        // Set up "Back" button functionality
        backButton.setOnClickListener {
            val intent = Intent(this@UserLaundry, UserDashboard::class.java)
            startActivity(intent)
        }
    }

    // Load active job orders from the database
    private fun loadActiveJobOrders() {
        lifecycleScope.launch(Dispatchers.IO) {
            val activeJobs = db.jobOrderDao().getActiveJobOrders() // Fetch only active job orders

            withContext(Dispatchers.Main) {
                if (activeJobs.isNotEmpty()) {
                    // Create a SimpleDateFormat to format the creation date
                    val dateFormat = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

                    // Create a list to hold formatted strings for each job order
                    val jobOrderDetails = activeJobs.map { jobOrder ->
                        val laundryType = jobOrder.laundryType // Get laundry type directly from the job order
                        val formattedDate = dateFormat.format(Date(jobOrder.createdDate))

                        // Build the display string with customer name, laundry type, loads, total price, and date
                        "${jobOrder.customerName} | $laundryType | Loads: ${jobOrder.loads} | Price: ₱${jobOrder.totalPrice} | Date: $formattedDate"
                    }

                    // Set up the ListView adapter with the formatted job order details
                    val adapter = ArrayAdapter(this@UserLaundry, android.R.layout.simple_list_item_1, jobOrderDetails)
                    listView.adapter = adapter

                    // Set item click listener to select a job order
                    listView.setOnItemClickListener { _, _, position, _ ->
                        selectedJobOrder = activeJobs[position]
                        finishButton.isEnabled = true // Enable the "Finish Laundry" button when a job is selected
                    }
                } else {
                    // If no active jobs, show a message and clear the ListView
                    Toast.makeText(this@UserLaundry, "No active laundry jobs", Toast.LENGTH_SHORT).show()
                    listView.adapter = null
                }
            }
        }
    }

}
