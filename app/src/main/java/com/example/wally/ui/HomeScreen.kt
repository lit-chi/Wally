package com.example.wally.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*

import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wally.data.*
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.Calendar

enum class EntryMode {
    PAYMENT, DUE
}

private fun todayRange(): Pair<Long, Long> {
    val start = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val end = start.clone() as Calendar
    end.add(Calendar.DAY_OF_YEAR, 1)

    return start.timeInMillis to end.timeInMillis
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    expenseDao: ExpenseDao,
    dueDao: DueDao,
    tagDao: TagDao
) {
    var mode by remember { mutableStateOf(EntryMode.PAYMENT) }
    var showStats by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var personName by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf<String?>(null) }
    var expenseToDelete by remember { mutableStateOf<Expense?>(null) }
    var dueToDelete by remember {mutableStateOf<Due?>(null)}

    val tags by tagDao.getAllTags()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var showTagSelector by remember { mutableStateOf(false) }

    val expenses by expenseDao.getAllExpenses()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val dues by dueDao.getAllDues()
        .collectAsStateWithLifecycle(initialValue = emptyList())

    val tagUsage = remember(expenses, dues) {
        (expenses.map { it.tag } + dues.map { it.tag })
            .groupingBy { it }
            .eachCount()
    }

    val visibleTags = tags
        .sortedWith(
            compareByDescending<com.example.wally.data.Tag> {
                tagUsage[it.name] ?: 0
            }.thenBy { it.name }
        )
        .take(6)

    val scope = rememberCoroutineScope()

    val (todayStart, todayEnd) = remember {
        todayRange()
    }

    val todayPayments by expenseDao.getTodayTotal(todayStart, todayEnd)
        .collectAsStateWithLifecycle(initialValue = 0)

    val todayDues by dueDao.getTodayTotal(todayStart, todayEnd)
        .collectAsStateWithLifecycle(initialValue = 0)

    val peopleDue by dueDao.getPeopleDue()
        .collectAsStateWithLifecycle(initialValue = 0)

    if (showStats) {
        StatsScreen(
            todayPayments = todayPayments,
            todayDues = todayDues,
            peopleDue = peopleDue,
            onBack = { showStats = false }
        )
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "WALLY",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = MaterialTheme.typography.titleLarge.letterSpacing
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showStats = true }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Savings,
                            contentDescription = "Statistics",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            ModeSelector(
                mode = mode,
                onModeChange = {
                    mode = it
                    description = ""
                    personName = ""
                    amount = ""
                    selectedTag = null
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            EntryCard(
                isDue = mode == EntryMode.DUE,
                description = description,
                personName = personName,
                amount = amount,
                tags = visibleTags.map { it.name },
                selectedTag = selectedTag,
                onDescriptionChange = { description = it },
                onPersonNameChange = { personName = it },
                onAmountChange = {
                    amount = it.filter { char -> char.isDigit() }
                },
                onTagSelected = { selectedTag = it },
                onAddTag = { showTagSelector = true },
                onSubmit = {
                    val parsedAmount = amount.toIntOrNull()
                        ?: return@EntryCard

                    val tag = selectedTag
                        ?: return@EntryCard

                    scope.launch {

                        if (mode == EntryMode.PAYMENT) {

                            expenseDao.insertExpense(
                                Expense(
                                    description = description.trim(),
                                    amount = parsedAmount,
                                    tag = tag
                                )
                            )

                        } else {

                            if (personName.isBlank()) {
                                return@launch
                            }

                            dueDao.insertDue(
                                Due(
                                    name = personName.trim(),
                                    description = description.trim(),
                                    amount = parsedAmount,
                                    tag = tag
                                )
                            )
                        }

                        description = ""
                        personName = ""
                        amount = ""
                        selectedTag = null
                    }
                }
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (mode == EntryMode.PAYMENT) {
                    "EXPENSES"
                } else {
                    "DUES"
                },
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {

                if (mode == EntryMode.PAYMENT) {

                    items(
                        expenses,
                        key = { it.id }
                    ) { expense ->
                        PaymentRow(
                            expense = expense,
                            onLongClick = {expenseToDelete = expense}
                        )
                    }

                } else {

                    items(
                        dues,
                        key = { it.id }
                    ) { due ->
                        DueRow(
                            due=due,
                            onLongClick = {dueToDelete = due}
                        )
                    }
                }
            }
        }
    }
    if (showTagSelector) {
        TagSelector(
            tags = tags,
            selectedTag = selectedTag,

            onTagSelected = { tag ->
                selectedTag = tag
            },

            onAddTag = { tagName ->
                scope.launch {
                    tagDao.insertTag(
                        Tag(name = tagName)
                    )
                }
            },

            onDeleteTag = { tag ->
                scope.launch {
                    tagDao.deleteTag(tag)

                    if (selectedTag == tag.name) {
                        selectedTag = null
                    }
                }
            },

            onDismiss = {
                showTagSelector = false
            }
        )
    }
    if(expenseToDelete != null){
        AlertDialog(
            onDismissRequest = {expenseToDelete = null},
            title = {Text("Delete ${expenseToDelete?.description} ?")},
            dismissButton = {
                TextButton(onClick = {expenseToDelete = null }) {
                    Text("CANCEL")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val expense = expenseToDelete ?: return@TextButton
                        scope.launch {
                            expenseDao.deleteExpense(expense.id)
                        }
                        expenseToDelete = null
                    }
                ){
                    Text("DELETE")
                }
            }
        )
    }

}







