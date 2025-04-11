package com.example.financeapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * ViewModel pro správu transakcí v aplikaci.
 * Tento ViewModel poskytuje data o transakcích, včetně celkového příjmu a výdaje,
 * a umožňuje provádět operace jako získání všech transakcí a jejich odstranění.
 */
class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    // LiveData pro celkový příjem a výdaj
    val totalIncome: LiveData<Double> = repository.getTotalIncome()
    val totalExpense: LiveData<Double> = repository.getTotalExpense()

    // LiveData pro všechny transakce
    val allTransactions: LiveData<List<Transaction>> = repository.getTransactions()

    /**
     * Získává všechny transakce a volá zpětný callback s výsledky.
     * Tento postup je prováděn asynchronně v rámci viewModelScope.
     */
    fun getAllTransactions(callback: (List<Transaction>) -> Unit) {
        viewModelScope.launch {
            val transactions = repository.getAllTransactions()
            callback(transactions)
        }
    }

    /**
     * Odstraňuje zadanou transakci z databáze.
     * Tento postup je prováděn asynchronně v rámci IO threadu.
     */
    fun deleteTransaction(transaction: Transaction, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
            callback()
        }
    }
}
