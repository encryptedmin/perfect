package com.odessy.srlaundry.others

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.Promotion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PromoViewModel(application: Application) : AndroidViewModel(application) {

    private val promoDao = AppDatabase.getDatabase(application, viewModelScope).promotionDao()
    private val firestoreDb = FirebaseFirestore.getInstance().collection("promotions")

    // Existing function to get promo data from Room
    fun getPromo(): LiveData<Promotion?> {
        return promoDao.getPromo()
    }

    // Insert or update promotion settings locally
    fun insertOrUpdatePromo(promotion: Promotion) {
        viewModelScope.launch(Dispatchers.IO) {
            promoDao.insertOrUpdatePromo(promotion)
        }
    }

    // Sync Firestore promotion with Room (Firestore as the source of truth)
    fun syncPromoFromFirestore() {
        viewModelScope.launch(Dispatchers.IO) {
            firestoreDb.document("currentPromo").get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val serviceFrequency = document.getLong("serviceFrequency")?.toInt() ?: 1
                        val isPromoActive = document.getBoolean("isPromoActive") ?: false

                        // Create a Promotion object from Firestore data
                        val promotion = Promotion(
                            id = 1,  // Ensure only one entry
                            serviceFrequency = serviceFrequency,
                            isPromoActive = isPromoActive
                        )

                        // Clear local promotions and insert the synced one
                        viewModelScope.launch(Dispatchers.IO) {
                            promoDao.deleteAllPromos()
                            promoDao.insertOrUpdatePromo(promotion)
                        }
                    }
                }
                .addOnFailureListener { e ->
                    e.printStackTrace() // Handle Firestore failure
                }
        }
    }
}
