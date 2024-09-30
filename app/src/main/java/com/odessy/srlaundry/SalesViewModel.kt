package com.odessy.srlaundry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odessy.srlaundry.dao.JobOrderDao
import kotlinx.coroutines.launch

class SalesViewModel(private val jobOrderDao: JobOrderDao) : ViewModel() {

    // Function to fetch general sales data
    fun fetchSalesData(startDate: Long, endDate: Long, callback: (SalesData) -> Unit) {
        viewModelScope.launch {
            val totalIncome = jobOrderDao.getTotalIncome(startDate, endDate)
            val totalLoads = jobOrderDao.getTotalLoads(startDate, endDate)
            val totalDetergentAddons = jobOrderDao.getTotalDetergentAddons(startDate, endDate)
            val totalFabricConditionerAddons = jobOrderDao.getTotalFabricConditionerAddons(startDate, endDate)
            val totalBleachAddons = jobOrderDao.getTotalBleachAddons(startDate, endDate)

            val salesData = SalesData(
                totalIncome = totalIncome,
                totalLoads = totalLoads,
                totalDetergentAddons = totalDetergentAddons,
                totalFabricConditionerAddons = totalFabricConditionerAddons,
                totalBleachAddons = totalBleachAddons
            )

            callback(salesData)
        }
    }

    // Function to fetch sales data by laundry type
    fun fetchSalesDataByLaundryType(startDate: Long, endDate: Long, callback: (List<LaundrySalesData>) -> Unit) {
        viewModelScope.launch {
            val totalIncomeByType = jobOrderDao.getTotalIncomeByLaundryType(startDate, endDate)
            val totalLoadsByType = jobOrderDao.getTotalLoadsByLaundryType(startDate, endDate)
            val addonsByType = jobOrderDao.getAddonsByLaundryType(startDate, endDate)

            // Combine the results into a list of LaundrySalesData
            val laundrySalesData = totalIncomeByType.map { income ->
                LaundrySalesData(
                    laundryType = income.laundryType,
                    totalIncome = income.totalIncome,
                    totalLoads = totalLoadsByType.find { it.laundryType == income.laundryType }?.totalLoads ?: 0,
                    totalDetergentAddons = addonsByType.find { it.laundryType == income.laundryType }?.totalDetergent ?: 0,
                    totalFabricConditionerAddons = addonsByType.find { it.laundryType == income.laundryType }?.totalFabricConditioner ?: 0,
                    totalBleachAddons = addonsByType.find { it.laundryType == income.laundryType }?.totalBleach ?: 0
                )
            }

            callback(laundrySalesData)
        }
    }
}

// Data class to hold sales data by laundry type
data class LaundrySalesData(
    val laundryType: String,
    val totalIncome: Double,
    val totalLoads: Int,
    val totalDetergentAddons: Int,
    val totalFabricConditionerAddons: Int,
    val totalBleachAddons: Int
)
