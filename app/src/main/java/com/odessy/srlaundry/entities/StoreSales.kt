package com.odessy.srlaundry.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "StoreSales")
data class StoreSales(@PrimaryKey(autoGenerate = true)
    val transactionDate: Date,
    val storeCartId: Int = 0,
    val items: String,
    val totalSales: Double


    )