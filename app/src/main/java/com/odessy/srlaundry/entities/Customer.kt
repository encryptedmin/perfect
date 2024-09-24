package com.odessy.srlaundry.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Customer")
data class Customer(@PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    val promo: Int = 0
)