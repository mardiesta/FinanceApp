package com.example.financeapp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

internal class TransactionViewModel : ViewModel() {
    var uiState by mutableStateOf(MainUiState())
        private set

    fun onEvent(event: MainUiEvent) {
        uiState = reduce(uiState, event)
    }

    fun addTransaction() {
        onEvent(createAddEvent())
    }

    private fun createAddEvent(): MainUiEvent {
        val description = uiState.description.trim()
        val amount = uiState.amount.toIntOrNull()
        return when {
            uiState.amount.isEmpty() || description.isEmpty() -> MainUiEvent.InvalidTransaction
            amount == null -> MainUiEvent.AmountTooLarge
            amount <= 0 -> MainUiEvent.InvalidTransaction
            else -> MainUiEvent.AddTransaction(Transaction(amount, uiState.type, description))
        }
    }

    private fun reduce(state: MainUiState, event: MainUiEvent): MainUiState = when (event) {
        is MainUiEvent.AmountChanged -> {
            if (event.value.all(Char::isDigit)) {
                state.copy(amount = event.value, error = null)
            } else {
                state
            }
        }

        is MainUiEvent.DescriptionChanged -> state.copy(description = event.value, error = null)

        is MainUiEvent.TypeChanged -> state.copy(type = event.type, error = null)

        is MainUiEvent.AddTransaction -> applyTransaction(state, event.transaction)

        MainUiEvent.InvalidTransaction -> state.copy(error = TransactionError.INVALID_INPUT)

        MainUiEvent.AmountTooLarge -> state.copy(error = TransactionError.AMOUNT_TOO_LARGE)
    }

    private fun applyTransaction(state: MainUiState, transaction: Transaction): MainUiState {
        require(transaction.amount > 0) { "Transaction amount must be positive" }
        val change = when (transaction.type) {
            TransactionType.INCOME -> transaction.amount.toLong()
            TransactionType.EXPENSE -> -transaction.amount.toLong()
        }
        val newBalance = state.balance.toLong() + change
        return if (newBalance in Int.MIN_VALUE.toLong()..Int.MAX_VALUE.toLong()) {
            state.copy(
                amount = "",
                description = "",
                error = null,
                balance = newBalance.toInt(),
                transactions = state.transactions + transaction,
                transactionCount = state.transactionCount + 1,
            )
        } else {
            state.copy(error = TransactionError.AMOUNT_TOO_LARGE)
        }
    }
}
