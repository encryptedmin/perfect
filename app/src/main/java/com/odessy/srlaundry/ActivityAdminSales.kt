package com.odessy.srlaundry

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.odessy.srlaundry.database.AppDatabase // Ensure this is the correct import for your database
import java.util.Calendar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class ActivityAdminSales : AppCompatActivity() {

    // Define a CoroutineScope
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    // Initialize the JobOrderDao directly using your AppDatabase with the coroutine scope
    private val jobOrderDao by lazy { AppDatabase.getDatabase(applicationContext, coroutineScope).jobOrderDao() }

    // Initialize the ViewModel using the factory pattern
    private val salesViewModel: SalesViewModel by viewModels {
        SalesViewModelFactory(jobOrderDao)
    }

    // Variables to hold the start and end dates
    private var startDate: Long = 0
    private var endDate: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_sales)

        // Initialize the views for displaying data
        val regularSalesTextView: TextView = findViewById(R.id.tv_regular_total_sales)
        val regularLoadsTextView: TextView = findViewById(R.id.tv_regular_total_loads)
        val regularDetergentTextView: TextView = findViewById(R.id.tv_regular_addon_detergent)
        val regularBleachTextView: TextView = findViewById(R.id.tv_regular_addon_bleach)
        val regularFabricConditionerTextView: TextView = findViewById(R.id.tv_regular_addon_fabric_conditioner)

        val bedsheetsSalesTextView: TextView = findViewById(R.id.tv_bedsheets_total_sales)
        val bedsheetsLoadsTextView: TextView = findViewById(R.id.tv_bedsheets_total_loads)
        val bedsheetsDetergentTextView: TextView = findViewById(R.id.tv_bedsheets_addon_detergent)
        val bedsheetsBleachTextView: TextView = findViewById(R.id.tv_bedsheets_addon_bleach)
        val bedsheetsFabricConditionerTextView: TextView = findViewById(R.id.tv_bedsheets_addon_fabric_conditioner)

        // Date filter buttons (assuming you have these in your XML layout)
        val dailyButton: Button = findViewById(R.id.btn_daily)
        val weeklyButton: Button = findViewById(R.id.btn_weekly)
        val monthlyButton: Button = findViewById(R.id.btn_monthly)
        val backButton: Button = findViewById(R.id.btn_back)
        // Set click listeners to set date ranges based on user selection
        dailyButton.setOnClickListener {
            setDatesForDaily()
            fetchSalesData(regularSalesTextView, regularLoadsTextView, regularDetergentTextView, regularBleachTextView, regularFabricConditionerTextView,
                bedsheetsSalesTextView, bedsheetsLoadsTextView, bedsheetsDetergentTextView, bedsheetsBleachTextView, bedsheetsFabricConditionerTextView)
        }

        weeklyButton.setOnClickListener {
            setDatesForWeekly()
            fetchSalesData(regularSalesTextView, regularLoadsTextView, regularDetergentTextView, regularBleachTextView, regularFabricConditionerTextView,
                bedsheetsSalesTextView, bedsheetsLoadsTextView, bedsheetsDetergentTextView, bedsheetsBleachTextView, bedsheetsFabricConditionerTextView)
        }

        monthlyButton.setOnClickListener {
            setDatesForMonthly()
            fetchSalesData(regularSalesTextView, regularLoadsTextView, regularDetergentTextView, regularBleachTextView, regularFabricConditionerTextView,
                bedsheetsSalesTextView, bedsheetsLoadsTextView, bedsheetsDetergentTextView, bedsheetsBleachTextView, bedsheetsFabricConditionerTextView)
        }

        backButton.setOnClickListener {
            finish()
        }

        // Initial load (default to daily)
        setDatesForDaily()
        fetchSalesData(regularSalesTextView, regularLoadsTextView, regularDetergentTextView, regularBleachTextView, regularFabricConditionerTextView,
            bedsheetsSalesTextView, bedsheetsLoadsTextView, bedsheetsDetergentTextView, bedsheetsBleachTextView, bedsheetsFabricConditionerTextView)
    }

    // Set the start and end dates for "Daily" (current date)
    private fun setDatesForDaily() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startDate = calendar.timeInMillis // Start of the day

        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        endDate = calendar.timeInMillis // End of the day
    }

    // Set the start and end dates for "Weekly" (current date + 6 days before)
    private fun setDatesForWeekly() {
        val currentDate = System.currentTimeMillis()
        startDate = currentDate - (6 * 24 * 60 * 60 * 1000L) // 6 days ago in milliseconds
        endDate = currentDate
    }

    // Set the start and end dates for "Monthly" (current date + 30 days before)
    private fun setDatesForMonthly() {
        val calendar = Calendar.getInstance()
        endDate = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_MONTH, -30) // Set start date to 30 days ago
        startDate = calendar.timeInMillis
    }

    // Fetch sales data from ViewModel
    private fun fetchSalesData(
        regularSalesTextView: TextView,
        regularLoadsTextView: TextView,
        regularDetergentTextView: TextView,
        regularBleachTextView: TextView,
        regularFabricConditionerTextView: TextView,
        bedsheetsSalesTextView: TextView,
        bedsheetsLoadsTextView: TextView,
        bedsheetsDetergentTextView: TextView,
        bedsheetsBleachTextView: TextView,
        bedsheetsFabricConditionerTextView: TextView
    ) {
        salesViewModel.fetchSalesDataForLaundryTypes(startDate, endDate) { result ->
            // Update Regular Laundry Data
            regularSalesTextView.text = getString(R.string.total_sales, result.regularSales.totalIncome.toString())
            regularLoadsTextView.text = getString(R.string.total_loads, result.regularSales.totalLoads)
            regularDetergentTextView.text = getString(R.string.addon_detergent, result.regularSales.totalDetergentAddons)
            regularBleachTextView.text = getString(R.string.addon_bleach, result.regularSales.totalBleachAddons)
            regularFabricConditionerTextView.text = getString(R.string.addon_fabric_conditioner, result.regularSales.totalFabricConditionerAddons)

            // Update Bedsheets Laundry Data
            bedsheetsSalesTextView.text = getString(R.string.total_sales, result.bedsheetsSales.totalIncome.toString())
            bedsheetsLoadsTextView.text = getString(R.string.total_loads, result.bedsheetsSales.totalLoads)
            bedsheetsDetergentTextView.text = getString(R.string.addon_detergent, result.bedsheetsSales.totalDetergentAddons)
            bedsheetsBleachTextView.text = getString(R.string.addon_bleach, result.bedsheetsSales.totalBleachAddons)
            bedsheetsFabricConditionerTextView.text = getString(R.string.addon_fabric_conditioner, result.bedsheetsSales.totalFabricConditionerAddons)
        }
    }
}
