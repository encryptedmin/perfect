package com.odessy.srlaundry.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.odessy.srlaundry.databinding.ActivityStoreItemsBinding
import com.odessy.srlaundry.entities.StoreItem
import com.odessy.srlaundry.others.StoreItemAdapter
import com.odessy.srlaundry.others.StoreItemsViewModel

class StoreItems : AppCompatActivity() {

    private lateinit var binding: ActivityStoreItemsBinding  // View Binding instance
    private val storeItemViewModel: StoreItemsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityStoreItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RecyclerView and Adapter
        val adapter = StoreItemAdapter(emptyList()) { item ->
            storeItemViewModel.delete(item) // Handle deletion
        }
        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager = LinearLayoutManager(this)

        // Add new product to the database when the button is clicked
        binding.btnAddProduct.setOnClickListener {
            val name = binding.etProductName.text.toString()
            val quantity = binding.etProductQuantity.text.toString().toIntOrNull() ?: 0
            val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && quantity > 0 && price > 0) {
                val storeItem = StoreItem(productName = name, quantity = quantity, price = price)
                storeItemViewModel.addOrUpdateStoreItem(storeItem) // Change to addOrUpdateStoreItem
            }
        }
        binding.btnBack.setOnClickListener {
            finish() // Finish the current activity and return to the previous one
        }

        // Observe the LiveData from the ViewModel and update the RecyclerView
        storeItemViewModel.allStoreItems.observe(this, { items ->
            adapter.submitList(items)
        })

        // Search for items in the database when the search text is updated
        binding.etSearchProduct.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                storeItemViewModel.searchStoreItems(query).observe(this@StoreItems, { items ->
                    adapter.submitList(items)
                })
            }
        })
    }
}
