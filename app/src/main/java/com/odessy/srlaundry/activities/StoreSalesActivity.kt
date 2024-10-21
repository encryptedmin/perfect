package com.odessy.srlaundry.activities

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.Transaction
import java.text.SimpleDateFormat
import java.util.*

class StoreSalesActivity : AppCompatActivity() {

    private lateinit var buttonDailySales: Button
    private lateinit var buttonWeeklySales: Button
    private lateinit var buttonMonthlySales: Button
    private lateinit var listViewSalesRecords: ListView
    private lateinit var textViewTotalSales: TextView
    private lateinit var textViewDateRange: TextView

    private val firestoreDb = FirebaseFirestore.getInstance().collection("transactions")
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_sales)
        buttonDailySales = findViewById(R.id.buttonDailySales)
        buttonWeeklySales = findViewById(R.id.buttonWeeklySales)
        buttonMonthlySales = findViewById(R.id.buttonMonthlySales)
        listViewSalesRecords = findViewById(R.id.listViewSalesRecords)
        textViewTotalSales = findViewById(R.id.textViewTotalSales)
        textViewDateRange = findViewById(R.id.textViewDateRange)
        buttonDailySales.setOnClickListener { fetchSalesData("daily") }
        buttonWeeklySales.setOnClickListener { fetchSalesData("weekly") }
        buttonMonthlySales.setOnClickListener { fetchSalesData("monthly") }
        fetchSalesData("daily")
    }
    private fun fetchSalesData(period: String) {
        val calendar = Calendar.getInstance()
        val startDate: Date
        val endDate = Date()
        when (period) {
            "daily" -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                startDate = calendar.time
                textViewDateRange.text = "Today's Sales"
            }
            "weekly" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                startDate = calendar.time
                textViewDateRange.text = "Last 7 Days Sales"
            }
            "monthly" -> {
                calendar.add(Calendar.MONTH, -1)
                startDate = calendar.time
                textViewDateRange.text = "Last 30 Days Sales"
            }
            else -> return
        }
        firestoreDb
            .whereGreaterThanOrEqualTo("timestamp", startDate)
            .whereLessThanOrEqualTo("timestamp", endDate)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val transactions = documents.toObjects(Transaction::class.java)
                val salesRecords = transactions.map {
                    "Product: ${it.productName}, Qty: ${it.quantity}, Total: ₱${it.totalPrice}"
                }
                val adapter = ArrayAdapter(
                    this,
                    android.R.layout.simple_list_item_1,
                    salesRecords
                )
                listViewSalesRecords.adapter = adapter

                val totalSales = transactions.sumOf { it.totalPrice }
                textViewTotalSales.text = "Total Sales: ₱${"%.2f".format(totalSales)}"
            }
            .addOnFailureListener { e ->

                e.printStackTrace()
            }
    }
}
