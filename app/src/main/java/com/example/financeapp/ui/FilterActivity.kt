package com.example.financeapp.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.DatePicker
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.R
import com.example.financeapp.databinding.ActivityFilterBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class FilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterBinding
    private var selectedDateFrom: Long = System.currentTimeMillis() // Výchozí datum je dnes
    private var selectedDateTo: Long = System.currentTimeMillis() // Výchozí datum je dnes


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Naplnění spinneru kategoriemi
        val categories = arrayOf("Vše", "Jídlo", "Doprava", "Zábava", "Ostatní")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
        setupDatePicker()

        binding.btnApplyFilter.setOnClickListener {
            applyFilter()
        }
    }

    private fun applyFilter() {
        val minAmount = binding.etMinAmount.text.toString().toDoubleOrNull() ?: 0.0
        val maxAmount = binding.etMaxAmount.text.toString().toDoubleOrNull() ?: Double.MAX_VALUE
        val category = binding.spinnerCategory.selectedItem.toString()



        val resultIntent = Intent()
        resultIntent.putExtra("minAmount", minAmount)
        resultIntent.putExtra("maxAmount", maxAmount)
        resultIntent.putExtra("category", category)
        resultIntent.putExtra("dateFrom", selectedDateFrom)
        resultIntent.putExtra("dateTo", selectedDateTo)

        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }


    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        binding.datePickerFrom.setText(dateFormat.format(calendar.time))
        binding.datePickerTo.setText(dateFormat.format(calendar.time))

        binding.datePickerFrom.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateFrom = calendar.timeInMillis
                    binding.datePickerFrom.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }



        binding.datePickerTo.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    selectedDateTo = calendar.timeInMillis
                    binding.datePickerTo.setText(dateFormat.format(calendar.time))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }
}
