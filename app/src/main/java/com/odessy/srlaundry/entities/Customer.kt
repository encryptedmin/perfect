package com.odessy.srlaundry.entities
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Customer")
data class Customer(@PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val phone: String,
    var promo: Int = 0
)
{
    override fun toString(): String {
        return name
    }
}