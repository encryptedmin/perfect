package com.odessy.srlaundry.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "SmsMessage")


data class SmsMessage(
    @PrimaryKey(autoGenerate = false) val id: Int = 1,
    val message: String,
    val timestamp: Date
) {

    constructor() : this(1, "", Date())
}