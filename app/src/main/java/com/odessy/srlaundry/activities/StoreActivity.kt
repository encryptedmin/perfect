package com.odessy.srlaundry.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.StoreItem
import com.odessy.srlaundry.others.StoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StoreActivity : AppCompatActivity() {

    private lateinit var searchProductBar: EditText
    private lateinit var productListView: ListView
    private lateinit var cartListView: ListView
    private lateinit var inputQuantity: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonClear: Button
    private lateinit var buttonConfirm: Button
    private lateinit var textTotalPrice: TextView

    private val storeViewModel: StoreViewModel by viewModels()

    private var selectedStoreItem: StoreItem? = null
    private var cartItems = mutableListOf<StoreItem>()
    private var totalPrice = 0.0
    private val lowStockThreshold = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store)

        // Initialize UI elements
        searchProductBar = findViewById(R.id.searchProductBar)
        productListView = findViewById(R.id.productListView)
        cartListView = findViewById(R.id.cartListView)
        inputQuantity = findViewById(R.id.inputQuantity)
        buttonAdd = findViewById(R.id.buttonAdd)
        buttonClear = findViewById(R.id.buttonClear)
        buttonConfirm = findViewById(R.id.buttonConfirm)
        textTotalPrice = findViewById(R.id.textTotalPrice)

        // Sync Firestore store items with Room
        storeViewModel.fetchAndSyncStoreItemsFromFirestore()

        // Load products from Room (observing LiveData)
        storeViewModel.allStoreItems.observe(this, { products ->
            val adapter = ArrayAdapter(
                this@StoreActivity,
                android.R.layout.simple_list_item_1,
                products.map { it.productName }
            )
            productListView.adapter = adapter

            productListView.setOnItemClickListener { _, _, position, _ ->
                selectedStoreItem = products[position]
                inputQuantity.isEnabled = true
                buttonAdd.isEnabled = true
            }
        })

        // Set up search functionality
        searchProductBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                storeViewModel.searchStoreItems(s.toString()).observe(this@StoreActivity, { products ->
                    val adapter = ArrayAdapter(
                        this@StoreActivity,
                        android.R.layout.simple_list_item_1,
                        products.map { it.productName }
                    )
                    productListView.adapter = adapter
                })
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // Add product to cart with quantity validation
        buttonAdd.setOnClickListener {
            val quantity = inputQuantity.text.toString().toIntOrNull() ?: return@setOnClickListener
            addItemToCart(quantity)
        }

        // Confirm purchase and update stock
        buttonConfirm.setOnClickListener {
            confirmPurchase()
        }

        // Clear cart
        buttonClear.setOnClickListener {
            clearCart()
        }
    }

    private fun addItemToCart(quantity: Int) {
        selectedStoreItem?.let { item ->
            if (quantity > item.quantity) {
                Toast.makeText(this, "Not enough stock available", Toast.LENGTH_SHORT).show()
                return
            }

            val cartItem = item.copy(quantity = quantity)
            cartItems.add(cartItem)
            totalPrice += cartItem.price * cartItem.quantity
            updateCartView()
            updateTotalPrice()
            inputQuantity.text.clear()
            Toast.makeText(this, "Added to Cart", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateCartView() {
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_list_item_1,
            cartItems.map { "${it.productName} - Qty: ${it.quantity}" }
        )
        cartListView.adapter = adapter
    }

    private fun updateTotalPrice() {
        textTotalPrice.text = "Total: $$totalPrice"
    }

    private fun clearCart() {
        cartItems.clear()
        totalPrice = 0.0
        updateCartView()
        updateTotalPrice()
    }

    private fun confirmPurchase() {
        lifecycleScope.launch(Dispatchers.IO) {
            for (item in cartItems) {
                val purchasedQuantity = item.quantity

                // Fetch the actual stock from Room using a suspend function
                val storeItem = storeViewModel.getStoreItemByName(item.productName)

                storeItem?.let {
                    // Subtract the purchased quantity from the actual stock
                    val updatedQuantity = storeItem.quantity - purchasedQuantity

                    // Update quantity in Room and Firestore
                    storeViewModel.updateQuantity(storeItem.productName, updatedQuantity)
                    storeViewModel.addTransaction(item, purchasedQuantity)  // Insert transaction
                }
            }

            // Check if any items are low in stock after the purchase
            checkLowStock()

            // Clear cart after confirming purchase
            launch(Dispatchers.Main) {
                clearCart()
                Toast.makeText(this@StoreActivity, "Purchase Confirmed", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun checkLowStock() {
        lifecycleScope.launch {
            val lowStockItems = storeViewModel.checkLowStock(lowStockThreshold)
            if (lowStockItems.isNotEmpty()) {
                val lowStockMessage = lowStockItems.joinToString(", ") { it.productName }
                Toast.makeText(this@StoreActivity, "Low stock alert: $lowStockMessage", Toast.LENGTH_LONG).show()
            }
        }
    }
}
