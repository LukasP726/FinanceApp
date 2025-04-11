package com.example.financeapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.financeapp.data.TransactionRepository

/**
 * Factory třída pro vytvoření instance ViewModelu TransactionViewModel.
 * Tato třída je zodpovědná za poskytování závislostí (TransactionRepository)
 * při vytváření instancí TransactionViewModel.
 */
class TransactionViewModelFactory(private val repository: TransactionRepository) : ViewModelProvider.Factory {

    /**
     * Vytváří novou instanci TransactionViewModelu.
     * Pokud je požadován jiný typ ViewModelu, vyhodí výjimku.
     */
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
