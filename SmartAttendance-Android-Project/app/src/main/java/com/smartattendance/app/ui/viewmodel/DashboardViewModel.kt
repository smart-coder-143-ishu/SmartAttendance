package com.smartattendance.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smartattendance.app.data.repository.AttendanceRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch // <-- Yeh import miss tha
import java.time.LocalDate

data class DashboardUiState(
    val totalStudents: Int = 0,
    val todayRecorded: Boolean = false,
    val todayPresentCount: Int = 0,
    val todayAbsentCount: Int = 0,
    val todayPercentage: Float = 0f,
    val overallAveragePercentage: Float = 0f,
    val totalRecordedDays: Int = 0,
    val lowAttendanceAlerts: Int = 0,
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val repository: AttendanceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardStats()
    }

    fun loadDashboardStats() {
        val today = LocalDate.now().toString()
        viewModelScope.launch {
            combine(
                repository.allStudents,
                repository.getRecordsForDate(today),
                repository.allRecordedDates
            ) { students, todayRecords, dates ->
                val totalStudents = students.size
                val todayRecorded = todayRecords.isNotEmpty()
                val todayPresent = todayRecords.count { it.isPresent }
                val todayAbsent = todayRecords.count { !it.isPresent }
                val todayPct = if (todayRecords.isNotEmpty()) {
                    (todayPresent.toFloat() / todayRecords.size) * 100f
                } else 0f

                DashboardUiState(
                    totalStudents = totalStudents,
                    todayRecorded = todayRecorded,
                    todayPresentCount = todayPresent,
                    todayAbsentCount = todayAbsent,
                    todayPercentage = todayPct,
                    totalRecordedDays = dates.size,
                    isLoading = false
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }
}
