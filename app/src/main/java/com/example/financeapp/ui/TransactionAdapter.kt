package com.example.financeapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.Transaction

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemLongClick: (Transaction, View) -> Unit // Přidáme callback
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTransactionName: TextView = view.findViewById(R.id.tvTransactionName)
        val tvTransactionAmount: TextView = view.findViewById(R.id.tvTransactionAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.tvTransactionName.text = transaction.category
        holder.tvTransactionAmount.text = "${transaction.amount} Kč"

        // Přidáme dlouhý klik
        holder.itemView.setOnLongClickListener {
            onItemLongClick(transaction, it)
            true
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
