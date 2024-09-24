package com.odessy.srlaundry.entities
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity (tableName = "laundrySales")

data class LaundrySales (@PrimaryKey(autoGenerate = true)val id: Int = 0,
    val transactionDate: Date,
    val laundryType: String,
    val weight: Double,
    val loads: Int,
    val addOnDetergent: Int,
    val addOnFabricConditioner: Int,
    val addOnBleach: Int
    )