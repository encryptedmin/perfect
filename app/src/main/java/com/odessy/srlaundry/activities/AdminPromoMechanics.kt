package com.odessy.srlaundry.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.Promotion
import com.odessy.srlaundry.others.PromoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AdminPromoMechanics : AppCompatActivity() {

    private lateinit var promoViewModel: PromoViewModel
    private lateinit var promoSwitch: Switch
    private lateinit var serviceFrequencyEditText: EditText
    private lateinit var savePromoButton: Button
    private lateinit var backButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_promo_mechanics)

        // Initialize views
        promoSwitch = findViewById(R.id.promoSwitch)
        serviceFrequencyEditText = findViewById(R.id.serviceFrequencyEditText)
        savePromoButton = findViewById(R.id.savePromoButton)
        backButton = findViewById(R.id.backButton)

        // Initialize ViewModel
        promoViewModel = ViewModelProvider(this)[PromoViewModel::class.java]

        // Set up click listeners
        savePromoButton.setOnClickListener {
            savePromoSettings()
        }

        backButton.setOnClickListener {
            onBackPressed() // Go back to previous activity
        }

        // Load the current promo settings
        loadPromoSettings()
    }

    private fun savePromoSettings() {
        val isPromoActive = promoSwitch.isChecked
        val serviceFrequencyText = serviceFrequencyEditText.text.toString()

        // Input validation
        if (serviceFrequencyText.isEmpty()) {
            Toast.makeText(this, "Please enter service frequency", Toast.LENGTH_SHORT).show()
            return
        }

        val serviceFrequency = serviceFrequencyText.toInt()

        // Create a Promotion object with the data
        val promotion = Promotion(serviceFrequency = serviceFrequency, isPromoActive = isPromoActive)

        // Save the promo to the database
        CoroutineScope(Dispatchers.IO).launch {
            promoViewModel.insertOrUpdatePromo(promotion)
            runOnUiThread {
                Toast.makeText(this@AdminPromoMechanics, "Promotion settings saved", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPromoSettings() {
        promoViewModel.getPromo().observe(this) { promo ->
            promo?.let {
                promoSwitch.isChecked = it.isPromoActive
                serviceFrequencyEditText.setText(it.serviceFrequency.toString())
            }
        }
    }
}
