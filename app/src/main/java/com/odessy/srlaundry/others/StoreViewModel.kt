package com.odessy.srlaundry.others

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.odessy.srlaundry.entities.StoreItem

class StoreViewModel : ViewModel() {

    private val _selectedItem = MutableLiveData<StoreItem?>()
    val selectedItem: LiveData<StoreItem?> = _selectedItem

    private val _cartItems = MutableLiveData<MutableList<StoreItem>>()
    val cartItems: LiveData<MutableList<StoreItem>> = _cartItems

    private var totalPrice = 0.0

    init {
        _cartItems.value = mutableListOf()
    }

    fun selectItem(item: StoreItem) {
        _selectedItem.value = item
    }

    fun addItemToCart(quantity: Int) {
        val item = _selectedItem.value ?: return
        val cartItem = item.copy(quantity = quantity)
        _cartItems.value?.add(cartItem)
        totalPrice += cartItem.price * cartItem.quantity
    }

    fun clearCart() {
        _cartItems.value?.clear()
        totalPrice = 0.0
    }

    fun getTotalPrice(): String {
        return "Total: $$totalPrice"
    }
}
