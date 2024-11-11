package com.odessy.srlaundry.activities

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.odessy.srlaundry.R
import com.odessy.srlaundry.adapters.SalesAdapter
import com.odessy.srlaundry.entities.LaundrySales
import com.odessy.srlaundry.viewmodel.AdminSalesViewModel
import java.util.*

class AdminSalesActivity : AppCompatActivity() {

    private lateinit var tabLayout: TabLayout
    private lateinit var btnSelectDate: Button
    private lateinit var tvSalesTotal: TextView
    private lateinit var rvSalesRecords: RecyclerView
    private val salesAdapter = SalesAdapter()
    private val adminSalesViewModel: AdminSalesViewModel by viewModels()
    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_sales)
        initializeViews()

        rvSalesRecords.layoutManager = LinearLayoutManager(this)
        rvSalesRecords.adapter = salesAdapter

        // Observe data updates from the ViewModel
        adminSalesViewModel.salesRecords.observe(this) { salesList ->
            Log.d("AdminSalesActivity", "Sales list updated with ${salesList.size} items.")
            salesAdapter.submitList(salesList)
            updateSalesTotal(salesList)
        }

        // Listener for tab selection to update filter label
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                updateFilterLabel(tab?.position ?: 0)
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // Listener for btnSelectDate to show different date pickers based on tab
        btnSelectDate.setOnClickListener {
            val selectedTab = tabLayout.selectedTabPosition
            when (selectedTab) {
                0 -> showDatePicker { date -> // Daily Tab
                    adminSalesViewModel.fetchSalesData(
                        adminSalesViewModel.getStartOfDay(date),
                        adminSalesViewModel.getEndOfDay(date)
                    )
                }
                1 -> showDatePicker { startDate -> // Weekly Tab
                    val endDate = Calendar.getInstance().apply {
                        time = startDate
                        add(Calendar.DAY_OF_MONTH, 6) // One week from selected date
                    }.time
                    adminSalesViewModel.fetchSalesData(startDate, endDate)
                }
                2 -> showMonthPicker { selectedMonth -> // Monthly Tab
                    val endOfMonth = Calendar.getInstance().apply {
                        time = selectedMonth
                        set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                    }.time
                    adminSalesViewModel.fetchSalesData(selectedMonth, endOfMonth)
                }
            }
        }
    }

    private fun initializeViews() {
        tabLayout = findViewById(R.id.tabLayout)
        btnSelectDate = findViewById(R.id.btn_select_date)
        tvSalesTotal = findViewById(R.id.tv_sales_total)
        rvSalesRecords = findViewById(R.id.rv_sales_records)
    }

    private fun updateFilterLabel(tabPosition: Int) {
        btnSelectDate.text = when (tabPosition) {
            0 -> "Choose Date" // Daily
            1 -> "Choose Week Start Date" // Weekly
            2 -> "Choose Month" // Monthly
            else -> "Select Date"
        }
    }

    private fun showDatePicker(onDateSelected: (Date) -> Unit) {
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                onDateSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showMonthPicker(onMonthSelected: (Date) -> Unit) {
        DatePickerDialog(
            this,
            { _, year, month, _ ->
                calendar.set(year, month, 1) // Set to the first day of the selected month
                onMonthSelected(calendar.time)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Hide the day picker to allow only month selection
            datePicker.findViewById<View>(
                resources.getIdentifier("day", "id", "android")
            )?.visibility = View.GONE
        }.show()
    }

    private fun updateSalesTotal(salesList: List<LaundrySales>) {
        val totalSales = salesList.sumOf { it.totalPrice }
        tvSalesTotal.text = "Total Sales: ₱%.2f".format(totalSales)
    }
}
