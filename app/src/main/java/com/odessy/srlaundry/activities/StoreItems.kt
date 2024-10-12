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


        binding = ActivityStoreItemsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val adapter = StoreItemAdapter(emptyList()) { item ->
            storeItemViewModel.delete(item) // Handle deletion
        }
        binding.rvProducts.adapter = adapter
        binding.rvProducts.layoutManager = LinearLayoutManager(this)


        storeItemViewModel.syncStoreItems()

        binding.btnAddProduct.setOnClickListener {
            val name = binding.etProductName.text.toString()
            val quantity = binding.etProductQuantity.text.toString().toIntOrNull() ?: 0
            val price = binding.etProductPrice.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && quantity > 0 && price > 0) {
                val storeItem = StoreItem(productName = name, quantity = quantity, price = price)
                storeItemViewModel.addOrUpdateStoreItem(storeItem)
            }
        }


        binding.btnBack.setOnClickListener {
            finish()
        }


        storeItemViewModel.allStoreItems.observe(this, { items ->
            adapter.submitList(items)
        })

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
