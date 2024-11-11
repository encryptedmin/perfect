package com.odessy.srlaundry.others

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

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textViewProductName: TextView = itemView.findViewById(R.id.productName)
        private val textViewQuantity: TextView = itemView.findViewById(R.id.quantity)
        private val textViewTotalPrice: TextView = itemView.findViewById(R.id.totalPrice)
        private val textViewTimestamp: TextView = itemView.findViewById(R.id.timestamp)

        fun bind(transaction: Transaction) {
            textViewProductName.text = transaction.productName
            textViewQuantity.text = "Quantity: ${transaction.quantity}"
            textViewTotalPrice.text = "Total: ₱%.2f".format(transaction.totalPrice)
            textViewTimestamp.text = "Timestamp: ${dateFormatter.format(transaction.timestamp)}"
        }
    }
}
