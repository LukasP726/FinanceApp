package com.example.financeapp.ui

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.financeapp.R

import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.financeapp.data.Transaction
import com.example.financeapp.data.TransactionDatabase
import com.example.financeapp.data.TransactionRepository
import com.example.financeapp.databinding.ActivityMainBinding
import com.example.financeapp.viewmodel.TransactionViewModel
import com.example.financeapp.viewmodel.TransactionViewModelFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import androidx.fragment.app.DialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TransactionAdapter
    //private val FILTER_REQUEST_CODE = 1001
    //private var allTransactions: List<Transaction> = listOf()
    private var minAmount: Double = 0.0
    private var maxAmount: Double = Double.MAX_VALUE
    private var category: String = "Vše"
    private var dateFrom: Long = 0L
    private var dateTo: Long = Long.MAX_VALUE




    private var filteredTransactions: List<Transaction> = emptyList()
    companion object {
        private const val ADD_TRANSACTION_REQUEST_CODE = 1
        //private const val FILTER_REQUEST_CODE = 1
    }

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(TransactionRepository(TransactionDatabase.getDatabase(this).transactionDao()))
    }


    private val filterActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                     minAmount = data.getDoubleExtra("minAmount", 0.0)
                     maxAmount = data.getDoubleExtra("maxAmount", Double.MAX_VALUE)
                     category = data.getStringExtra("category") ?: "Vše"
                     dateFrom = data.getLongExtra("dateFrom", 0L)
                     dateTo = data.getLongExtra("dateTo", Long.MAX_VALUE)

                    // Aplikuj filtr na seznam transakcí
                    applyFilter(minAmount, maxAmount, category, dateFrom, dateTo)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {



        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = TransactionAdapter(emptyList()) { transaction, view ->
            showPopupMenu(transaction, view)
        }
        binding.rvTransactions.layoutManager = LinearLayoutManager(this)
        binding.rvTransactions.adapter = adapter

        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }



        binding.fabOpenFilter.setOnClickListener{
            val intent = Intent(this, FilterActivity::class.java)
            filterActivityResultLauncher.launch(intent)

        }

        binding.fabOpenStatistics.setOnClickListener{
            val intent = Intent(this, StatisticsActivity::class.java).apply {
                putExtra("minAmount", minAmount)
                putExtra("maxAmount", maxAmount)
                putExtra("category", category)
                putExtra("dateFrom", dateFrom)
                putExtra("dateTo", dateTo)
            }
            startActivity(intent)
        }

/*
        findViewById<FloatingActionButton>(R.id.btnOpenFilter).setOnClickListener {
            val filterIntent = Intent(this, FilterActivity::class.java)
            filterLauncher.launch(filterIntent) // Spustí aktivitu filtrace
        }
*/
        binding.btnToggleTheme.setOnClickListener{
            val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                //binding.btnToggleTheme.setImageResource(android.R.drawable.ic_menu_month)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                //binding.btnToggleTheme.setImageResource(android.R.drawable.ic_menu_day)
            }



            recreate()

        }






        loadTransactions()
    }
/*
    // Načítání transakcí a jejich uložení
    private fun loadTransactions() {
        viewModel.allTransactions.observe(this) { transactions ->
            allTransactions = transactions
            adapter.updateData(allTransactions) // Načte všechny transakce na začátku
        }
    }

 */

    private fun loadTransactions() {

        viewModel.allTransactions.observe(this) { transactions ->
            adapter.updateData(transactions)
        }


    }


    private fun showPopupMenu(transaction: Transaction, view: View) {
        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.transaction_menu, popup.menu)

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.edit_transaction -> editTransaction(transaction)
                R.id.delete_transaction -> confirmDeleteTransaction(transaction)
            }
            true
        }
        popup.show()
    }

    private fun confirmDeleteTransaction(transaction: Transaction) {
        AlertDialog.Builder(this)
            .setTitle("Smazat transakci")
            .setMessage("Opravdu chcete smazat tuto transakci?")
            .setPositiveButton("Ano") { _, _ ->
                deleteTransaction(transaction)
            }
            .setNegativeButton("Ne", null)
            .show()
    }

    private fun deleteTransaction(transaction: Transaction) {
        viewModel.deleteTransaction(transaction) {
            runOnUiThread { loadTransactions() }
        }
    }

    private fun editTransaction(transaction: Transaction) {
        val intent = Intent(this, AddTransactionActivity::class.java).apply {
            putExtra("transaction_id", transaction.id)
            putExtra("amount", transaction.amount)
            putExtra("type", transaction.type)
            putExtra("date", transaction.date)
            putExtra("category", transaction.category)
        }
        startActivity(intent)
    }




    private fun applyFilter(minAmount: Double, maxAmount: Double, category: String, dateFrom: Long, dateTo: Long) {
        viewModel.getAllTransactions { transactions ->
             filteredTransactions = transactions.filter { //val
                it.amount in minAmount..maxAmount &&
                        (it.category == category) && //category == "Vše" ||
                        it.date in dateFrom..dateTo
            }
            runOnUiThread {
                adapter.updateData(filteredTransactions)
            }
        }
    }

/*
    // Výsledek z FilterActivity
    private val filterLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                val selectedType = data?.getStringExtra("selectedType")
                val amount = data?.getStringExtra("amount")
                val category = data?.getStringExtra("category")

                applyFilter(selectedType, amount, category)
            }
        }

    // Metoda na aplikaci filtru
    private fun applyFilter(type: String?, amount: String?, category: String?) {
        val amountDouble = amount?.toDoubleOrNull() // Převod String na Double (nebo null, pokud je neplatný)

        val filteredTransactions = allTransactions.filter { transaction ->
            (type.isNullOrEmpty() || transaction.type == type) &&
                    (amountDouble == null || transaction.amount == amountDouble) && // Správné porovnání s Double
                    (category.isNullOrEmpty() || transaction.category.contains(category, ignoreCase = true))
        }
        adapter.updateData(filteredTransactions)
    }

*/























}