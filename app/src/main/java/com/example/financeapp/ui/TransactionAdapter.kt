package com.example.financeapp.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financeapp.R
import com.example.financeapp.data.Transaction

/**
 * Adapter pro zobrazení seznamu transakcí v RecyclerView.
 * Tento adaptér zajišťuje zobrazení jednotlivých transakcí, jejich kategorií a částek.
 */
class TransactionAdapter(
    private var transactions: List<Transaction>,
    private val onItemLongClick: (Transaction, View) -> Unit // Přidáme callback pro dlouhý klik
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    /**
     * ViewHolder pro jednotlivé položky seznamu transakcí.
     * Obsahuje reference na TextView pro zobrazení názvu transakce a její částky.
     */
    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTransactionName: TextView = view.findViewById(R.id.tvTransactionName)
        val tvTransactionAmount: TextView = view.findViewById(R.id.tvTransactionAmount)
    }

    /**
     * Vytváří nový ViewHolder pro položku transakce.
     * Inflatuje layout pro jednotlivou transakci.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    /**
     * Binduje data (název kategorie a částku) k ViewHolderu pro danou pozici.
     * Nastavuje také kliknutí na položku pro volání callbacku při dlouhém kliknutí.
     */
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

    /**
     * Vrací počet položek v seznamu transakcí.
     */
    override fun getItemCount(): Int = transactions.size

    /**
     * Aktualizuje data v adapteru a znovu vykreslí seznam.
     */
    fun updateData(newTransactions: List<Transaction>) {
        transactions = newTransactions
        notifyDataSetChanged()
    }
}
