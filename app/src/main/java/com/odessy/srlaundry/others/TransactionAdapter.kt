package com.odessy.srlaundry.others

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.odessy.srlaundry.R
import com.odessy.srlaundry.entities.Transaction

class TransactionAdapter : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    private var transactions: List<Transaction> = emptyList()

    class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val productName: TextView = itemView.findViewById(R.id.productName)
        val quantity: TextView = itemView.findViewById(R.id.quantity)
        val totalPrice: TextView = itemView.findViewById(R.id.totalPrice)
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
    }
    override fun getItemCount(): Int {
        return transactions.size
    }
    fun submitList(transactionList: List<Transaction>) {
        transactions = transactionList
        notifyDataSetChanged()
    }
}
