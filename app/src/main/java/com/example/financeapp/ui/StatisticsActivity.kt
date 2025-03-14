package com.example.financeapp.ui

import android.animation.ObjectAnimator
import android.content.ContentValues
import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

import com.example.financeapp.R
import com.example.financeapp.data.TransactionDatabase
import com.example.financeapp.data.TransactionRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class StatisticsActivity : AppCompatActivity() {
    private lateinit var repository: TransactionRepository
    private lateinit var pieChart: PieChart

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)

        pieChart = findViewById(R.id.pieChart)

        val entries = listOf(
            PieEntry(500f, "Příjmy"),
            PieEntry(300f, "Výdaje")
        )

        val dataSet = PieDataSet(entries, "Statistiky")
        val data = PieData(dataSet)

        pieChart.data = data
        pieChart.invalidate()
        val dao = TransactionDatabase.getDatabase(this).transactionDao()
        repository = TransactionRepository(dao)

        loadStatistics()

        val button = findViewById<Button>(R.id.btnExportCSV)
        button.setOnClickListener {
            val animator = ObjectAnimator.ofFloat(it, "scaleX", 1f, 1.1f, 1f)
            animator.duration = 200
            animator.start()

            exportToCSV(this)
        }

        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }




    }


    private fun loadStatistics() {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = repository.getAllTransactions()

            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val balance = totalIncome - totalExpense

            withContext(Dispatchers.Main) {
                findViewById<TextView>(R.id.tvTotalIncome).text = "Příjmy: ${totalIncome} Kč"
                findViewById<TextView>(R.id.tvTotalExpense).text = "Výdaje: ${totalExpense} Kč"
                findViewById<TextView>(R.id.tvBalance).text = "Zůstatek: ${balance} Kč"

                setupPieChart(totalIncome.toFloat(), totalExpense.toFloat())
            }
        }
    }

    private fun setupPieChart(income: Float, expense: Float) {
        val pieChart = findViewById<PieChart>(R.id.pieChart)

        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(income, "Příjmy"))
        entries.add(PieEntry(expense, "Výdaje"))

        val dataSet = PieDataSet(entries, "Finanční přehled")
        dataSet.colors = listOf(Color.GREEN, Color.RED)

        val data = PieData(dataSet)
        pieChart.data = data

        pieChart.invalidate() // Aktualizace grafu
    }



    private fun exportToCSV(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            val transactions = repository.getAllTransactions()

            val csvHeader = "ID,Typ,Částka,Kategorie,Datum\n"
            val csvData = StringBuilder(csvHeader)

            for (transaction in transactions) {
                csvData.append("${transaction.id},${transaction.type},${transaction.amount},${transaction.category},${transaction.date}\n")
            }

            val fileName = "finance_data.csv"
            var outputStream: OutputStream? = null
            var uri: Uri? = null

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // 📌 Android 10+ (API 29+): Použití MediaStore
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }

                    val resolver = context.contentResolver
                    uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
                    outputStream = uri?.let { resolver.openOutputStream(it) }
                } else {
                    // 📌 Android 9 a starší (API 28-): Uložení do interní složky aplikace
                    val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
                    outputStream = FileOutputStream(file)
                }

                if (outputStream != null) {
                    outputStream.write(csvData.toString().toByteArray())
                    outputStream.close()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "CSV exportováno do složky Stahování/Dokumenty", Toast.LENGTH_LONG).show()
                    }
                } else {
                    throw Exception("Nepodařilo se vytvořit soubor")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Chyba při exportu: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }



}
