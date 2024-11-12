package com.odessy.srlaundry.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.Customer

class CustomerAdapter(
    private val customers: List<Customer>,
    private val onCustomerSelected: (Customer) -> Unit
) : RecyclerView.Adapter<CustomerAdapter.CustomerViewHolder>() {

    private var selectedPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomerViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_customer, parent, false)
        return CustomerViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomerViewHolder, position: Int) {
        val customer = customers[position]


        holder.nameTextView.text = "Name: ${customer.name}"
        holder.phoneTextView.text = "Phone Number: ${customer.phone}"
        holder.promoCountTextView.text = "Service Frequency Count: ${customer.promo}"


        if (position == selectedPosition) {
            holder.itemView.setBackgroundColor(Color.parseColor("#00FF7F"))
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT)
        }

        holder.itemView.setOnClickListener {
            selectedPosition = position
            onCustomerSelected(customer)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int = customers.size

    class CustomerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.customerNameTextView)
        val phoneTextView: TextView = itemView.findViewById(R.id.customerPhoneTextView)
        val promoCountTextView: TextView = itemView.findViewById(R.id.customerPromoCount)
    }
}