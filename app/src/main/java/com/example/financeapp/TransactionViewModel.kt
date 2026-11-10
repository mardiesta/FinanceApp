package com.example.financeapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financeapp.data.DatabaseProvider
import com.example.financeapp.data.MINOR_UNITS
import com.example.financeapp.data.RoomTransactionRepository
import com.example.financeapp.data.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val STOP_TIMEOUT_MS = 5_000L

internal class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    private val form = MutableStateFlow(FormState())

    val uiState: StateFlow<MainUiState> = combine(
        form,
        repository.transactions,
        repository.balanceMinor,
        repository.categories,
    ) { currentForm, rows, balanceMinor, categories ->
        MainUiState(
            amount = currentForm.amount,
            description = currentForm.description,
            type = currentForm.type,
            categoryId = currentForm.categoryId,
            error = currentForm.error,
            balance = (balanceMinor / MINOR_UNITS).toInt(),
            transactionCount = rows.size,
            transactions = rows,
            categories = categories,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MainUiState())

    init {
        viewModelScope.launch { repository.seedDefaultCategoriesIfEmpty() }
    }

    fun onEvent(event: MainUiEvent) {
        form.update { reduceForm(it, event) }
    }

    fun addTransaction() {
        when (val result = submitForm(form.value)) {
            is SubmitResult.Accepted -> {
                viewModelScope.launch { repository.add(result.transaction) }
                form.update { it.copy(amount = "", description = "", error = null) }
            }

            is SubmitResult.Rejected -> form.update { it.copy(error = result.error) }
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY])
                val database = DatabaseProvider.get(application)
                TransactionViewModel(RoomTransactionRepository(database.transactionDao(), database.categoryDao()))
            }
        }
    }
}
