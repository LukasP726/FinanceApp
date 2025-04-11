package com.example.financeapp.ui

import android.app.Activity
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.View
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

/**
 * Hlavní aktivita aplikace pro správu financí.
 * Zobrazuje seznam transakcí, umožňuje přidávání, filtrování, mazání a přepínání motivu.
 */
class MainActivity : AppCompatActivity()  {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TransactionAdapter
    private var minAmount: Double = 0.0
    private var maxAmount: Double = Double.MAX_VALUE
    private var category: String = "Vše"
    private var dateFrom: Long = 0L
    private var dateTo: Long = Long.MAX_VALUE

    private var filteredTransactions: List<Transaction> = emptyList()

    companion object {
        private const val ADD_TRANSACTION_REQUEST_CODE = 1
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
                    applyFilter(minAmount, maxAmount, category, dateFrom, dateTo)
                }
            }
        }

    /**
     * Inicializace aktivity a UI komponent.
     */
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

        binding.fabOpenFilter.setOnClickListener {
            val intent = Intent(this, FilterActivity::class.java)
            filterActivityResultLauncher.launch(intent)
        }

        binding.fabOpenStatistics.setOnClickListener {
            val intent = Intent(this, StatisticsActivity::class.java).apply {
                putExtra("minAmount", minAmount)
                putExtra("maxAmount", maxAmount)
                putExtra("category", category)
                putExtra("dateFrom", dateFrom)
                putExtra("dateTo", dateTo)
            }
            startActivity(intent)
        }

        binding.btnToggleTheme.setOnClickListener {
            val nightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
            if (nightMode == Configuration.UI_MODE_NIGHT_YES) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            }
            recreate()
        }

        loadTransactions()

        if (savedInstanceState != null) {
            minAmount = savedInstanceState.getDouble("minAmount", 0.0)
            maxAmount = savedInstanceState.getDouble("maxAmount", Double.MAX_VALUE)
            category = savedInstanceState.getString("category", "Vše") ?: "Vše"
            dateFrom = savedInstanceState.getLong("dateFrom", 0L)
            dateTo = savedInstanceState.getLong("dateTo", Long.MAX_VALUE)
            applyFilter(minAmount, maxAmount, category, dateFrom, dateTo)
        }
    }

    /**
     * Uložení stavu filtru při rotaci obrazovky apod.
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putDouble("minAmount", minAmount)
        outState.putDouble("maxAmount", maxAmount)
        outState.putString("category", category)
        outState.putLong("dateFrom", dateFrom)
        outState.putLong("dateTo", dateTo)
    }

    /**
     * Načte a zobrazí všechny transakce pomocí ViewModelu.
     */
    private fun loadTransactions() {
        viewModel.allTransactions.observe(this) { transactions ->
            adapter.updateData(transactions)
        }
    }

    /**
     * Zobrazí kontextové menu (úprava/smazání) pro vybranou transakci.
     */
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

    /**
     * Zobrazí dialogové okno pro potvrzení smazání transakce.
     */
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

    /**
     * Smaže transakci a znovu načte seznam.
     */
    private fun deleteTransaction(transaction: Transaction) {
        viewModel.deleteTransaction(transaction) {
            runOnUiThread { loadTransactions() }
        }
    }

    /**
     * Spustí aktivitu pro úpravu existující transakce.
     */
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

    /**
     * Aplikuje filtr na seznam transakcí podle zadaných kritérií.
     */
    private fun applyFilter(minAmount: Double, maxAmount: Double, category: String, dateFrom: Long, dateTo: Long) {
        viewModel.getAllTransactions { transactions ->
            filteredTransactions = transactions.filter {
                it.amount in minAmount..maxAmount &&
                        (category == "Vše" || it.category == category) &&
                        it.date in dateFrom..dateTo
            }
            runOnUiThread {
                adapter.updateData(filteredTransactions)
            }
        }
    }
}
