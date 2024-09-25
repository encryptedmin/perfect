package com.odessy.srlaundry

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Customer
import com.odessy.srlaundry.entities.JobOrder
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
    private lateinit var textTotalPrice: TextView
    private lateinit var textTotalLoads: TextView // New field for showing total loads
    private lateinit var buttonConfirm: Button
    private lateinit var buttonClearFields: Button
    private lateinit var buttonCancel: Button

    private var selectedCustomer: Customer? = null
    private var selectedLaundryType: String = "Regular"
    private var totalPrice: Double = 0.0
    private lateinit var laundryPrice: LaundryPrice
    private var addOnBleachCount = 0
    private var addOnDetergentCount = 0
    private var addOnFabricConditionerCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_job_order)

        // Initialize views
        searchCustomerBar = findViewById(R.id.searchCustomerBar)
        customerListView = findViewById(R.id.customerListView)
        buttonCreateNewCustomer = findViewById(R.id.buttonCreateNewCustomer)
        radioGroupLaundryType = findViewById(R.id.radioGroupLaundryType)
        inputLaundryWeight = findViewById(R.id.inputLaundryWeight)
        textTotalPrice = findViewById(R.id.textTotalPrice)
        textTotalLoads = findViewById(R.id.textTotalLoads) // Initialize textTotalLoads view
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
            selectedCustomer = customerListView.getItemAtPosition(position) as Customer
            buttonConfirm.isEnabled = true // Enable Confirm button once a customer is selected
        }

        // Create new customer button functionality
        buttonCreateNewCustomer.setOnClickListener {
            startActivity(Intent(this@new_job_order, new_customer::class.java)) // Start new customer activity
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

        // Cancel button functionality
        buttonCancel.setOnClickListener {
            startActivity(Intent(this@new_job_order, user_laundry::class.java))
        }

        // Clear fields button functionality
        buttonClearFields.setOnClickListener {
            clearFields()
        }

        // Confirm button functionality (create JobOrder)
        buttonConfirm.setOnClickListener {
            // Launch a coroutine to ensure the JobOrder is created before redirecting
            lifecycleScope.launch {
                createJobOrder() // Wait until job order is created
                val intent = Intent(this@new_job_order, user_laundry::class.java)
                startActivity(intent) // Redirect after job order creation
            }
        }

        // Setup add-on buttons
        setupAddOnButtons()
    }

    private fun loadLaundryPrices() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@new_job_order, lifecycleScope)
            val fetchedLaundryPrice = db.laundryPriceDao().getLaundryPrice()

            withContext(Dispatchers.Main) {
                if (fetchedLaundryPrice != null) {
                    laundryPrice = fetchedLaundryPrice
                    calculateTotalPrice()
                } else {
                    Toast.makeText(
                        this@new_job_order,
                        getString(R.string.laundry_price_not_found),
                        Toast.LENGTH_LONG
                    ).show()
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
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@new_job_order, lifecycleScope)
            val customers = db.customerDao().searchCustomers(query)
            val adapter = ArrayAdapter(this@new_job_order, android.R.layout.simple_list_item_1, customers)

            withContext(Dispatchers.Main) {
                customerListView.adapter = adapter
                customerListView.visibility = if (customers.isNotEmpty()) android.view.View.VISIBLE else android.view.View.GONE
            }
        }
    }

    private fun calculateTotalPrice() {
        val weight = inputLaundryWeight.text.toString().toDoubleOrNull() ?: 0.0

        val pricePerLoad = when (selectedLaundryType) {
            "Regular" -> laundryPrice.regular
            "Bedsheets" -> laundryPrice.bedSheet
            else -> 0.0
        }

        val loadSize = if (selectedLaundryType == "Regular") 8.0 else 6.0
        val loads = Math.ceil(weight / loadSize).toInt() // Calculate total loads

        totalPrice = (loads * pricePerLoad) +
                (addOnBleachCount * laundryPrice.addOnBleach) +
                (addOnDetergentCount * laundryPrice.addOnDetergent) +
                (addOnFabricConditionerCount * laundryPrice.addOnFabricConditioner)

        // Set total price and total loads
        textTotalPrice.text = getString(R.string.total_price_format, totalPrice)
        textTotalLoads.text = getString(R.string.total_loads_format, loads) // Set number of loads

        Log.d("DEBUG", "Weight: $weight, Loads: $loads, Total Price: $totalPrice")
    }

    private fun clearFields() {
        searchCustomerBar.text.clear()
        inputLaundryWeight.text.clear()
        buttonConfirm.isEnabled = false
        selectedCustomer = null
        textTotalPrice.text = getString(R.string.total_price_format, 0.0)
        textTotalLoads.text = getString(R.string.total_loads_format, 0) // Reset loads to 0
        addOnBleachCount = 0
        addOnDetergentCount = 0
        addOnFabricConditionerCount = 0
        updateAddOnText()
    }

    // Suspend function to ensure job order creation completes before redirection
    private suspend fun createJobOrder() {
        val weight = inputLaundryWeight.text.toString().toDoubleOrNull() ?: 0.0
        val loads = Math.ceil(weight / (if (selectedLaundryType == "Regular") 8.0 else 6.0)).toInt()

        val jobOrder = JobOrder(
            customerName = selectedCustomer?.name ?: "",
            weight = weight,
            loads = loads,
            addOnDetergent = addOnDetergentCount,
            addOnFabricConditioner = addOnFabricConditionerCount,
            addOnBleach = addOnBleachCount,
            totalPrice = totalPrice
        )

        // Perform database operation in IO dispatcher
        withContext(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@new_job_order, lifecycleScope)
            db.jobOrderDao().insertJobOrder(jobOrder)
        }

        // Update UI or show a Toast message on the Main thread after job order is created
        withContext(Dispatchers.Main) {
            Toast.makeText(this@new_job_order, getString(R.string.job_order_created), Toast.LENGTH_SHORT).show()
            clearFields()
        }
    }

    private fun setupAddOnButtons() {
        findViewById<Button>(R.id.buttonPlusBleach).setOnClickListener {
            addOnBleachCount++
            updateAddOnText()
        }

        findViewById<Button>(R.id.buttonMinusBleach).setOnClickListener {
            if (addOnBleachCount > 0) {
                addOnBleachCount--
                updateAddOnText()
            }
        }

        findViewById<Button>(R.id.buttonPlusDetergent).setOnClickListener {
            addOnDetergentCount++
            updateAddOnText()
        }

        findViewById<Button>(R.id.buttonMinusDetergent).setOnClickListener {
            if (addOnDetergentCount > 0) {
                addOnDetergentCount--
                updateAddOnText()
            }
        }

        findViewById<Button>(R.id.buttonPlusConditioner).setOnClickListener {
            addOnFabricConditionerCount++
            updateAddOnText()
        }

        findViewById<Button>(R.id.buttonMinusConditioner).setOnClickListener {
            if (addOnFabricConditionerCount > 0) {
                addOnFabricConditionerCount--
                updateAddOnText()
            }
        }
    }

    private fun updateAddOnText() {
        findViewById<TextView>(R.id.textBleachAmount).text = getString(R.string.bleach_amount_format, addOnBleachCount)
        findViewById<TextView>(R.id.textDetergentAmount).text = getString(R.string.detergent_amount_format, addOnDetergentCount)
        findViewById<TextView>(R.id.textConditionerAmount).text = getString(R.string.fabric_conditioner_amount_format, addOnFabricConditionerCount)
        calculateTotalPrice()
    }
}
