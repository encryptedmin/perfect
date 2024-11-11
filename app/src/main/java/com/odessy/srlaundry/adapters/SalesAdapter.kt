package com.odessy.srlaundry.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.LaundrySales
import java.text.SimpleDateFormat
import java.util.*

class SalesAdapter : RecyclerView.Adapter<SalesAdapter.SalesViewHolder>() {

    private var salesList: List<LaundrySales> = listOf()

    fun submitList(sales: List<LaundrySales>) {
        salesList = sales
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sales, parent, false)
        return SalesViewHolder(view)
    }

    override fun onBindViewHolder(holder: SalesViewHolder, position: Int) {
        holder.bind(salesList[position])
    }

    override fun getItemCount(): Int = salesList.size

    inner class SalesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvLaundryType: TextView = itemView.findViewById(R.id.tvLaundryType)
        private val tvAddOns: TextView = itemView.findViewById(R.id.tvAddOns)
        private val tvWeight: TextView = itemView.findViewById(R.id.tvWeight)
        private val tvLoads: TextView = itemView.findViewById(R.id.tvLoads)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val tvTransactionDate: TextView = itemView.findViewById(R.id.tvTransactionDate)

        fun bind(sale: LaundrySales) {
            tvLaundryType.text = sale.laundryType
            tvAddOns.text = "Bleach: ${sale.addOnBleach}, Detergent: ${sale.addOnDetergent}, Fabric Conditioner: ${sale.addOnFabricConditioner}"
            tvWeight.text = "Weight: ${sale.weight} kg"
            tvLoads.text = "Loads: ${sale.loads}"
            tvTotalPrice.text = "Total Price: ₱${String.format("%.2f", sale.totalPrice)}"

            // Convert transactionDate from Date to formatted string
            val date = sale.transactionDate // Ensure this is of type Date
            val formattedDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(date)
            tvTransactionDate.text = formattedDate
        }
    }
}
