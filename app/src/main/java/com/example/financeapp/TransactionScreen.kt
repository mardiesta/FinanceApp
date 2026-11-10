package com.example.financeapp

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.financeapp.data.MINOR_UNITS
import com.example.financeapp.data.TransactionRow

@Composable
@Suppress("FunctionNaming")
internal fun TransactionScreen(viewModel: TransactionViewModel = viewModel(factory = TransactionViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(space = 12.dp),
        ) {
            item {
                Text(
                    text = stringResource(R.string.balance_summary, state.balance, state.transactionCount),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
            item {
                TransactionForm(
                    state = state,
                    onEvent = viewModel::onEvent,
                    onSubmit = viewModel::addTransaction,
                )
            }
            item {
                Text(
                    text = stringResource(R.string.history_title),
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            items(state.transactions, key = { it.id }) { row ->
                TransactionItem(row)
            }
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionForm(state: MainUiState, onEvent: (MainUiEvent) -> Unit, onSubmit: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        OutlinedTextField(
            value = state.amount,
            onValueChange = { onEvent(MainUiEvent.AmountChanged(it)) },
            label = { Text(stringResource(R.string.transaction_amount)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = { onEvent(MainUiEvent.DescriptionChanged(it)) },
            label = { Text(stringResource(R.string.transaction_description)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            FilterChip(
                selected = state.type == TransactionType.INCOME,
                onClick = { onEvent(MainUiEvent.TypeChanged(TransactionType.INCOME)) },
                label = { Text(stringResource(R.string.type_income)) },
            )
            FilterChip(
                selected = state.type == TransactionType.EXPENSE,
                onClick = { onEvent(MainUiEvent.TypeChanged(TransactionType.EXPENSE)) },
                label = { Text(stringResource(R.string.type_expense)) },
            )
        }
        CategoryChips(state = state, onEvent = onEvent)
        Button(onClick = onSubmit, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.add_transaction_button))
        }
        state.error?.let { error ->
            Text(
                text = stringResource(errorMessageRes(error)),
                color = MaterialTheme.colorScheme.error,
            )
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun CategoryChips(state: MainUiState, onEvent: (MainUiEvent) -> Unit) {
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        state.categories.forEach { category ->
            FilterChip(
                selected = state.categoryId == category.id,
                onClick = { onEvent(MainUiEvent.CategoryChanged(category.id)) },
                label = { Text(category.name) },
            )
        }
    }
}

@Composable
@Suppress("FunctionNaming")
private fun TransactionItem(row: TransactionRow) {
    val category = row.categoryName ?: stringResource(R.string.no_category)
    val amountRubles = row.amountMinor / MINOR_UNITS
    val text = if (row.type == TransactionType.INCOME.name) {
        stringResource(R.string.income_item, row.description, category, amountRubles)
    } else {
        stringResource(R.string.expense_item, row.description, category, amountRubles)
    }
    Text(text = text, style = MaterialTheme.typography.bodyLarge)
}

private fun errorMessageRes(error: TransactionError): Int = when (error) {
    TransactionError.INVALID_INPUT -> R.string.transaction_invalid_input
    TransactionError.AMOUNT_TOO_LARGE -> R.string.transaction_amount_too_large
}
