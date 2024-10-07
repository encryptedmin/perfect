package com.odessy.srlaundry.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "laundry_sales")
data class LaundrySales(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val transactionDate: Date = Date(),
    val laundryType: String = "",
    val weight: Double = 0.0,
    val loads: Int = 0,
    val addOnDetergent: Int = 0,
    val addOnFabricConditioner: Int = 0,
    val addOnBleach: Int = 0,
    val totalPrice: Double = 0.0
) {

    constructor() : this(0, Date(), "", 0.0, 0, 0, 0, 0, 0.0)
}
