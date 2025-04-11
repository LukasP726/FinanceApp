package com.example.financeapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow

/**
 * Repository sloužící jako mezivrstva mezi databází a ViewModely.
 * Obsahuje metody pro manipulaci s transakcemi a získání statistických údajů.
 */
class TransactionRepository(private val transactionDao: TransactionDao) {

    /**
     * Vloží novou transakci do databáze.
     */
    suspend fun insertTransaction(transaction: Transaction) {
        transactionDao.insertTransaction(transaction)
    }

    /**
     * Odstraní danou transakci z databáze.
     */
    suspend fun deleteTransaction(transaction: Transaction) {
        transactionDao.deleteTransaction(transaction)
    }

    /**
     * Vrací seznam všech transakcí jako List (asynchronní).
     */
    suspend fun getAllTransactions(): List<Transaction> {
        return transactionDao.getAllTransactions()
    }

    /**
     * Vrací seznam všech transakcí jako LiveData (pro pozorování z UI).
     */
    fun getTransactions(): LiveData<List<Transaction>> {
        return transactionDao.getTransactions()
    }

    /**
     * Vrací seznam transakcí daného typu (např. "Příjem", "Výdaj").
     */
    suspend fun getTransactionsByType(transactionType: String): List<Transaction> {
        return transactionDao.getTransactionsByType(transactionType)
    }

    /**
     * Aktualizuje existující transakci v databázi.
     */
    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    /**
     * Vrací celkovou hodnotu příjmů jako LiveData.
     */
    fun getTotalIncome(): LiveData<Double> = transactionDao.getTotalIncome()

    /**
     * Vrací celkovou hodnotu výdajů jako LiveData.
     */
    fun getTotalExpense(): LiveData<Double> = transactionDao.getTotalExpense()
}
