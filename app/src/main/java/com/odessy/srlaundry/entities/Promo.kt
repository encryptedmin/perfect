package com.odessy.srlaundry.entities
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity (tableName = "Promo")
data class Promo (@PrimaryKey(autoGenerate = true)val id: Int = 0,
    val serviceFrequency: Int
    )