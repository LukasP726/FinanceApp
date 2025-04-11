package com.example.financeapp.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room databáze pro ukládání transakcí.
 * Obsahuje jednu entitu: Transaction.
 * Poskytuje přístup k DAO a zajišťuje singleton instanci databáze.
 */
@Database(entities = [Transaction::class], version = 1, exportSchema = false)
abstract class TransactionDatabase : RoomDatabase() {

    /**
     * Vrací instanci DAO pro přístup k operacím nad transakcemi.
     */
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: TransactionDatabase? = null

        /**
         * Vrací singleton instanci databáze.
         * Pokud ještě neexistuje, vytvoří ji.
         */
        fun getDatabase(context: Context): TransactionDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TransactionDatabase::class.java,
                    "transaction_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
