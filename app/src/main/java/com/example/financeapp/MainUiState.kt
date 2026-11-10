package com.example.financeapp

internal data class MainUiState(
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val balance: Int = 0,
    val transactionCount: Int = 0,
    val transactions: List<Transaction> = emptyList(),
    val error: TransactionError? = null,
)

internal enum class TransactionType {
    INCOME,
    EXPENSE,
}

internal data class Transaction(
    val amount: Int,
    val type: TransactionType,
    val description: String,
)

internal enum class TransactionError {
    INVALID_INPUT,
    AMOUNT_TOO_LARGE,
}

internal sealed interface MainUiEvent {
    data class AmountChanged(val value: String) : MainUiEvent

    data class DescriptionChanged(val value: String) : MainUiEvent

    data class TypeChanged(val type: TransactionType) : MainUiEvent

    data class AddTransaction(val transaction: Transaction) : MainUiEvent

    data object InvalidTransaction : MainUiEvent

    data object AmountTooLarge : MainUiEvent
}
