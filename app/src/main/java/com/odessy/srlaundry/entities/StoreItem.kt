package com.odessy.srlaundry.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_items")
data class StoreItem(
    @PrimaryKey val productName: String,
    val quantity: Int,
    val price: Double
)
{
constructor():this("",0,0.0)
}