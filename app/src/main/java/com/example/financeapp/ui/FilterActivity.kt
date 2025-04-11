package com.example.financeapp.ui

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.R
import com.example.financeapp.databinding.ActivityFilterBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Aktivita pro filtrování výdajů podle částky, kategorie a časového období.
 * Vrací zadané filtrační parametry jako výsledek do předchozí aktivity.
 */
class FilterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFilterBinding
    private var selectedDateFrom: Long = System.currentTimeMillis()
    private var selectedDateTo: Long = System.currentTimeMillis()

    /**
     * Inicializuje layout, spinner s kategoriemi a obsluhu tlačítek.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val categories = arrayOf("Vše", "Jídlo", "Doprava", "Zábava", "Ostatní")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        setupDatePicker()

        binding.btnApplyFilter.setOnClickListener {
            applyFilter()
        }
    }

    /**
     * Získá hodnoty z formuláře a odešle je zpět do předchozí aktivity jako výsledek.
     */
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

    /**
     * Nastaví výběr počátečního a koncového data pomocí DatePickerDialog.
     */
    private fun setupDatePicker() {
        val calendarFrom = Calendar.getInstance()
        calendarFrom.add(Calendar.DAY_OF_MONTH, -1)
        selectedDateFrom = calendarFrom.timeInMillis

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        binding.datePickerFrom.setText(dateFormat.format(calendarFrom.time))

        binding.datePickerFrom.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    selectedDateFrom = selectedCalendar.timeInMillis
                    binding.datePickerFrom.setText(dateFormat.format(selectedCalendar.time))
                },
                calendarFrom.get(Calendar.YEAR),
                calendarFrom.get(Calendar.MONTH),
                calendarFrom.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }

        val calendarTo = Calendar.getInstance()
        selectedDateTo = calendarTo.timeInMillis
        binding.datePickerTo.setText(dateFormat.format(calendarTo.time))

        binding.datePickerTo.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    val selectedCalendar = Calendar.getInstance()
                    selectedCalendar.set(year, month, dayOfMonth)
                    selectedDateTo = selectedCalendar.timeInMillis
                    binding.datePickerTo.setText(dateFormat.format(selectedCalendar.time))
                },
                calendarTo.get(Calendar.YEAR),
                calendarTo.get(Calendar.MONTH),
                calendarTo.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }
}
