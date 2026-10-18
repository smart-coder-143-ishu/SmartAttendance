package com.smartattendance.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.smartattendance.app.data.model.AttendanceRecord
import com.smartattendance.app.data.model.Student
import com.smartattendance.app.data.repository.AttendanceRepository
import com.smartattendance.app.ui.screens.DailyAttendanceScreen
import com.smartattendance.app.ui.theme.SmartAttendanceTheme
import com.smartattendance.app.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SmartAttendanceApp
        val repository = app.repository
        val dashboardViewModel = DashboardViewModel(repository)

        setContent {
            SmartAttendanceTheme {
                MainAppScreen(repository = repository, dashboardViewModel = dashboardViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    repository: AttendanceRepository,
    dashboardViewModel: DashboardViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentTab by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

    val students by repository.allStudents.collectAsState(initial = emptyList())
    val dateRecords by remember(selectedDate) {
        repository.getRecordsForDate(selectedDate)
    }.collectAsState(initial = emptyList())
    val dashState by dashboardViewModel.uiState.collectAsState()

    var showAddStudentDialog by remember { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.CheckCircle, contentDescription = null) },
                    label = { Text("Attendance") }
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.People, contentDescription = null) },
                    label = { Text("Students (${students.size})") }
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                    label = { Text("Dashboard") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (currentTab) {
                0 -> {
                    if (students.isEmpty()) {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Please add students first!", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showAddStudentDialog = true }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Student")
                            }
                        }
                    } else {
                        val initialMap = remember(dateRecords) {
                            dateRecords.associate { it.studentId to it.isPresent }
                        }
                        DailyAttendanceScreen(
                            students = students,
                            selectedDate = selectedDate,
                            onDateChange = { selectedDate = it },
                            initialStatus = initialMap,
                            onSaveAttendance = { map ->
                                scope.launch {
                                    val records = map.map { (studentId, isPresent) ->
                                        AttendanceRecord(
                                            studentId = studentId,
                                            date = selectedDate,
                                            isPresent = isPresent
                                        )
                                    }
                                    repository.saveAttendance(records)
                                    dashboardViewModel.loadDashboardStats()
                                    Toast.makeText(context, "Attendance Saved!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
                1 -> {
                    Scaffold(
                        topBar = { TopAppBar(title = { Text("Manage Students") }) },
                        floatingActionButton = {
                            ExtendedFloatingActionButton(
                                onClick = { showAddStudentDialog = true },
                                icon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
                                text = { Text("Add Student") }
                            )
                        }
                    ) { pad ->
                        LazyColumn(
                            modifier = Modifier.padding(pad).fillMaxSize().padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(students, key = { it.id }) { student ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(student.name, style = MaterialTheme.typography.titleMedium)
                                            Text("Roll: ${student.rollNumber} | Class: ${student.className}")
                                        }
                                        IconButton(onClick = {
                                            scope.launch { repository.deleteStudent(student) }
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                2 -> {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("Dashboard Statistics", style = MaterialTheme.typography.headlineSmall)
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Total Students: ${dashState.totalStudents}", style = MaterialTheme.typography.titleMedium)
                                Text("Total Recorded Days: ${dashState.totalRecordedDays}")
                                Divider()
                                Text("Today Present: ${dashState.todayPresentCount}", color = MaterialTheme.colorScheme.primary)
                                Text("Today Absent: ${dashState.todayAbsentCount}", color = MaterialTheme.colorScheme.error)
                                Text("Today Attendance Rate: ${"%.1f".format(dashState.todayPercentage)}%")
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddStudentDialog) {
        var name by remember { mutableStateOf("") }
        var roll by remember { mutableStateOf("") }
        var className by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStudentDialog = false },
            title = { Text("Add New Student") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name") })
                    OutlinedTextField(value = roll, onValueChange = { roll = it }, label = { Text("Roll Number") })
                    OutlinedTextField(value = className, onValueChange = { className = it }, label = { Text("Class / Section") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (name.isNotBlank() && roll.isNotBlank() && className.isNotBlank()) {
                        scope.launch {
                            try {
                                repository.insertStudent(name, roll, className)
                                showAddStudentDialog = false
                            } catch (e: Exception) {
                                Toast.makeText(context, "Roll number already exists!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStudentDialog = false }) { Text("Cancel") }
            }
        )
    }
}
