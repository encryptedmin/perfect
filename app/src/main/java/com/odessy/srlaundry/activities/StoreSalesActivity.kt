package com.odessy.srlaundry.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.others.TransactionAdapter
import com.odessy.srlaundry.others.StoreSalesViewModel
import com.odessy.srlaundry.others.StoreSalesViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class StoreSalesActivity : AppCompatActivity() {

    private lateinit var transactionRecyclerView: RecyclerView
    private lateinit var textTotalSales: TextView
    private lateinit var buttonFromDate: Button
    private lateinit var buttonToDate: Button

    private val viewModel: StoreSalesViewModel by viewModels { StoreSalesViewModelFactory(applicationContext) }

    private var fromDate: Long? = null
    private var toDate: Long? = null
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_sales)

        // Initialize UI components
        transactionRecyclerView = findViewById(R.id.transactionRecyclerView)
        textTotalSales = findViewById(R.id.textTotalSales)
        buttonFromDate = findViewById(R.id.buttonFromDate)
        buttonToDate = findViewById(R.id.buttonToDate)

        // Set up RecyclerView
        transactionRecyclerView.layoutManager = LinearLayoutManager(this)
        val transactionAdapter = TransactionAdapter()
        transactionRecyclerView.adapter = transactionAdapter

        // Observe transactions
        viewModel.transactions.observe(this, { transactions ->
            transactionAdapter.submitList(transactions)

            // Calculate total sales
            val totalSales = transactions.sumOf { it.totalPrice }
            textTotalSales.text = "Total Sales: $${"%.2f".format(totalSales)}"
        })

        // Handle From Date selection
        buttonFromDate.setOnClickListener {
            showDatePicker { date ->
                fromDate = date
                buttonFromDate.text = dateFormat.format(Date(date))
                filterTransactions()
            }
        }

        // Handle To Date selection
        buttonToDate.setOnClickListener {
            showDatePicker { date ->
                toDate = date
                buttonToDate.text = dateFormat.format(Date(date))
                filterTransactions()
            }
        }

        // Load initial data
        viewModel.loadTransactions()
    }

    private fun filterTransactions() {
        viewModel.loadTransactions(fromDate, toDate)
    }

    private fun showDatePicker(onDateSelected: (Long) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.timeInMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
