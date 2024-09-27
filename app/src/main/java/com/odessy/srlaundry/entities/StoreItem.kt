package com.odessy.srlaundry.entities
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "store_items")
data class StoreItem(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val productName: String,
    val quantity: Int,
    val price: Double
)