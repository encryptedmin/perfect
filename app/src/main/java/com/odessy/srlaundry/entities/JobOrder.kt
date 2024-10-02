    package com.odessy.srlaundry.entities

    import androidx.room.Entity
    import androidx.room.PrimaryKey


    @Entity(tableName = "JobOrder")
    data class JobOrder(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        val customerName: String,
        val customerPhone: String,
        val weight: Double,
        val loads: Int,
        val addOnDetergent: Int,
        val addOnFabricConditioner: Int,
        val addOnBleach: Int,
        val totalPrice: Double,
        val laundryType: String,
        var isActive: Boolean = true,
        val createdDate: Long = System.currentTimeMillis()
    )

