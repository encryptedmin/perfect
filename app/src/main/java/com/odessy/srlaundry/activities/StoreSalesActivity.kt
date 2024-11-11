package com.odessy.srlaundry.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.adapters.TransactionAdapter
import com.odessy.srlaundry.others.StoreSalesViewModel
import java.util.*

class StoreSalesActivity : AppCompatActivity() {

    private lateinit var storeSalesViewModel: StoreSalesViewModel
    private lateinit var textViewTotalSales: TextView
    private lateinit var recyclerViewSalesRecords: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_sales)

        val buttonDailySales: Button = findViewById(R.id.buttonDailySales)
        val buttonWeeklySales: Button = findViewById(R.id.buttonWeeklySales)
        val buttonMonthlySales: Button = findViewById(R.id.buttonMonthlySales)
        textViewTotalSales = findViewById(R.id.textViewTotalSales)
        recyclerViewSalesRecords = findViewById(R.id.recyclerViewSalesRecords)

        // Setup RecyclerView
        recyclerViewSalesRecords.layoutManager = LinearLayoutManager(this)

        storeSalesViewModel = ViewModelProvider(this).get(StoreSalesViewModel::class.java)

        // Daily sales with date picker
        buttonDailySales.setOnClickListener {
            showDatePickerDialog { year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                storeSalesViewModel.loadDailySales(selectedDate.time)
            }
        }

        // Weekly sales with date range picker (start date only)
        buttonWeeklySales.setOnClickListener {
            showDatePickerDialog { year, month, day ->
                val startDate = Calendar.getInstance()
                startDate.set(year, month, day)

                // Calculate the Sunday and Saturday of the week
                val weekStart = getStartOfWeek(startDate)
                val weekEnd = getEndOfWeek(startDate)

                storeSalesViewModel.loadWeeklySales(weekStart.time, weekEnd.time)
            }
        }

        // Monthly sales by selecting the first day of the month
        buttonMonthlySales.setOnClickListener {
            showMonthPickerDialog { year, month ->
                storeSalesViewModel.loadMonthlySales(year, month)
            }
        }

        // Observe ViewModel LiveData for sales records and total sales
        storeSalesViewModel.totalSales.observe(this, { total ->
            textViewTotalSales.text = "Total Sales: ₱$total"
        })

        storeSalesViewModel.salesRecords.observe(this, { salesRecords ->
            // Update RecyclerView with sales records
            val adapter = TransactionAdapter(salesRecords)
            recyclerViewSalesRecords.adapter = adapter
        })
    }

    // Helper function to calculate the start (Sunday) of the week
    private fun getStartOfWeek(calendar: Calendar): Calendar {
        val weekStart = calendar.clone() as Calendar
        weekStart.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        return weekStart
    }

    // Helper function to calculate the end (Saturday) of the week
    private fun getEndOfWeek(calendar: Calendar): Calendar {
        val weekEnd = calendar.clone() as Calendar
        weekEnd.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
        return weekEnd
    }

    // Show DatePickerDialog for daily and weekly date selection
    private fun showDatePickerDialog(onDateSet: (year: Int, month: Int, day: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                onDateSet(year, month, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Show DatePickerDialog for monthly selection (will always pick the first day of the selected month)
    private fun showMonthPickerDialog(onMonthSet: (year: Int, month: Int) -> Unit) {
        val calendar = Calendar.getInstance()

        // Use DatePickerDialog to let the user select the first day of the month
        DatePickerDialog(
            this,
            { _, year, month, _ ->
                // Always set the day to 1 (start of the selected month)
                onMonthSet(year, month)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1  // Default day is 1
        ).show()
    }
}
