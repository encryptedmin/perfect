package com.odessy.srlaundry.activities

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.Promotion
import com.odessy.srlaundry.others.PromoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminPromoMechanics : AppCompatActivity() {

    private lateinit var promoViewModel: PromoViewModel
    private lateinit var promoSwitch: Switch
    private lateinit var serviceFrequencyEditText: EditText
    private lateinit var savePromoButton: Button
    private lateinit var backButton: Button
    private val firestoreDb = FirebaseFirestore.getInstance()
    private val promotionCollection = firestoreDb.collection("promotions")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_promo_mechanics)
        promoSwitch = findViewById(R.id.promoSwitch)
        serviceFrequencyEditText = findViewById(R.id.serviceFrequencyEditText)
        savePromoButton = findViewById(R.id.savePromoButton)
        backButton = findViewById(R.id.backButton)
        promoViewModel = ViewModelProvider(this)[PromoViewModel::class.java]
        savePromoButton.setOnClickListener {
            savePromoSettings()
        }
        backButton.setOnClickListener {
            onBackPressed()
        }
        loadPromoSettings()
    }
    private fun savePromoSettings() {
        val isPromoActive = promoSwitch.isChecked
        val serviceFrequencyText = serviceFrequencyEditText.text.toString()
        if (serviceFrequencyText.isEmpty()) {
            Toast.makeText(this, "Please enter service frequency", Toast.LENGTH_SHORT).show()
            return
        }
        val serviceFrequency = serviceFrequencyText.toInt()
        val promotion = Promotion(serviceFrequency = serviceFrequency, isPromoActive = isPromoActive)
        CoroutineScope(Dispatchers.IO).launch {
            savePromoToFirestore(promotion)
            promoViewModel.insertOrUpdatePromo(promotion)
            withContext(Dispatchers.Main) {
                Toast.makeText(this@AdminPromoMechanics, "Promotion settings saved", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun savePromoToFirestore(promotion: Promotion) {
        val promoData = hashMapOf(
            "serviceFrequency" to promotion.serviceFrequency,
            "isPromoActive" to promotion.isPromoActive
        )
        promotionCollection.document("currentPromo")
            .set(promoData)
            .addOnSuccessListener {
                runOnUiThread {
                    Toast.makeText(this, "Promotion settings updated in Firestore", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                runOnUiThread {
                    Toast.makeText(this, "Failed to update promotion in Firestore: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
    }
    private fun loadPromoSettings() {
        promotionCollection.document("currentPromo")
            .get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val serviceFrequency = document.getLong("serviceFrequency")?.toInt() ?: 1
                    val isPromoActive = document.getBoolean("isPromoActive") ?: false
                    promoSwitch.isChecked = isPromoActive
                    serviceFrequencyEditText.setText(serviceFrequency.toString())
                    val promotion = Promotion(serviceFrequency = serviceFrequency, isPromoActive = isPromoActive)
                    promoViewModel.insertOrUpdatePromo(promotion)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load promotion settings from Firestore: ${e.message}", Toast.LENGTH_LONG).show()
            }
        promoViewModel.getPromo().observe(this) { promo ->
            promo?.let {
                promoSwitch.isChecked = it.isPromoActive
                serviceFrequencyEditText.setText(it.serviceFrequency.toString())
            }
        }
    }
}
