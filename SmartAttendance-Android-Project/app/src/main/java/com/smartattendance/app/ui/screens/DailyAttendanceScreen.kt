package com.smartattendance.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smartattendance.app.data.model.Student

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyAttendanceScreen(
    students: List<Student>,
    selectedDate: String,
    onDateChange: (String) -> Unit,
    onSaveAttendance: (Map<Long, Boolean>) -> Unit,
    initialStatus: Map<Long, Boolean> = emptyMap()
) {
    var attendanceMap by remember(selectedDate, initialStatus) {
        mutableStateOf(initialStatus.ifEmpty {
            // Default: All students present
            students.associate { it.id to true }
        })
    }

    val presentCount = attendanceMap.values.count { it }
    val absentCount = attendanceMap.size - presentCount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Attendance") },
                actions = {
                    TextButton(onClick = {
                        // Mark all present feature
                        attendanceMap = students.associate { it.id to true }
                    }) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark All Present")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onSaveAttendance(attendanceMap) },
                icon = { Icon(Icons.Default.Save, contentDescription = null) },
                text = { Text("Save Attendance") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Date Selector Card
            OutlinedCard(
                modifier = Modifier.fillMaxWidth(),
                onClick = { /* Open Material 3 DatePickerDialog */ }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("Attendance Date", style = MaterialTheme.typography.labelMedium)
                        Text(selectedDate, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Present: $presentCount",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge
                )
                Text(
                    "Absent: $absentCount",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Student List with Present / Absent Toggle
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students, key = { it.id }) { student ->
                    val isPresent = attendanceMap[student.id] ?: true
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isPresent)
                                MaterialTheme.colorScheme.surfaceVariant
                            else
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(student.name, style = MaterialTheme.typography.titleMedium)
                                Text("Roll: ${student.rollNumber} • ${student.className}", style = MaterialTheme.typography.bodySmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                FilterChip(
                                    selected = isPresent,
                                    onClick = { attendanceMap = attendanceMap + (student.id to true) },
                                    label = { Text("P") }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                FilterChip(
                                    selected = !isPresent,
                                    onClick = { attendanceMap = attendanceMap + (student.id to false) },
                                    label = { Text("A") }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
