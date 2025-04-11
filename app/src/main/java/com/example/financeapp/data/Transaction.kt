package com.example.financeapp.data

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")

data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,  // Primární klíč
    val amount: Double,  // Částka
    val type: String,  // "income" nebo "expense"
    val date: Long,  // Timestamp (uložené datum)
    val category: String,  // Kategorie (např. Jídlo, Doprava...)
)
