package com.example.financeapp.ui

import android.app.DatePickerDialog
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.R
import com.example.financeapp.data.TransactionDatabase
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionRepository
import com.example.financeapp.databinding.ActivityAddTransactionBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.io.OutputStream

/**
 * Aktivita sloužící k přidání nové transakce nebo úpravě existující.
 * Umožňuje zadat částku, typ (příjem/výdaj), kategorii a datum.
 */
class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var repository: TransactionRepository
    private var selectedDate: Long = System.currentTimeMillis()
    private var transactionId: Int = -1

    /**
     * Inicializace aktivity a UI prvků.
     * Při editaci načte existující data.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val dao = TransactionDatabase.getDatabase(this).transactionDao()
        repository = TransactionRepository(dao)

        setupCategorySpinner()
        setupDatePicker()

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        transactionId = intent.getIntExtra("transaction_id", -1)
        if (transactionId != -1) {
            loadTransactionData()
        }
    }

    /**
     * Načte data transakce z intentu pro účely úpravy.
     */
    private fun loadTransactionData() {
        val amount = intent.getDoubleExtra("amount", 0.0)
        val type = intent.getStringExtra("type") ?: "expense"
        val date = intent.getLongExtra("date", System.currentTimeMillis())
        val category = intent.getStringExtra("category") ?: ""

        binding.etTransactionAmount.setText(amount.toString())
        binding.etTransactionDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date))
        selectedDate = date

        val typeIndex = if (type == "income") 0 else 1
        binding.spinnerType.setSelection(typeIndex)

        val categoryAdapter = binding.spinnerCategory.adapter as ArrayAdapter<String>
        val categoryIndex = categoryAdapter.getPosition(category)
        binding.spinnerCategory.setSelection(categoryIndex)
    }

    /**
     * Nastaví hodnoty pro spinner kategorií.
     */
    private fun setupCategorySpinner() {
        val categories = listOf("Jídlo", "Doprava", "Zábava", "Oblečení", "Jiné")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    /**
     * Nastaví zobrazení a výběr data pomocí DatePickerDialogu.
     */
    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        binding.etTransactionDate.setText(dateFormat.format(calendar.time))

        binding.etTransactionDate.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDate = calendar.timeInMillis
                    binding.etTransactionDate.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    /**
     * Uloží transakci do databáze – novou nebo upravenou.
     */
    private fun saveTransaction() {
        val amountText = binding.etTransactionAmount.text.toString()
        if (amountText.isEmpty()) return
        val category = binding.spinnerCategory.selectedItem.toString()

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Zadejte platnou částku", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedType = binding.spinnerType.selectedItem.toString()
        val type = if (selectedType == "Příjem") "income" else "expense"

        val transaction = Transaction(transactionId, amount, type, selectedDate, category)

        CoroutineScope(Dispatchers.IO).launch {
            if (transactionId == -1) {
                repository.insertTransaction(transaction)
            } else {
                repository.updateTransaction(transaction)
            }

            withContext(Dispatchers.Main) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
