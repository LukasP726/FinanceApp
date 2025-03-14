package com.example.financeapp.ui

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

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: TransactionAdapter
    companion object {
        private const val ADD_TRANSACTION_REQUEST_CODE = 1
    }

    private val viewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(TransactionRepository(TransactionDatabase.getDatabase(this).transactionDao()))
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

        binding.fabOpenStatistics.setOnClickListener{
            val intent = Intent(this, StatisticsActivity::class.java)
            startActivity(intent)
        }

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
















}