package com.odessy.srlaundry

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.dao.JobOrderDao
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.databinding.ActivityAdminSalesBinding
import java.util.*

class activity_admin_sales : AppCompatActivity() {
    private lateinit var binding: ActivityAdminSalesBinding

    private val salesViewModel: SalesViewModel by viewModels {
        val jobOrderDao: JobOrderDao = AppDatabase.getDatabase(application, lifecycleScope).jobOrderDao()
        SalesViewModelFactory(jobOrderDao, lifecycleScope)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAdminSalesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up button listeners for Daily, Weekly, and Monthly sales
        setupButtonListeners()

        // Fetch and display today's sales as default when the activity starts
        val todayStart = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val todayEnd = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        fetchAndDisplaySalesData(todayStart, todayEnd) // Default is today's sales
    }

    private fun setupButtonListeners() {
        // Fetch daily sales data
        binding.dailyButton.setOnClickListener {
            val (startDate, endDate) = getDailyRange()
            fetchAndDisplaySalesData(startDate, endDate)
        }

        // Fetch weekly sales data
        binding.weeklyButton.setOnClickListener {
            val (startDate, endDate) = getWeeklyRange()
            fetchAndDisplaySalesData(startDate, endDate)
        }

        // Fetch monthly sales data
        binding.monthlyButton.setOnClickListener {
            val (startDate, endDate) = getMonthlyRange()
            fetchAndDisplaySalesData(startDate, endDate)
        }
    }

    private fun fetchAndDisplaySalesData(startDate: Long, endDate: Long) {
        // Fetch the sales data from ViewModel
        salesViewModel.fetchSalesData(startDate, endDate) { salesData ->
            // Update UI with fetched sales data
            binding.totalIncomeTextView.text = getString(R.string.total_income, salesData.totalIncome.toString())
            binding.regularLoadsTextView.text = getString(R.string.total_regular_loads, salesData.totalLoads.toString())
            binding.detergentAddonsTextView.text = getString(R.string.detergent_addons, salesData.totalDetergentAddons.toString())
            binding.fabricConditionerAddonsTextView.text = getString(R.string.fabric_conditioner_addons, salesData.totalFabricConditionerAddons.toString())
            binding.bleachAddonsTextView.text = getString(R.string.bleach_addons, salesData.totalBleachAddons.toString())
        }
    }

    // Get the daily date range (start and end of today)
    private fun getDailyRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }.timeInMillis

        val end = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        return Pair(start, end)
    }

    // Get the weekly date range (Monday to Sunday)
    private fun getWeeklyRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val start = calendar.timeInMillis

        calendar.add(Calendar.DATE, 6) // Set to Sunday
        val end = calendar.timeInMillis

        return Pair(start, end)
    }

    // Get the monthly date range (start and end of the current month)
    private fun getMonthlyRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Set to the first day of the month
        val start = calendar.timeInMillis

        calendar.add(Calendar.MONTH, 1) // Move to the next month
        calendar.set(Calendar.DAY_OF_MONTH, 1) // Set to the first day of the next month
        calendar.add(Calendar.DATE, -1) // Go back one day to get the last day of this month
        val end = calendar.timeInMillis

        return Pair(start, end)
    }
}
