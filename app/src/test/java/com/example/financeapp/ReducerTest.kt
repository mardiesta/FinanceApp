package com.example.financeapp

import org.junit.Assert.assertEquals
import org.junit.Test

class ReducerTest {
    @Test
    fun income_increasesBalanceAndCount() {
        val state = reduce(
            MainUiState(),
            MainUiEvent.AddTransaction(Transaction(500, TransactionType.INCOME)),
        )

        assertEquals(25_500, state.balance)
        assertEquals(1, state.transactionCount)
    }

    @Test
    fun expense_decreasesBalanceAndIncrementsCount() {
        val afterIncome = reduce(
            MainUiState(),
            MainUiEvent.AddTransaction(Transaction(500, TransactionType.INCOME)),
        )
        val state = reduce(
            afterIncome,
            MainUiEvent.AddTransaction(Transaction(250, TransactionType.EXPENSE)),
        )

        assertEquals(25_250, state.balance)
        assertEquals(2, state.transactionCount)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nonPositiveAmount_isRejected() {
        reduce(
            MainUiState(),
            MainUiEvent.AddTransaction(Transaction(0, TransactionType.INCOME)),
        )
    }
}
