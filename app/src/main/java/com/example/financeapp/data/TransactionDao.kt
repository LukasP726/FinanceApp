package com.example.financeapp.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction)  // Přidání transakce

    @Delete
    suspend fun deleteTransaction(transaction: Transaction)  // Smazání transakce

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    suspend fun getAllTransactions(): List<Transaction>  // Načtení všech transakcí


    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getTransactions(): LiveData<List<Transaction>>  // Načtení všech transakcí

    @Query("SELECT * FROM transactions WHERE type = :transactionType ORDER BY date DESC")
    suspend fun getTransactionsByType(transactionType: String): List<Transaction>  // Příjmy/Výdaje

    @Update
    suspend fun update(transaction: Transaction)


    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'income'")
    fun getTotalIncome(): LiveData<Double>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'expense'")
    fun getTotalExpense(): LiveData<Double>

    @Query(
        """SELECT * FROM transactions WHERE 
        (:category IS NULL OR category = :category) AND
        (:minAmount IS NULL OR amount >= :minAmount) AND
        (:maxAmount IS NULL OR amount <= :maxAmount)"""
    )
    fun getFilteredTransactions(
        category: String?,
        minAmount: Double?,
        maxAmount: Double?
    ): LiveData<List<Transaction>>


}
