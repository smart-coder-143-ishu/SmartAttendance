package com.smartattendance.app

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartattendance.app.data.model.AttendanceRecord
import com.smartattendance.app.data.model.Student
import com.smartattendance.app.data.repository.AttendanceRepository
import com.smartattendance.app.ui.viewmodel.DashboardViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

// AI Studio Premium Dark Theme Colors
val Slate950 = Color(0xFF020617)
val Slate900 = Color(0xFF0F172A)
val Slate800 = Color(0xFF1E293B)
val Indigo600 = Color(0xFF4F46E5)
val Emerald500 = Color(0xFF10B981)
val Rose500 = Color(0xFFF43F5E)
val TextGray = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as SmartAttendanceApp
        val repository = app.repository
        val dashboardViewModel = DashboardViewModel(repository)

        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    background = Slate950,
                    surface = Slate900,
                    primary = Indigo600,
                    secondary = Emerald500,
                    error = Rose500
                )
            ) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    PremiumMainScreen(repository, dashboardViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumMainScreen(repository: AttendanceRepository, viewModel: DashboardViewModel) {
    var currentTab by remember { mutableStateOf(0) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    val students by repository.allStudents.collectAsState(initial = emptyList())
    val dashState by viewModel.uiState.collectAsState()
    
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }
    val dateRecords by remember(selectedDate) { repository.getRecordsForDate(selectedDate) }.collectAsState(initial = emptyList())

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Slate950, tonalElevation = 8.dp) {
                val tabs = listOf(
                    Triple("Dashboard", Icons.Default.Dashboard, 0),
                    Triple("Daily", Icons.Default.CheckCircle, 1),
                    Triple("Students", Icons.Default.People, 2),
                    Triple("Stats", Icons.Default.Analytics, 3)
                )
                tabs.forEach { (title, icon, index) ->
                    NavigationBarItem(
                        selected = currentTab == index,
                        onClick = { currentTab = index },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Indigo600,
                            indicatorColor = Indigo600.copy(alpha = 0.3f),
                            unselectedIconColor = TextGray,
                            unselectedTextColor = TextGray
                        )
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize().background(Slate950)) {
            when (currentTab) {
                0 -> DashboardTab(dashState, students.size, onNavigate = { currentTab = it })
                1 -> DailyAttendanceTab(
                    students = students,
                    records = dateRecords,
                    selectedDate = selectedDate,
                    onSave = { map ->
                        scope.launch {
                            val newRecords = map.map { (id, isP) -> AttendanceRecord(studentId = id, date = selectedDate, isPresent = isP) }
                            repository.saveAttendance(newRecords)
                            viewModel.loadDashboardStats()
                            Toast.makeText(context, "Attendance Saved Successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                )
                2 -> StudentsTab(
                    students = students,
                    onAdd = { name, roll, cls -> scope.launch { repository.insertStudent(name, roll, cls) } },
                    onUpdate = { student -> scope.launch { repository.updateStudent(student) } },
                    onDelete = { student -> scope.launch { repository.deleteStudent(student) } }
                )
                3 -> StatsTab(students = students, dashState = dashState)
            }
        }
    }
}

// ---------------- 1. DASHBOARD TAB ----------------
@Composable
fun DashboardTab(state: com.smartattendance.app.ui.viewmodel.DashboardUiState, totalStudents: Int, onNavigate: (Int) -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Smart Attendance", color = Indigo600, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("Dashboard", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            DashboardCard(title = "Total Students", value = totalStudents.toString(), icon = Icons.Default.People, color = Indigo600, modifier = Modifier.weight(1f))
            DashboardCard(title = "Avg Turnout", value = "${"%.1f".format(state.todayPercentage)}%", icon = Icons.Default.TrendingUp, color = Emerald500, modifier = Modifier.weight(1f))
        }

        Card(colors = CardDefaults.cardColors(containerColor = Slate900), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Today's Snapshot", color = Color.White, fontWeight = FontWeight.Bold)
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatBox("Present", state.todayPresentCount, Emerald500, Modifier.weight(1f))
                    StatBox("Absent", state.todayAbsentCount, Rose500, Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Turnout", color = TextGray, fontSize = 12.sp)
                    Text("${"%.1f".format(state.todayPercentage)}%", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = if (totalStudents > 0) state.todayPresentCount.toFloat() / totalStudents else 0f,
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = Emerald500,
                    trackColor = Slate800
                )

                Button(
                    onClick = { onNavigate(1) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Slate800, contentColor = Indigo600),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Take Attendance Now")
                }
            }
        }
    }
}

@Composable
fun DashboardCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier) {
    Card(colors = CardDefaults.cardColors(containerColor = Slate800), shape = RoundedCornerShape(16.dp), modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Text(title, color = TextGray, fontSize = 12.sp)
        }
    }
}

