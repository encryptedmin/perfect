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
import com.odessy.srlaundry.entities.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class NewJobOrder : AppCompatActivity() {

    private lateinit var searchCustomerBar: EditText
    private lateinit var customerListView: ListView
    private lateinit var buttonCreateNewCustomer: Button
    private lateinit var radioGroupLaundryType: RadioGroup
    private lateinit var inputLaundryWeight: EditText
    private lateinit var textTotalPrice: TextView
    private lateinit var textTotalLoads: TextView // Field for showing total loads
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
        initializeViews()

        // Load laundry prices from the database
        loadLaundryPrices()

        // Search customer functionality
        setupCustomerSearch()

        // Radio group for laundry type selection
        setupLaundryTypeSelection()

        // Laundry weight input
        setupWeightInput()

        // Button functionalities
        setupButtonFunctions()

        // Setup add-on buttons
        setupAddOnButtons()
    }

    private fun initializeViews() {
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
    }

    private fun setupCustomerSearch() {
        searchCustomerBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchCustomer(s.toString())
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        customerListView.setOnItemClickListener { _, _, position, _ ->
            selectedCustomer = customerListView.getItemAtPosition(position) as? Customer
            buttonConfirm.isEnabled = selectedCustomer != null // Enable Confirm button once a customer is selected
        }
    }

    private fun setupLaundryTypeSelection() {
        radioGroupLaundryType.setOnCheckedChangeListener { _, checkedId ->
            selectedLaundryType = when (checkedId) {
                R.id.radioRegular -> "Regular"
                R.id.radioBedsheets -> "Bedsheets"
                else -> "Regular"
            }
            calculateTotalPrice()
        }
    }

    private fun setupWeightInput() {
        inputLaundryWeight.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                calculateTotalPrice()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupButtonFunctions() {
        buttonCreateNewCustomer.setOnClickListener {
            startActivity(Intent(this@NewJobOrder, NewCustomer::class.java)) // Start new customer activity
        }

        buttonCancel.setOnClickListener {
            startActivity(Intent(this@NewJobOrder, UserLaundry::class.java))
        }

        buttonClearFields.setOnClickListener {
            clearFields()
        }

        buttonConfirm.setOnClickListener {
            // Launch a coroutine to ensure the JobOrder is created before redirecting
            lifecycleScope.launch {
                createJobOrder() // Wait until job order is created
                startActivity(Intent(this@NewJobOrder, UserLaundry::class.java)) // Redirect after job order creation
            }
        }
    }

    private fun loadLaundryPrices() {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@NewJobOrder, lifecycleScope)
            val fetchedLaundryPrice = db.laundryPriceDao().getLaundryPrice()

            withContext(Dispatchers.Main) {
                laundryPrice = fetchedLaundryPrice ?: LaundryPrice(
                    regular = 0.0,
                    bedSheet = 0.0,
                    addOnDetergent = 0.0,
                    addOnFabricConditioner = 0.0,
                    addOnBleach = 0.0
                )
                if (fetchedLaundryPrice == null) {
                    Toast.makeText(
                        this@NewJobOrder,
                        getString(R.string.laundry_price_not_found),
                        Toast.LENGTH_LONG
                    ).show()
                }
                calculateTotalPrice()
            }
        }
    }

    private fun searchCustomer(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@NewJobOrder, lifecycleScope)
            val customers = db.customerDao().searchCustomers(query)
            val adapter = ArrayAdapter(this@NewJobOrder, android.R.layout.simple_list_item_1, customers)

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
        textTotalLoads.text = getString(R.string.total_loads_format, 0)
        addOnBleachCount = 0
        addOnDetergentCount = 0
        addOnFabricConditionerCount = 0
        updateAddOnText()
    }

    // Suspend function to ensure job order creation completes before redirection
    private suspend fun createJobOrder() {
        val weight = inputLaundryWeight.text.toString().toDoubleOrNull() ?: 0.0
        val loadSize = if (selectedLaundryType == "Regular") 8.0 else 6.0
        val loads = Math.ceil(weight / loadSize).toInt()

        // Fetch the current promotion details
        val db = AppDatabase.getDatabase(this@NewJobOrder, lifecycleScope)
        val promotion = withContext(Dispatchers.IO) { db.promotionDao().getActivePromotion() }

        var isPromoApplied = false
        var finalTotalPrice = totalPrice

        // Check if the promo is active and customer qualifies
        if (promotion != null && promotion.isPromoActive && selectedCustomer != null) {
            if (selectedCustomer!!.promo >= promotion.serviceFrequency) {
                // Apply promo by deducting the price of one load (based on selected laundry type)
                val pricePerLoad = if (selectedLaundryType == "Regular") laundryPrice.regular else laundryPrice.bedSheet
                finalTotalPrice -= pricePerLoad // Deduct price for 1 free load
                isPromoApplied = true
                selectedCustomer!!.promo = 0 // Reset promo count after applying
            } else {
                selectedCustomer!!.promo += 1 // Increment promo count if promo not applied
            }
        }

        // Create the JobOrder with updated final price
        val jobOrder = JobOrder(
            customerName = selectedCustomer?.name ?: "",
            customerPhone = selectedCustomer?.phone ?: "",
            weight = weight,
            loads = loads,
            addOnDetergent = addOnDetergentCount,
            addOnFabricConditioner = addOnFabricConditionerCount,
            addOnBleach = addOnBleachCount,
            totalPrice = finalTotalPrice, // Use the updated total price after promo application
            laundryType = selectedLaundryType,
            isActive = true
        )

        // Insert job order and update customer promo count
        withContext(Dispatchers.IO) {
            db.jobOrderDao().insertJobOrder(jobOrder)

            // Update customer's promo count
            selectedCustomer?.let {
                db.customerDao().updateCustomerPromo(it.id, it.promo)
            }
        }

        // Show a message indicating whether a promo was applied
        withContext(Dispatchers.Main) {
            if (isPromoApplied) {
                Toast.makeText(this@NewJobOrder, "Promo applied! 1 load free.", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this@NewJobOrder, "Job order created.", Toast.LENGTH_SHORT).show()
            }
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
