package com.odessy.srlaundry.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laundryPrice")
data class LaundryPrice(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val regular: Double,
    val bedSheet: Double,
    val addOnDetergent: Double,
    val addOnFabricConditioner: Double,
    val addOnBleach: Double
)
