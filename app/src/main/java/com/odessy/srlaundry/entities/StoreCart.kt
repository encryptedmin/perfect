package com.odessy.srlaundry.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "StoreSales")
data class StoreCart(@PrimaryKey(autoGenerate = true)
    val cartId: Int = 0,
    val transactionDate: Date,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double

    )

