package com.example.financeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionViewModelTest {
    @Test
    fun amount_acceptsOnlyDigits() {
        val viewModel = TransactionViewModel()

        viewModel.onEvent(MainUiEvent.AmountChanged("12a"))
        assertEquals("", viewModel.uiState.amount)

        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        assertEquals("250", viewModel.uiState.amount)
    }

    @Test
    fun expense_decreasesBalanceAndAddsHistory() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))

        viewModel.addTransaction()

        assertEquals(-250, viewModel.uiState.balance)
        assertEquals(1, viewModel.uiState.transactionCount)
        assertEquals(1, viewModel.uiState.transactions.size)
        assertEquals("", viewModel.uiState.amount)
        assertEquals("", viewModel.uiState.description)
    }

    @Test
    fun income_afterExpense_givesExpectedBalance() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.AmountChanged("250"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Кофе"))
        viewModel.addTransaction()

        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))
        viewModel.onEvent(MainUiEvent.AmountChanged("500"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Зарплата"))
        viewModel.addTransaction()

        assertEquals(250, viewModel.uiState.balance)
        assertEquals(2, viewModel.uiState.transactionCount)
        assertEquals(2, viewModel.uiState.transactions.size)
    }

    @Test
    fun emptyDescription_setsInvalidInputError() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.AmountChanged("250"))

        viewModel.addTransaction()

        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)
        assertEquals(0, viewModel.uiState.balance)
    }

    @Test
    fun tooLargeAmount_setsAmountTooLargeError() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.AmountChanged("99999999999"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Машина"))

        viewModel.addTransaction()

        assertEquals(TransactionError.AMOUNT_TOO_LARGE, viewModel.uiState.error)
        assertEquals(0, viewModel.uiState.balance)
    }

    @Test
    fun overflowingBalance_keepsPreviousBalance() {
        val viewModel = TransactionViewModel()
        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))
        viewModel.onEvent(MainUiEvent.AmountChanged(Int.MAX_VALUE.toString()))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Большой доход"))
        viewModel.addTransaction()

        viewModel.onEvent(MainUiEvent.AmountChanged("1"))
        viewModel.onEvent(MainUiEvent.DescriptionChanged("Ещё один доход"))
        viewModel.addTransaction()

        assertEquals(Int.MAX_VALUE, viewModel.uiState.balance)
        assertEquals(1, viewModel.uiState.transactionCount)
        assertEquals(TransactionError.AMOUNT_TOO_LARGE, viewModel.uiState.error)
    }

    @Test
    fun typeChange_clearsError() {
        val viewModel = TransactionViewModel()
        viewModel.addTransaction()
        assertEquals(TransactionError.INVALID_INPUT, viewModel.uiState.error)

        viewModel.onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME))

        assertNull(viewModel.uiState.error)
    }
}
