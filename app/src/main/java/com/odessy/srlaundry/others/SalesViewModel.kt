package com.odessy.srlaundry.others

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odessy.srlaundry.dao.JobOrderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SalesViewModel(private val jobOrderDao: JobOrderDao) : ViewModel() {


    fun fetchSalesDataForLaundryTypes(startDate: Long, endDate: Long, callback: (LaundrySalesResult) -> Unit) {
        viewModelScope.launch {
            try {
                val regularSalesData = fetchSalesDataByLaundryType("Regular", startDate, endDate)
                val bedsheetsSalesData = fetchSalesDataByLaundryType("Bedsheets", startDate, endDate)

                val result = LaundrySalesResult(
                    regularSales = regularSalesData,
                    bedsheetsSales = bedsheetsSalesData
                )
                callback(result)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private suspend fun fetchSalesDataByLaundryType(laundryType: String, startDate: Long, endDate: Long): LaundrySalesData =
        withContext(Dispatchers.IO) {
            val totalIncome = jobOrderDao.getTotalIncomeByLaundryType(startDate, endDate).find { it.laundryType == laundryType }?.totalIncome ?: 0.0
            val totalLoads = jobOrderDao.getTotalLoadsByLaundryType(startDate, endDate).find { it.laundryType == laundryType }?.totalLoads ?: 0
            val addons = jobOrderDao.getAddonsByLaundryType(startDate, endDate).find { it.laundryType == laundryType }

            return@withContext LaundrySalesData(
                laundryType = laundryType,
                totalIncome = totalIncome,
                totalLoads = totalLoads,
                totalDetergentAddons = addons?.totalDetergent ?: 0,
                totalFabricConditionerAddons = addons?.totalFabricConditioner ?: 0,
                totalBleachAddons = addons?.totalBleach ?: 0
            )
        }
}





