package com.odessy.srlaundry.entities
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity(tableName = "StoreItems")
data class StoreItems (@PrimaryKey(autoGenerate = true)val id: Int = 0,
    val productName: String,
    val price: Double,
    val quantity: Int
    )