package com.odessy.srlaundry.others

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.StoreItem

class StoreItemAdapter(
    private var storeItems: List<StoreItem>,
    private val deleteListener: (StoreItem) -> Unit
) : RecyclerView.Adapter<StoreItemAdapter.StoreItemViewHolder>() {
    class StoreItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.tvProductName)
        val quantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val price: TextView = itemView.findViewById(R.id.tvPrice)
        val deleteButton: View = itemView.findViewById(R.id.btnDelete)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoreItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_store, parent, false)
        return StoreItemViewHolder(view)
    }
    override fun onBindViewHolder(holder: StoreItemViewHolder, position: Int) {
        val item = storeItems[position]
        holder.productName.text = item.productName
        holder.quantity.text = "Quantity: ${item.quantity}"
        holder.price.text = " Price: ₱${item.price}"
        holder.deleteButton.setOnClickListener {
            deleteListener(item)
        }
    }
    override fun getItemCount(): Int = storeItems.size
    fun submitList(newItems: List<StoreItem>) {
        storeItems = newItems
        notifyDataSetChanged()
    }
}
