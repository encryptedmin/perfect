package com.odessy.srlaundry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.odessy.srlaundry.dao.JobOrderDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SalesViewModel(private val jobOrderDao: JobOrderDao) : ViewModel() {


    private suspend fun fetchGeneralSalesData(startDate: Long, endDate: Long): SalesData = withContext(Dispatchers.IO) {
        val totalIncome = jobOrderDao.getTotalIncome(startDate, endDate)
        val totalLoads = jobOrderDao.getTotalLoads(startDate, endDate)
        val totalDetergentAddons = jobOrderDao.getTotalDetergentAddons(startDate, endDate)
        val totalFabricConditionerAddons = jobOrderDao.getTotalFabricConditionerAddons(startDate, endDate)
        val totalBleachAddons = jobOrderDao.getTotalBleachAddons(startDate, endDate)

        return@withContext SalesData(
            totalIncome = totalIncome,
            totalLoads = totalLoads,
            totalDetergentAddons = totalDetergentAddons,
            totalFabricConditionerAddons = totalFabricConditionerAddons,
            totalBleachAddons = totalBleachAddons
        )
    }


    fun fetchSalesData(startDate: Long, endDate: Long, callback: (SalesData) -> Unit) {
        viewModelScope.launch {
            try {
                val salesData = fetchGeneralSalesData(startDate, endDate)
                callback(salesData)
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }


    fun fetchSalesDataByLaundryType(startDate: Long, endDate: Long, callback: (List<LaundrySalesData>) -> Unit) {
        viewModelScope.launch {
            try {
                val laundrySalesData = fetchLaundryTypeSalesData(startDate, endDate)
                callback(laundrySalesData)
            } catch (e: Exception) {

                e.printStackTrace()
            }
        }
    }


    private suspend fun fetchLaundryTypeSalesData(startDate: Long, endDate: Long): List<LaundrySalesData> = withContext(Dispatchers.IO) {
        val totalIncomeByType = jobOrderDao.getTotalIncomeByLaundryType(startDate, endDate)
        val totalLoadsByType = jobOrderDao.getTotalLoadsByLaundryType(startDate, endDate)
        val addonsByType = jobOrderDao.getAddonsByLaundryType(startDate, endDate)


        val loadsMap = totalLoadsByType.associateBy { it.laundryType }
        val addonsMap = addonsByType.associateBy { it.laundryType }

        return@withContext totalIncomeByType.map { income ->
            LaundrySalesData(
                laundryType = income.laundryType,
                totalIncome = income.totalIncome,
                totalLoads = loadsMap[income.laundryType]?.totalLoads ?: 0,
                totalDetergentAddons = addonsMap[income.laundryType]?.totalDetergent ?: 0,
                totalFabricConditionerAddons = addonsMap[income.laundryType]?.totalFabricConditioner ?: 0,
                totalBleachAddons = addonsMap[income.laundryType]?.totalBleach ?: 0
            )
        }
    }
}
