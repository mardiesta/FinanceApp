package com.example.financeapp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TransactionFormTest {
    @Test
    fun amount_acceptsOnlyDigits() {
        val rejected = reduceForm(FormState(), MainUiEvent.AmountChanged("12a"))
        assertEquals("", rejected.amount)

        val accepted = reduceForm(rejected, MainUiEvent.AmountChanged("250"))
        assertEquals("250", accepted.amount)
    }

    @Test
    fun emptyDescription_isRejectedAsInvalidInput() {
        val result = submitForm(FormState(amount = "250"))

        assertEquals(SubmitResult.Rejected(TransactionError.INVALID_INPUT), result)
    }

    @Test
    fun zeroAmount_isRejectedAsInvalidInput() {
        val result = submitForm(FormState(amount = "0", description = "Кофе"))

        assertEquals(SubmitResult.Rejected(TransactionError.INVALID_INPUT), result)
    }

    @Test
    fun tooLargeAmount_isRejected() {
        val result = submitForm(FormState(amount = "99999999999", description = "Машина"))

        assertEquals(SubmitResult.Rejected(TransactionError.AMOUNT_TOO_LARGE), result)
    }

    @Test
    fun validForm_isAcceptedWithTrimmedDescription() {
        val result = submitForm(
            FormState(amount = "250", description = "  Кофе ", type = TransactionType.EXPENSE, categoryId = 3L),
        )

        assertEquals(
            SubmitResult.Accepted(Transaction(250, TransactionType.EXPENSE, "Кофе", 3L)),
            result,
        )
    }

    @Test
    fun typeChange_clearsError() {
        val initial = FormState(error = TransactionError.INVALID_INPUT)

        val state = reduceForm(initial, MainUiEvent.TypeChanged(TransactionType.INCOME))

        assertNull(state.error)
        assertEquals(TransactionType.INCOME, state.type)
    }

    @Test
    fun categoryChange_selectsCategory() {
        val state = reduceForm(FormState(), MainUiEvent.CategoryChanged(7L))

        assertEquals(7L, state.categoryId)
    }
}
