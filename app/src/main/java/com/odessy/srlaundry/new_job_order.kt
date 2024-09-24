package com.odessy.srlaundry

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.LaundryPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class new_job_order : AppCompatActivity() {

    private lateinit var searchCustomerBar: EditText
    private lateinit var customerListView: ListView
    private lateinit var buttonCreateNewCustomer: Button
    private lateinit var radioGroupLaundryType: RadioGroup
    private lateinit var inputLaundryWeight: EditText
    private lateinit var checkboxExtraSoap: CheckBox
    private lateinit var checkboxFabricConditioner: CheckBox
    private lateinit var checkboxBleach: CheckBox
    private lateinit var textTotalPrice: TextView
    private lateinit var buttonConfirm: Button
    private lateinit var buttonClearFields: Button
    private lateinit var buttonCancel: Button

    private var selectedCustomer: String? = null
    private var selectedLaundryType: String = "Regular"
    private var totalPrice: Double = 0.0
    private lateinit var laundryPrice: LaundryPrice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_job_order)

        // Initialize views
        searchCustomerBar = findViewById(R.id.searchCustomerBar)
        customerListView = findViewById(R.id.customerListView)
        buttonCreateNewCustomer = findViewById(R.id.buttonCreateNewCustomer)
        radioGroupLaundryType = findViewById(R.id.radioGroupLaundryType)
        inputLaundryWeight = findViewById(R.id.inputLaundryWeight)
        checkboxExtraSoap = findViewById(R.id.checkboxExtraSoap)
        checkboxFabricConditioner = findViewById(R.id.checkboxFabricConditioner)
        checkboxBleach = findViewById(R.id.checkboxBleach)
        textTotalPrice = findViewById(R.id.textTotalPrice)
        buttonConfirm = findViewById(R.id.buttonConfirm)
        buttonClearFields = findViewById(R.id.buttonClearFields)
        buttonCancel = findViewById(R.id.buttonCancel)

        buttonConfirm.isEnabled = false // Disable Confirm button initially

        // Load laundry prices from the database
        loadLaundryPrices()

        // Search customer functionality
        searchCustomerBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchCustomer(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        customerListView.setOnItemClickListener { _, _, position, _ ->
            selectedCustomer = customerListView.getItemAtPosition(position).toString()
            buttonConfirm.isEnabled = true // Enable Confirm button once a customer is selected
        }

        // Create new customer button functionality
        buttonCreateNewCustomer.setOnClickListener {
            val intent = Intent(this@new_job_order, new_customer::class.java)
            startActivity(intent) // Start new customer activity
        }

        // Radio group for laundry type selection
        radioGroupLaundryType.setOnCheckedChangeListener { _, checkedId ->
            selectedLaundryType = when (checkedId) {
                R.id.radioRegular -> "Regular"
                R.id.radioBedsheets -> "Bedsheets"
                else -> "Regular"
            }
            calculateTotalPrice()
        }

        // Laundry weight input
        inputLaundryWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calculateTotalPrice()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Checkboxes for add-ons
        checkboxExtraSoap.setOnCheckedChangeListener { _, _ -> calculateTotalPrice() }
        checkboxFabricConditioner.setOnCheckedChangeListener { _, _ -> calculateTotalPrice() }
        checkboxBleach.setOnCheckedChangeListener { _, _ -> calculateTotalPrice() }

        // Cancel button functionality
        buttonCancel.setOnClickListener {
            startActivity(Intent(this@new_job_order, user_laundry::class.java))
        }

        // Clear fields button functionality
        buttonClearFields.setOnClickListener {
            clearFields()
        }

        // Confirm button functionality (JobOrder logic will be added later)
        buttonConfirm.setOnClickListener {
            // Logic to save the JobOrder will be added later
            Toast.makeText(this, "Job order confirmed!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadLaundryPrices() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@new_job_order, lifecycleScope)
            val fetchedLaundryPrice = db.laundryPriceDao().getLaundryPrice() // Assuming you have a getPrice method

            withContext(Dispatchers.Main) {
                if (fetchedLaundryPrice != null) {
                    laundryPrice = fetchedLaundryPrice
                    calculateTotalPrice()
                } else {
                    Toast.makeText(
                        this@new_job_order,
                        "No laundry prices found, using default values!",
                        Toast.LENGTH_LONG
                    ).show()
                    // Provide default values if no laundry price found
                    laundryPrice = LaundryPrice(
                        regular = 0.0,
                        bedSheet = 0.0,
                        addOnDetergent = 0.0,
                        addOnFabricConditioner = 0.0,
                        addOnBleach = 0.0
                    )
                    calculateTotalPrice()
                }
            }
        }
    }

    private fun searchCustomer(query: String) {
        // TODO: Implement customer search functionality using your database
        val customers = listOf("John Doe", "Jane Smith", "Customer 3") // Mocked customers for now
        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, customers)
        customerListView.adapter = adapter
        customerListView.visibility = if (customers.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun calculateTotalPrice() {
        val weight = inputLaundryWeight.text.toString().toDoubleOrNull() ?: 0.0

        val pricePerLoad = when (selectedLaundryType) {
            "Regular" -> laundryPrice.regular
            "Bedsheets" -> laundryPrice.bedSheet
            else -> 0.0
        }

        val loadSize = if (selectedLaundryType == "Regular") 8.0 else 6.0
        val loads = Math.ceil(weight / loadSize) // Calculate total loads

        var totalPrice = loads * pricePerLoad

        // Add-ons
        if (checkboxExtraSoap.isChecked) {
            totalPrice += laundryPrice.addOnDetergent
        }
        if (checkboxFabricConditioner.isChecked) {
            totalPrice += laundryPrice.addOnFabricConditioner
        }
        if (checkboxBleach.isChecked) {
            totalPrice += laundryPrice.addOnBleach
        }

        this.totalPrice = totalPrice
        textTotalPrice.text = "Total: $$totalPrice"
    }

    private fun clearFields() {
        searchCustomerBar.text.clear()
        inputLaundryWeight.text.clear()
        checkboxExtraSoap.isChecked = false
        checkboxFabricConditioner.isChecked = false
        checkboxBleach.isChecked = false
        buttonConfirm.isEnabled = false
        selectedCustomer = null
        textTotalPrice.text = "Total: $0.00"
    }
}
