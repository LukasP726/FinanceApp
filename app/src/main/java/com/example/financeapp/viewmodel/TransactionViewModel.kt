package com.example.financeapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    val totalIncome: LiveData<Double> = repository.getTotalIncome()
    val totalExpense: LiveData<Double> = repository.getTotalExpense()
    val allTransactions: LiveData<List<Transaction>> = repository.getTransactions()


    fun insertTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.insertTransaction(transaction)
        }
    }
    /*
    fun deleteTransaction(transaction: Transaction, function: () -> Unit) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
        }
    }

     */

    fun getAllTransactions(callback: (List<Transaction>) -> Unit) {
        viewModelScope.launch {
            val transactions = repository.getAllTransactions()
            callback(transactions)
        }
    }

    fun getTransactionsByType(type: String, callback: (List<Transaction>) -> Unit) {
        viewModelScope.launch {
            val transactions = repository.getTransactionsByType(type)
            callback(transactions)
        }
    }


    fun deleteTransaction(transaction: Transaction, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteTransaction(transaction)
            callback()
        }
    }

    fun updateTransaction(transaction: Transaction, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateTransaction(transaction)
            callback()
        }
    }


    fun filterTransactions(category: String?, minAmount: Double?, maxAmount: Double?): LiveData<List<Transaction>> {
        return repository.getFilteredTransactions(category, minAmount, maxAmount)
    }






}
