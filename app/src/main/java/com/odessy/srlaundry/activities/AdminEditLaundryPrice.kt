package com.odessy.srlaundry.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.R
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.LaundryPrice
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminEditLaundryPrice : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var firestoreDb: FirebaseFirestore
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
    private var currentPrices: LaundryPrice? = null // Nullable LaundryPrice for Room

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_edit_laundry_price)

        // Initialize the database and Firestore
        db = AppDatabase.getDatabase(this, lifecycleScope)
        firestoreDb = FirebaseFirestore.getInstance()

        // Find views
        regularPriceInput = findViewById(R.id.editTextRegularPrice)
        bedSheetPriceInput = findViewById(R.id.editTextBedSheetPrice)
        detergentAddOnInput = findViewById(R.id.editTextDetergentAddOn)
        fabricConditionerAddOnInput = findViewById(R.id.editTextFabricConditionerAddOn)
        bleachAddOnInput = findViewById(R.id.editTextBleachAddOn)
        updateButton = findViewById(R.id.buttonUpdatePrices)
        currentRegularPrice = findViewById(R.id.textViewCurrentRegularPrice)
        currentBedSheetPrice = findViewById(R.id.textViewCurrentBedSheetPrice)
        currentDetergentAddOnPrice = findViewById(R.id.textViewCurrentDetergentAddOnPrice)
        currentFabricConditionerAddOnPrice = findViewById(R.id.textViewCurrentFabricConditionerAddOnPrice)
        currentBleachAddOnPrice = findViewById(R.id.textViewCurrentBleachAddOnPrice)

        // Load the latest prices from Firestore and sync with Room
        loadPricesFromFirestore()

        // Handle the update button click
        updateButton.setOnClickListener {
            updatePrices()
        }

        // Cancel button functionality
        findViewById<Button>(R.id.buttonCancel).setOnClickListener {
            startActivity(Intent(this@AdminEditLaundryPrice, AdminDashboard::class.java))
        }
    }

    private fun updatePrices() {
        val regularPrice = regularPriceInput.text.toString().toDoubleOrNull()
        val bedSheetPrice = bedSheetPriceInput.text.toString().toDoubleOrNull()
        val detergentAddOnPrice = detergentAddOnInput.text.toString().toDoubleOrNull()
        val fabricConditionerAddOnPrice = fabricConditionerAddOnInput.text.toString().toDoubleOrNull()
        val bleachAddOnPrice = bleachAddOnInput.text.toString().toDoubleOrNull()

        if (regularPrice != null && bedSheetPrice != null && detergentAddOnPrice != null &&
            fabricConditionerAddOnPrice != null && bleachAddOnPrice != null) {

            // Create LaundryPrice object
            val updatedPrice = LaundryPrice(
                id = currentPrices?.id ?: 1, // Use existing ID or default
                regular = regularPrice,
                bedSheet = bedSheetPrice,
                addOnDetergent = detergentAddOnPrice,
                addOnFabricConditioner = fabricConditionerAddOnPrice,
                addOnBleach = bleachAddOnPrice
            )

            // Update in Room and Firestore
            lifecycleScope.launch(Dispatchers.IO) {
                db.laundryPriceDao().updatePrices(updatedPrice)
                savePricesToFirestore(updatedPrice) // Sync with Firestore
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminEditLaundryPrice, "Prices updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Please fill in all fields with valid numbers", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePricesToFirestore(prices: LaundryPrice) {
        val priceData = hashMapOf(
            "regular" to prices.regular,
            "bedSheet" to prices.bedSheet,
            "addOnDetergent" to prices.addOnDetergent,
            "addOnFabricConditioner" to prices.addOnFabricConditioner,
            "addOnBleach" to prices.addOnBleach
        )

        firestoreDb.collection("laundry_prices").document("current_prices")
            .set(priceData)
            .addOnSuccessListener {
                runOnUiThread {
                    Toast.makeText(this@AdminEditLaundryPrice, "Prices updated in Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    Toast.makeText(this@AdminEditLaundryPrice, "Error updating prices in Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun loadPricesFromFirestore() {
        firestoreDb.collection("laundry_prices").document("current_prices")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val regular = document.getDouble("regular") ?: 0.0
                    val bedSheet = document.getDouble("bedSheet") ?: 0.0
                    val detergent = document.getDouble("addOnDetergent") ?: 0.0
                    val fabricConditioner = document.getDouble("addOnFabricConditioner") ?: 0.0
                    val bleach = document.getDouble("addOnBleach") ?: 0.0

                    // Update Room with Firestore prices
                    val firestorePrices = LaundryPrice(
                        id = 1, // Assume only one set of prices for the app
                        regular = regular,
                        bedSheet = bedSheet,
                        addOnDetergent = detergent,
                        addOnFabricConditioner = fabricConditioner,
                        addOnBleach = bleach
                    )

                    lifecycleScope.launch(Dispatchers.IO) {
                        db.laundryPriceDao().updatePrices(firestorePrices)
                        withContext(Dispatchers.Main) {
                            populateCurrentPrices(firestorePrices)
                        }
                    }
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load prices from Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }

        // Load prices from Room as a fallback
        lifecycleScope.launch(Dispatchers.IO) {
            currentPrices = db.laundryPriceDao().getLaundryPrice()
            currentPrices?.let {
                withContext(Dispatchers.Main) {
                    populateCurrentPrices(it)
                }
            }
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
