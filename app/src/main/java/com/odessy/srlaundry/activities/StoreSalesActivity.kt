package com.odessy.srlaundry.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
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

    private lateinit var textViewTotalSales: TextView
    private lateinit var textViewDateRange: TextView
    private lateinit var recyclerViewSalesRecords: RecyclerView
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    private val storeSalesViewModel: StoreSalesViewModel by viewModels { StoreSalesViewModelFactory() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_sales)

        val buttonDailySales: Button = findViewById(R.id.buttonDailySales)
        val buttonWeeklySales: Button = findViewById(R.id.buttonWeeklySales)
        val buttonMonthlySales: Button = findViewById(R.id.buttonMonthlySales)
        textViewTotalSales = findViewById(R.id.textViewTotalSales)
        textViewDateRange = findViewById(R.id.textViewDateRange)
        recyclerViewSalesRecords = findViewById(R.id.recyclerViewSalesRecords)
        recyclerViewSalesRecords.layoutManager = LinearLayoutManager(this)

        buttonDailySales.setOnClickListener {
            showDatePickerDialog { year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                storeSalesViewModel.loadDailySales(selectedDate.time)
                textViewDateRange.text = "Daily Sales: ${dateFormatter.format(selectedDate.time)}"
            }
        }

        buttonWeeklySales.setOnClickListener {
            showDatePickerDialog { year, month, day ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(year, month, day)
                storeSalesViewModel.loadWeeklySales(selectedDate.time)
                textViewDateRange.text = "Weekly Sales: ${dateFormatter.format(getStartOfWeek(selectedDate).time)} - ${dateFormatter.format(getEndOfWeek(selectedDate).time)}"
            }
        }

        buttonMonthlySales.setOnClickListener {
            showMonthPickerDialog { year, month ->
                val selectedDate = Calendar.getInstance().apply {
                    set(year, month, 1)
                }
                storeSalesViewModel.loadMonthlySales(selectedDate.time)
                textViewDateRange.text = "Monthly Sales: ${SimpleDateFormat("MMM yyyy", Locale.getDefault()).format(selectedDate.time)}"
            }
        }

        storeSalesViewModel.totalSales.observe(this) { total ->
            textViewTotalSales.text = "Total Sales: ₱%.2f".format(total)
        }

        storeSalesViewModel.salesRecords.observe(this) { salesRecords ->
            recyclerViewSalesRecords.adapter = TransactionAdapter(salesRecords)
        }
    }

    private fun getStartOfWeek(calendar: Calendar): Calendar {
        return calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    private fun getEndOfWeek(calendar: Calendar): Calendar {
        return calendar.apply {
            set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }
    }

    private fun showDatePickerDialog(onDateSet: (year: Int, month: Int, day: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth -> onDateSet(year, month, dayOfMonth) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showMonthPickerDialog(onMonthSet: (year: Int, month: Int) -> Unit) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, _ -> onMonthSet(year, month) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            1
        ).apply {
            datePicker.findViewById<View>(resources.getIdentifier("day", "id", "android"))?.visibility = View.GONE
        }.show()
    }
}
