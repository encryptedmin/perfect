package com.odessy.srlaundry.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.odessy.srlaundry.R
import com.odessy.srlaundry.database.AppDatabase
import com.odessy.srlaundry.entities.StoreItem
import com.odessy.srlaundry.entities.Transaction
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class StoreActivity : AppCompatActivity() {

    private lateinit var searchProductBar: EditText
    private lateinit var productListView: ListView
    private lateinit var cartListView: ListView
    private lateinit var inputQuantity: EditText
    private lateinit var buttonAdd: Button
    private lateinit var buttonClear: Button
    private lateinit var buttonConfirm: Button
    private lateinit var textTotalPrice: TextView

    private lateinit var db: AppDatabase
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

        // Initialize database
        db = AppDatabase.getDatabase(applicationContext, lifecycleScope)

        // Load products from the database
        loadProducts()

        // Set up search functionality
        searchProductBar.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                searchProducts(s.toString())
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

    private fun loadProducts() {
        db.storeItemDao().getAllStoreItems().observe(this, Observer { products ->
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
    }

    private fun searchProducts(query: String) {
        db.storeItemDao().searchStoreItems("%$query%").observe(this, Observer { products ->
            val adapter = ArrayAdapter(
                this@StoreActivity,
                android.R.layout.simple_list_item_1,
                products.map { it.productName }
            )
            productListView.adapter = adapter
        })
    }

    private fun addItemToCart(quantity: Int) {
        selectedStoreItem?.let { item ->
            if (quantity > item.quantity) {
                // If requested quantity exceeds available stock, show an error
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
            val transactionDao = db.transactionDao()
            val storeItemDao = db.storeItemDao()

            for (item in cartItems) {
                // Deduct quantity from StoreItem after purchase
                val newQuantity = item.quantity - item.quantity
                storeItemDao.updateQuantity(item.id, newQuantity)

                // Insert the transaction record
                val transaction = Transaction(
                    id = item.id,
                    productName = item.productName,
                    quantity = item.quantity,
                    totalPrice = item.price * item.quantity,
                    timestamp = Date()
                )
                transactionDao.insertTransaction(transaction)
            }

            // Check if any items are low in stock after the purchase
            checkLowStock()

            // Clear cart after confirming purchase
            withContext(Dispatchers.Main) {
                clearCart()
                Toast.makeText(this@StoreActivity, "Purchase Confirmed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun checkLowStock() {
        val lowStockItems = db.storeItemDao().getItemsBelowThreshold(lowStockThreshold)

        withContext(Dispatchers.Main) {
            if (lowStockItems.isNotEmpty()) {
                val lowStockMessage = lowStockItems.joinToString(", ") { it.productName }
                Toast.makeText(this@StoreActivity, "Low stock alert: $lowStockMessage", Toast.LENGTH_LONG).show()
            }
        }
    }
}
