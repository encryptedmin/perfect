package com.odessy.srlaundry.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "SmsMessage")


data class SmsMessage(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val message: String,
    val timestamp: Date
) {
    // Firestore requires a no-arg constructor
    constructor() : this(0, "", Date())
}