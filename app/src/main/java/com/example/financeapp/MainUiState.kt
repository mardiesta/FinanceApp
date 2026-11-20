package com.example.financeapp

import com.example.financeapp.data.CategoryEntity
import com.example.financeapp.data.TransactionRow

internal data class MainUiState(
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val error: TransactionError? = null,
    val balance: Int = 0,
    val transactionCount: Int = 0,
    val transactions: List<TransactionRow> = emptyList(),
    val categories: List<CategoryEntity> = emptyList(),
)

internal enum class TransactionType {
    INCOME,
    EXPENSE,
}

internal data class Transaction(
    val amount: Int,
    val type: TransactionType,
    val description: String,
    val categoryId: Long?,
)

internal enum class TransactionError {
    INVALID_INPUT,
    AMOUNT_TOO_LARGE,
}
