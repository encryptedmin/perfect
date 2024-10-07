package com.odessy.srlaundry.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val quantity: Int,
    val totalPrice: Double,
    val timestamp: Date
)
