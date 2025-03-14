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

class AddTransactionActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddTransactionBinding
    private lateinit var repository: TransactionRepository
    private var selectedDate: Long = System.currentTimeMillis() // Výchozí datum je dnes
    private var transactionId: Int = -1

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

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Získání hodnot z Intentu, pokud nějaké existují (tj. editace)
        transactionId = intent.getIntExtra("transaction_id", -1)
        if (transactionId != -1) { // Pokud je předán platný ID transakce, načíst ji
            loadTransactionData()
        }
    }

    private fun loadTransactionData() {
        val amount = intent.getDoubleExtra("amount", 0.0)
        val type = intent.getStringExtra("type") ?: "expense"
        val date = intent.getLongExtra("date", System.currentTimeMillis())
        val category = intent.getStringExtra("category") ?: ""

        binding.etTransactionAmount.setText(amount.toString())
        binding.etTransactionDate.setText(SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date))
        selectedDate = date

        // Nastavení spinneru pro typ transakce
        val typeIndex = if (type == "income") 0 else 1
        binding.spinnerType.setSelection(typeIndex)

        // Nastavení spinneru pro kategorii
        val categoryAdapter = binding.spinnerCategory.adapter as ArrayAdapter<String>
        val categoryIndex = categoryAdapter.getPosition(category)
        binding.spinnerCategory.setSelection(categoryIndex)
    }





    private fun setupCategorySpinner() {
        val categories = listOf("Jídlo", "Doprava", "Zábava", "Oblečení", "Jiné")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

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
                setResult(RESULT_OK) // Nastavíme výsledek aktivity
                finish() // Ukončíme aktivitu
            }
        }
    }











}
