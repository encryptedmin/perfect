package com.odessy.srlaundry.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.LaundrySales

class SalesAdapter : ListAdapter<LaundrySales, SalesAdapter.SalesViewHolder>(SalesDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_sales_record, parent, false)
        return SalesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        val salesRecord = getItem(position)
        holder.bind(salesRecord)
    }

    class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvDate: TextView = itemView.findViewById(R.id.tv_sales_date)
        private val tvType: TextView = itemView.findViewById(R.id.tv_sales_type)
        private val tvLoads: TextView = itemView.findViewById(R.id.tv_sales_loads)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tv_sales_price)

        fun bind(sales: LaundrySales) {
            tvDate.text = sales.transactionDate.toString() // Format date as needed
            tvType.text = sales.laundryType
            tvLoads.text = "Loads: ${sales.loads}"
            tvTotalPrice.text = "Total: $${sales.totalPrice}"
        }
    }

    class SalesDiffCallback : DiffUtil.ItemCallback<LaundrySales>() {
        override fun areItemsTheSame(oldItem: LaundrySales, newItem: LaundrySales): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: LaundrySales, newItem: LaundrySales): Boolean {
            return oldItem == newItem
        }
    }
}
