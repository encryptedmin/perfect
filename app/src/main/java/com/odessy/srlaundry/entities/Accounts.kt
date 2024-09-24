package com.odessy.srlaundry.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Accounts")
data class Accounts(@PrimaryKey(autoGenerate = true) val id: Int = 0,
    val username: String,
    val role: String,
    val password: String
)
