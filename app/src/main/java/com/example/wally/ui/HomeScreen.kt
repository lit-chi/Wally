package com.example.wally.ui


import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.wally.data.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.time.DayOfWeek
import java.time.ZoneId


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

fun LocalDate.toMillis(): Long {
    return atStartOfDay(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
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

    var selectedFilter by remember {mutableStateOf("Day")}
    var selectedDate by remember {mutableStateOf(LocalDate.now())}
    var selectedWeekStart by remember {
        mutableStateOf(
            LocalDate.now().with(DayOfWeek.MONDAY)
        )
    }


    val tags by tagDao.getAllTags()
        .collectAsStateWithLifecycle(initialValue = emptyList())
    var showTagSelector by remember { mutableStateOf(false) }

    val periodStartDate = if (selectedFilter == "Day") {
        selectedDate
    } else {
        selectedWeekStart
    }

    val periodEndDate = if (selectedFilter == "Day") {
        selectedDate.plusDays(1)
    } else {
        selectedWeekStart.plusDays(7)
    }

    val periodStart = periodStartDate.toMillis()
    val periodEnd = periodEndDate.toMillis()

    val expenses by remember(periodStart, periodEnd) {
        expenseDao.getExpensesBetween(periodStart, periodEnd)
    }.collectAsStateWithLifecycle(initialValue = emptyList())

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




    fun goNext(){
        if(selectedFilter == "Day"){
            selectedDate = selectedDate.plusDays(1)
        }
        else{
            selectedWeekStart = selectedWeekStart.plusDays(7)
        }
    }

    fun goPrevious(){
        if(selectedFilter == "Day"){
            selectedDate = selectedDate.minusDays(1)
        }
        else{
            selectedWeekStart = selectedWeekStart.minusDays(7)
        }
    }


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

            Spacer(modifier = Modifier.height(14.dp))

            if (mode == EntryMode.PAYMENT) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPENSES",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(
                        modifier = Modifier.weight(1f)
                    )


                    val selectedOffset by animateDpAsState(
                        targetValue = if (selectedFilter == "Day") 0.dp else 60.dp,
                        label = "selected filter"
                    )

                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .height(36.dp)
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .offset(x = selectedOffset)
                                    .width(60.dp)
                                    .fillMaxHeight()
                                    .background(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = RoundedCornerShape(50)
                                    )
                            )

                            // DAY / WEEK buttons
                            Row(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            selectedFilter = "Day"
                                            selectedDate = selectedWeekStart
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "DAY",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selectedFilter == "Day") {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable {
                                            selectedFilter = "Week"
                                            selectedWeekStart = selectedDate.with(DayOfWeek.MONDAY)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "WEEK",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selectedFilter == "Week") {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                        }
                    }
                }



                Spacer(modifier = Modifier.height(8.dp))
                val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { goPrevious() },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronLeft,
                            contentDescription = "Previous"
                        )
                    }

                    Text(
                        text = if (selectedFilter == "Day") {
                            selectedDate.format(dateFormatter)
                        } else {
                            "${selectedWeekStart.format(DateTimeFormatter.ofPattern("dd MMM"))} – " +
                                    "${
                                        selectedWeekStart.plusDays(6)
                                            .format(DateTimeFormatter.ofPattern("dd MMM"))
                                    }"
                        },
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )

                    IconButton(
                        onClick = { goNext() }
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChevronRight,
                            contentDescription = "Next"
                        )
                    }
                }

            } else {
                Text(
                    text = "DUES",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }






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








