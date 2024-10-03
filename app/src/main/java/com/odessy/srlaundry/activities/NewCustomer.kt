package com.odessy.srlaundry.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.R
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NewCustomer : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var customerNameInput: EditText
    private lateinit var customerPhoneInput: EditText
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button
    private lateinit var cancelButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_customer)

        // Initialize the database
        db = AppDatabase.getDatabase(this, lifecycleScope)

        // Find views
        customerNameInput = findViewById(R.id.editTextCustomerName)
        customerPhoneInput = findViewById(R.id.editTextCustomerPhoneNumber)
        saveButton = findViewById(R.id.buttonSave)
        clearButton = findViewById(R.id.buttonClear)
        cancelButton = findViewById(R.id.buttonCancel)

        // Save button functionality
        saveButton.setOnClickListener {
            val name = customerNameInput.text.toString().trim()
            val phone = customerPhoneInput.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val newCustomer = Customer(name = name, phone = phone, promo = 0) // promo starts at 0
                    db.customerDao().insertCustomer(newCustomer)
                    runOnUiThread {
                        Toast.makeText(this@NewCustomer, "Customer added successfully!", Toast.LENGTH_SHORT).show()

                        // Redirect back to new_job_order after saving
                        val intent = Intent(this@NewCustomer, NewJobOrder::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP // Ensures the previous activity isn't recreated
                        startActivity(intent)
                        finish() // Close the current activity
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Clear button functionality
        clearButton.setOnClickListener {
            clearInputs()
        }

        // Cancel button functionality
        cancelButton.setOnClickListener {
            startActivity(Intent(this@NewCustomer, NewJobOrder::class.java))
        }
    }

    private fun clearInputs() {
        customerNameInput.text.clear()
        customerPhoneInput.text.clear()
    }
}
