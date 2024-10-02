package com.odessy.srlaundry.entities
import androidx.room.PrimaryKey
import androidx.room.Entity

@Entity
data class Promo(
    @PrimaryKey val id: Int = 1,
    var serviceFrequency: Int,
    var isPromoActive: Boolean
)