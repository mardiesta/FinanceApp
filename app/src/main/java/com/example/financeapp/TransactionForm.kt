package com.example.financeapp

internal data class FormState(
    val amount: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE,
    val categoryId: Long? = null,
    val error: TransactionError? = null,
)

internal sealed interface MainUiEvent {
    data class AmountChanged(val value: String) : MainUiEvent

    data class DescriptionChanged(val value: String) : MainUiEvent

    data class TypeChanged(val type: TransactionType) : MainUiEvent

    data class CategoryChanged(val categoryId: Long?) : MainUiEvent
}

internal sealed interface SubmitResult {
    data class Accepted(val transaction: Transaction) : SubmitResult

    data class Rejected(val error: TransactionError) : SubmitResult
}

internal fun reduceForm(state: FormState, event: MainUiEvent): FormState = when (event) {
    is MainUiEvent.AmountChanged -> {
        if (event.value.all(Char::isDigit)) {
            state.copy(amount = event.value, error = null)
        } else {
            state
        }
    }

    is MainUiEvent.DescriptionChanged -> state.copy(description = event.value, error = null)

    is MainUiEvent.TypeChanged -> state.copy(type = event.type, error = null)

    is MainUiEvent.CategoryChanged -> state.copy(categoryId = event.categoryId)
}

internal fun submitForm(state: FormState): SubmitResult {
    val description = state.description.trim()
    val amount = state.amount.toIntOrNull()
    return when {
        state.amount.isEmpty() || description.isEmpty() -> SubmitResult.Rejected(TransactionError.INVALID_INPUT)
        amount == null -> SubmitResult.Rejected(TransactionError.AMOUNT_TOO_LARGE)
        amount <= 0 -> SubmitResult.Rejected(TransactionError.INVALID_INPUT)
        else -> SubmitResult.Accepted(Transaction(amount, state.type, description, state.categoryId))
    }
}
