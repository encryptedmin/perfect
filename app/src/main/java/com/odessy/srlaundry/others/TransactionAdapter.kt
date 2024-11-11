package com.odessy.srlaundry.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.Transaction
import java.text.SimpleDateFormat
import java.util.*

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productName: TextView = view.findViewById(R.id.productName)
        val quantity: TextView = view.findViewById(R.id.quantity)
        val totalPrice: TextView = view.findViewById(R.id.totalPrice)
        val timestamp: TextView = view.findViewById(R.id.timestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.productName.text = transaction.productName
        holder.quantity.text = "Quantity: ${transaction.quantity}"
        holder.totalPrice.text = "Total: ₱${transaction.totalPrice}"

        // Format timestamp to a readable date
        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        holder.timestamp.text = "Timestamp: ${dateFormat.format(transaction.timestamp)}"
    }

    override fun getItemCount(): Int {
        return transactions.size
    }
}
