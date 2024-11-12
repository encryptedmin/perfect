package com.odessy.srlaundry.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.adapters.CustomerAdapter
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Customer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.odessy.srlaundry.viewmodel.CustomerViewModelFactory
import com.odessy.srlaundry.viewmodel.CustomerViewModel

class NewCustomer : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var customerNameInput: EditText
    private lateinit var customerPhoneInput: EditText
    private lateinit var saveButton: Button
    private lateinit var clearButton: Button
    private lateinit var cancelButton: Button
    private lateinit var editButton: Button
    private lateinit var recyclerView: RecyclerView

    private var selectedCustomer: Customer? = null
    private var isEditMode: Boolean = false

    private val viewModel: CustomerViewModel by viewModels {
        CustomerViewModelFactory(db)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_customer)

        db = AppDatabase.getDatabase(this, lifecycleScope)

        customerNameInput = findViewById(R.id.editTextCustomerName)
        customerPhoneInput = findViewById(R.id.editTextCustomerPhoneNumber)
        saveButton = findViewById(R.id.buttonSave)
        clearButton = findViewById(R.id.buttonClear)
        cancelButton = findViewById(R.id.buttonCancel)
        editButton = findViewById(R.id.buttonEdit)
        recyclerView = findViewById(R.id.recyclerViewCustomerList)

        recyclerView.layoutManager = LinearLayoutManager(this)


        viewModel.customers.observe(this) { customers ->
            val adapter = CustomerAdapter(customers) { customer ->
                if (!isEditMode) {

                    selectedCustomer = customer
                    editButton.isEnabled = true
                }
            }
            recyclerView.adapter = adapter
        }

        saveButton.setOnClickListener {
            val name = customerNameInput.text.toString().trim()
            val phone = customerPhoneInput.text.toString().trim()

            if (name.isNotEmpty() && phone.isNotEmpty()) {
                lifecycleScope.launch(Dispatchers.IO) {
                    if (isEditMode && selectedCustomer != null) {

                        selectedCustomer?.let {
                            it.name = name
                            it.phone = phone
                            db.customerDao().updateCustomer(it)
                        }
                    } else {

                        val newCustomer = Customer(name = name, phone = phone, promo = 0)
                        db.customerDao().insertCustomer(newCustomer)
                    }
                    runOnUiThread {
                        Toast.makeText(this@NewCustomer, "Customer saved successfully!", Toast.LENGTH_SHORT).show()
                        clearInputs()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }


        clearButton.setOnClickListener { clearInputs() }


        cancelButton.setOnClickListener {
            startActivity(Intent(this, UserLaundry::class.java))
        }

        editButton.setOnClickListener {
            selectedCustomer?.let {
                customerNameInput.setText(it.name)
                customerPhoneInput.setText(it.phone)
                isEditMode = true
            }
        }
    }

    private fun clearInputs() {
        customerNameInput.text.clear()
        customerPhoneInput.text.clear()
        selectedCustomer = null
        isEditMode = false
        editButton.isEnabled = false
    }
}