@Composable
fun StatBox(title: String, count: Int, color: Color, modifier: Modifier) {
    Box(modifier = modifier.background(color.copy(alpha = 0.15f), RoundedCornerShape(12.dp)).padding(12.dp)) {
        Column {
            Text(title, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text(count.toString(), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------- 2. DAILY ATTENDANCE TAB ----------------
@Composable
fun DailyAttendanceTab(students: List<Student>, records: List<AttendanceRecord>, selectedDate: String, onSave: (Map<Long, Boolean>) -> Unit) {
    var attendanceMap by remember(students, records) {
        val map = mutableMapOf<Long, Boolean>()
        students.forEach { s -> map[s.id] = records.find { it.studentId == s.id }?.isPresent ?: true }
        mutableStateOf(map)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Daily Attendance", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                TextButton(onClick = { attendanceMap = students.associate { it.id to true }.toMutableMap() }) {
                    Text("Mark All Present", color = Emerald500)
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = Slate900), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Date: $selectedDate", color = Indigo600, fontWeight = FontWeight.Bold)
                    Text("Present: ${attendanceMap.values.count { it }}", color = Emerald500, fontWeight = FontWeight.Bold)
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(students) { student ->
                val isPresent = attendanceMap[student.id] ?: true
                Card(colors = CardDefaults.cardColors(containerColor = Slate900), shape = RoundedCornerShape(16.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(modifier = Modifier.size(40.dp).background(Slate800, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                                Text("#${student.rollNumber}", color = TextGray, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Column {
                                Text(student.name, color = Color.White, fontWeight = FontWeight.Bold)
                                Text(student.className, color = TextGray, fontSize = 11.sp)
                            }
                        }
                        
                        Row(modifier = Modifier.background(Slate950, RoundedCornerShape(12.dp)).padding(4.dp)) {
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (isPresent) Emerald500 else Color.Transparent).clickable { attendanceMap = attendanceMap.toMutableMap().apply { this[student.id] = true } }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("P", color = if (isPresent) Color.White else TextGray, fontWeight = FontWeight.Bold)
                            }
                            Box(modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(if (!isPresent) Rose500 else Color.Transparent).clickable { attendanceMap = attendanceMap.toMutableMap().apply { this[student.id] = false } }.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                Text("A", color = if (!isPresent) Color.White else TextGray, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Button(
                onClick = { onSave(attendanceMap) },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Indigo600),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Attendance Record", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------- 3. STUDENTS TAB (With Search, Edit & Validation) ----------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentsTab(students: List<Student>, onAdd: (String, String, String) -> Unit, onUpdate: (Student) -> Unit, onDelete: (Student) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<Student?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    val filteredStudents = students.filter {
        it.name.contains(searchQuery, ignoreCase = true) || it.rollNumber.contains(searchQuery, ignoreCase = true)
    }

    Scaffold(
        containerColor = Slate950,
        floatingActionButton = {
            FloatingActionButton(onClick = { editingStudent = null; showDialog = true }, containerColor = Indigo600, contentColor = Color.White) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Student Directory", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or roll no...", color = TextGray) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextGray) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Indigo600,
                    unfocusedBorderColor = Slate800,
                    focusedContainerColor = Slate900,
                    unfocusedContainerColor = Slate900
                ),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (filteredStudents.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (searchQuery.isNotEmpty()) "No students found." else "No Students Added Yet", color = TextGray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filteredStudents) { student ->
                        Card(colors = CardDefaults.cardColors(containerColor = Slate800), shape = RoundedCornerShape(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(student.name, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Roll: ${student.rollNumber} • Class: ${student.className}", color = TextGray, fontSize = 12.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(onClick = { editingStudent = student; showDialog = true }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Emerald500)
                                    }
                                    IconButton(onClick = { onDelete(student) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Rose500)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add / Edit Dialog with Form Validation
        if (showDialog) {
            var name by remember { mutableStateOf(editingStudent?.name ?: "") }
            var roll by remember { mutableStateOf(editingStudent?.rollNumber ?: "") }
            var cls by remember { mutableStateOf(editingStudent?.className ?: "") }
            var errorMessage by remember { mutableStateOf("") }
            
            AlertDialog(
                onDismissRequest = { showDialog = false; errorMessage = "" },
                containerColor = Slate900,
                title = { Text(if (editingStudent == null) "Add New Student" else "Edit Student", color = Color.White, fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (errorMessage.isNotEmpty()) {
                            Text(errorMessage, color = Rose500, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }
                        OutlinedTextField(
                            value = name, 
                            onValueChange = { name = it; errorMessage = "" }, 
                            label = { Text("Full Name", color = TextGray) }, 
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = roll, 
                            onValueChange = { roll = it; errorMessage = "" }, 
                            label = { Text("Roll Number", color = TextGray) }, 
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = cls, 
                            onValueChange = { cls = it; errorMessage = "" }, 
                            label = { Text("Class / Grade", color = TextGray) }, 
                            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { 
                            if (name.isBlank() || roll.isBlank() || cls.isBlank()) {
                                errorMessage = "All fields are required!"
                            } else {
                                if (editingStudent != null) {
                                    onUpdate(editingStudent!!.copy(name = name.trim(), rollNumber = roll.trim(), className = cls.trim()))
                                } else {
                                    onAdd(name.trim(), roll.trim(), cls.trim())
                                }
                                showDialog = false
                            }
                        }, 
                        colors = ButtonDefaults.buttonColors(containerColor = Indigo600)
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false; errorMessage = "" }) { Text("Cancel", color = TextGray) }
                }
            )
        }
    }
}

// ---------------- 4. STATS TAB ----------------
@Composable
fun StatsTab(students: List<Student>, dashState: com.smartattendance.app.ui.viewmodel.DashboardUiState) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Attendance Stats", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            DashboardCard(title = "Total Recorded Days", value = dashState.totalRecordedDays.toString(), icon = Icons.Default.CalendarMonth, color = Indigo600, modifier = Modifier.weight(1f))
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(students) { student ->
                val percentage = dashState.todayPercentage 
                
                Card(colors = CardDefaults.cardColors(containerColor = Slate800), shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(student.name, color = Color.White, fontWeight = FontWeight.Bold)
                            Text("${"%.0f".format(percentage)}%", color = if (percentage > 75) Emerald500 else Rose500, fontWeight = FontWeight.Bold)
                        }
                        LinearProgressIndicator(
                            progress = percentage / 100f,
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = if (percentage > 75) Emerald500 else Rose500,
                            trackColor = Slate950
                        )
                    }
                }
            }
        }
    }
}
