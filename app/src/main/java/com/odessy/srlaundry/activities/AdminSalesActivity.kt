package com.odessy.srlaundry.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.adapters.SalesAdapter
import com.odessy.srlaundry.entities.LaundrySales
import com.odessy.srlaundry.viewmodel.AdminSalesViewModel

class AdminSalesActivity : AppCompatActivity() {

    private lateinit var btnDailySales: Button
    private lateinit var btnWeeklySales: Button
    private lateinit var btnMonthlySales: Button
    private lateinit var tvSalesTotal: TextView
    private lateinit var rvSalesRecords: RecyclerView
    private val salesAdapter = SalesAdapter()

    private val adminSalesViewModel: AdminSalesViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_sales)
        initializeViews()
        rvSalesRecords.layoutManager = LinearLayoutManager(this)
        rvSalesRecords.adapter = salesAdapter
        adminSalesViewModel.salesRecords.observe(this, Observer { salesList ->
            salesAdapter.submitList(salesList)
            updateSalesTotal(salesList)
        })
        adminSalesViewModel.fetchSalesData(AdminSalesViewModel.FilterType.DAILY)
        btnDailySales.setOnClickListener {
            adminSalesViewModel.fetchSalesData(AdminSalesViewModel.FilterType.DAILY)
        }
        btnWeeklySales.setOnClickListener {
            adminSalesViewModel.fetchSalesData(AdminSalesViewModel.FilterType.WEEKLY)
        }
        btnMonthlySales.setOnClickListener {
            adminSalesViewModel.fetchSalesData(AdminSalesViewModel.FilterType.MONTHLY)
        }
    }
    private fun initializeViews() {
        btnDailySales = findViewById(R.id.btn_daily_sales)
        btnWeeklySales = findViewById(R.id.btn_weekly_sales)
        btnMonthlySales = findViewById(R.id.btn_monthly_sales)
        tvSalesTotal = findViewById(R.id.tv_sales_total)
        rvSalesRecords = findViewById(R.id.rv_sales_records)
    }
    private fun updateSalesTotal(salesList: List<LaundrySales>) {
        val totalSales = salesList.sumOf { it.totalPrice }
        tvSalesTotal.text = "Total Sales: $%.2f".format(totalSales)
    }
}
