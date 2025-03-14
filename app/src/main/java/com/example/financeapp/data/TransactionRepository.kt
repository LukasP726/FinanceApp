package com.example.financeapp.data

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun getAllTransactions(): List<Transaction> {
        return transactionDao.getAllTransactions() // Předpoklad, že DAO vrací LiveData<List<Transaction>>
    }

    fun getTransactions(): LiveData<List<Transaction>> {
        return transactionDao.getTransactions() // Předpoklad, že DAO vrací LiveData<List<Transaction>>
    }

    suspend fun getTransactionsByType(transactionType: String): List<Transaction> {
        return transactionDao.getTransactionsByType(transactionType)
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    fun getTotalIncome(): LiveData<Double> = transactionDao.getTotalIncome()

    fun getTotalExpense(): LiveData<Double> = transactionDao.getTotalExpense()



}
