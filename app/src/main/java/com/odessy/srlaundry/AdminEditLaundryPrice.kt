package com.odessy.srlaundry

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.LaundryPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminEditLaundryPrice : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var regularPriceInput: EditText
    private lateinit var bedSheetPriceInput: EditText
    private lateinit var detergentAddOnInput: EditText
    private lateinit var fabricConditionerAddOnInput: EditText
    private lateinit var bleachAddOnInput: EditText
    private lateinit var updateButton: Button
    private lateinit var currentRegularPrice: TextView
    private lateinit var currentBedSheetPrice: TextView
    private lateinit var currentDetergentAddOnPrice: TextView
    private lateinit var currentFabricConditionerAddOnPrice: TextView
    private lateinit var currentBleachAddOnPrice: TextView
    private var currentPrices: LaundryPrice? = null  // Declare currentPrices as nullable without lateinit

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_laundry_price)

        // Initialize the database
        db = AppDatabase.getDatabase(this, lifecycleScope)

        // Find views
        regularPriceInput = findViewById(R.id.editTextRegularPrice)
        bedSheetPriceInput = findViewById(R.id.editTextBedSheetPrice)
        detergentAddOnInput = findViewById(R.id.editTextDetergentAddOn)
        fabricConditionerAddOnInput = findViewById(R.id.editTextFabricConditionerAddOn)
        bleachAddOnInput = findViewById(R.id.editTextBleachAddOn)
        updateButton = findViewById(R.id.buttonUpdatePrices)

        // Find current price TextViews
        currentRegularPrice = findViewById(R.id.textViewCurrentRegularPrice)
        currentBedSheetPrice = findViewById(R.id.textViewCurrentBedSheetPrice)
        currentDetergentAddOnPrice = findViewById(R.id.textViewCurrentDetergentAddOnPrice)
        currentFabricConditionerAddOnPrice = findViewById(R.id.textViewCurrentFabricConditionerAddOnPrice)
        currentBleachAddOnPrice = findViewById(R.id.textViewCurrentBleachAddOnPrice)

        // Load the existing laundry prices
        lifecycleScope.launch(Dispatchers.IO) {
            currentPrices = db.laundryPriceDao().getLaundryPrice()
            currentPrices?.let {
                runOnUiThread {
                    populateCurrentPrices(it)
                }
            }
        }

        // Handle the update button
        updateButton.setOnClickListener {
            val regularPrice = regularPriceInput.text.toString().toDoubleOrNull()
            val bedSheetPrice = bedSheetPriceInput.text.toString().toDoubleOrNull()
            val detergentAddOnPrice = detergentAddOnInput.text.toString().toDoubleOrNull()
            val fabricConditionerAddOnPrice = fabricConditionerAddOnInput.text.toString().toDoubleOrNull()
            val bleachAddOnPrice = bleachAddOnInput.text.toString().toDoubleOrNull()

            if (regularPrice != null && bedSheetPrice != null && detergentAddOnPrice != null &&
                fabricConditionerAddOnPrice != null && bleachAddOnPrice != null) {
                lifecycleScope.launch(Dispatchers.IO) {
                    val updatedPrice = LaundryPrice(
                        id = currentPrices?.id ?: 1, // Use existing ID for update
                        regular = regularPrice,
                        bedSheet = bedSheetPrice,
                        addOnDetergent = detergentAddOnPrice,
                        addOnFabricConditioner = fabricConditionerAddOnPrice,
                        addOnBleach = bleachAddOnPrice
                    )
                    db.laundryPriceDao().updatePrices(updatedPrice)
                    runOnUiThread {
                        Toast.makeText(this@AdminEditLaundryPrice, "Prices updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please fill in all fields with valid numbers", Toast.LENGTH_SHORT).show()
            }
        }

        // Cancel button functionality
        findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            startActivity(Intent(this@AdminEditLaundryPrice, AdminDashboard::class.java))
        }
    }

    private fun populateCurrentPrices(prices: LaundryPrice) {
        currentRegularPrice.text = prices.regular.toString()
        currentBedSheetPrice.text = prices.bedSheet.toString()
        currentDetergentAddOnPrice.text = prices.addOnDetergent.toString()
        currentFabricConditionerAddOnPrice.text = prices.addOnFabricConditioner.toString()
        currentBleachAddOnPrice.text = prices.addOnBleach.toString()
    }
}
