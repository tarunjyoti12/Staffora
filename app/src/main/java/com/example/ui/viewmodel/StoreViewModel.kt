package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.AdvanceEntity
import com.example.data.model.AdvanceStatus
import com.example.data.model.AttendanceEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.HolidayEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.LeaveStatus
import com.example.data.model.OvertimeEntity
import com.example.data.model.OvertimeStatus
import com.example.data.model.PayrollEntity
import com.example.data.model.PayrollStatus
import com.example.data.model.TaskEntity
import com.example.data.model.TaskStatus
import com.example.data.model.UserRole
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DashboardSummary(
    val totalEmployees: Int = 0,
    val presentToday: Int = 0,
    val absentToday: Int = 0,
    val onLeaveToday: Int = 0,
    val halfDayToday: Int = 0,
    val lateToday: Int = 0,
    val attendanceMissing: Int = 0,
    val pendingLeavesCount: Int = 0,
    val pendingPayrollCount: Int = 0,
    val pendingAdvancesCount: Int = 0,
    val expiringDocsCount: Int = 0,
    val overdueTasksCount: Int = 0
)

data class AskResult(
    val question: String,
    val answer: String,
    val timestamp: Long = System.currentTimeMillis()
)

class StoreViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = StoreRepository(db)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    val todayDateStr: String = dateFormat.format(Date())
    val currentMonthStr: String = monthFormat.format(Date())

    // Current selected date for Attendance screen
    private val _selectedAttendanceDate = MutableStateFlow(todayDateStr)
    val selectedAttendanceDate = _selectedAttendanceDate.asStateFlow()

    // Current selected month for Payroll screen
    private val _selectedPayrollMonth = MutableStateFlow(currentMonthStr)
    val selectedPayrollMonth = _selectedPayrollMonth.asStateFlow()

    // Active User Role
    private val _currentUserRole = MutableStateFlow(UserRole.OWNER)
    val currentUserRole = _currentUserRole.asStateFlow()

    // Active Employee ID (when role is EMPLOYEE)
    private val _currentEmployeeId = MutableStateFlow<Long?>(null)
    val currentEmployeeId = _currentEmployeeId.asStateFlow()

    // Global Search Query
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    // "Ask Business" Conversation History
    private val _askHistory = MutableStateFlow<List<AskResult>>(emptyList())
    val askHistory = _askHistory.asStateFlow()

    // Toast / Message banner
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage = _userMessage.asStateFlow()

    init {
        viewModelScope.launch {
            // Requirement: Completely remove all dummy, sample, test, placeholder data.
            // Do not automatically seed or recreate dummy data on startup.
            repository.clearSampleData()
        }
    }

    val allEmployees: StateFlow<List<EmployeeEntity>> = repository.allEmployees
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val attendanceForSelectedDate: StateFlow<List<AttendanceEntity>> = _selectedAttendanceDate
        .flatMapLatest { date -> repository.getAttendanceForDate(date) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val attendanceToday: StateFlow<List<AttendanceEntity>> = repository.getAttendanceForDate(todayDateStr)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLeaves: StateFlow<List<LeaveEntity>> = repository.allLeaves
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allHolidays: StateFlow<List<HolidayEntity>> = repository.allHolidays
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAdvances: StateFlow<List<AdvanceEntity>> = repository.allAdvances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOvertime: StateFlow<List<OvertimeEntity>> = repository.allOvertime
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val payrollForSelectedMonth: StateFlow<List<PayrollEntity>> = _selectedPayrollMonth
        .flatMapLatest { month -> repository.getPayrollForMonth(month) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTasks: StateFlow<List<TaskEntity>> = repository.allTasks
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDocuments: StateFlow<List<DocumentEntity>> = repository.allDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentAuditLogs: StateFlow<List<AuditLogEntity>> = repository.recentAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Combined Dashboard Summary calculation
    val dashboardSummary: StateFlow<DashboardSummary> = combine(
        combine(allEmployees, attendanceToday, allLeaves) { emps, atts, leaves ->
            Triple(emps, atts, leaves)
        },
        combine(allAdvances, allDocuments, allTasks) { advances, docs, tasks ->
            Triple(advances, docs, tasks)
        }
    ) { (emps, atts, leaves), (advances, docs, tasks) ->
        val activeEmps = emps.filter { it.isActive }
        val attMap = atts.associateBy { it.employeeId }

        val present = atts.count { it.status == AttendanceStatus.PRESENT.name }
        val absent = atts.count { it.status == AttendanceStatus.ABSENT.name }
        val onLeave = atts.count { it.status == AttendanceStatus.LEAVE.name }
        val halfDay = atts.count { it.status == AttendanceStatus.HALF_DAY.name }
        val late = atts.count { it.status == AttendanceStatus.LATE.name }

        // CRITICAL: Attendance Missing is count of active employees who have NO attendance record entered today!
        val missing = activeEmps.count { emp -> attMap[emp.id] == null }

        val pendingLeaves = leaves.count { it.status == LeaveStatus.PENDING.name }
        val pendingAdvances = advances.count { it.status == AdvanceStatus.ACTIVE.name && (it.amount - it.recoveredAmount) > 0 }

        val nowCal = Calendar.getInstance()
        val in30DaysCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }
        val expiringDocs = docs.count { doc ->
            if (doc.expiryDate.isBlank()) false
            else {
                try {
                    val expDate = dateFormat.parse(doc.expiryDate)
                    expDate != null && expDate.before(in30DaysCal.time)
                } catch (e: Exception) {
                    false
                }
            }
        }

        val overdueTasks = tasks.count { task ->
            if (task.status == TaskStatus.COMPLETED.name || task.dueDate.isBlank()) false
            else {
                try {
                    val dueDate = dateFormat.parse(task.dueDate)
                    dueDate != null && dueDate.before(nowCal.time)
                } catch (e: Exception) {
                    false
                }
            }
        }

        DashboardSummary(
            totalEmployees = activeEmps.size,
            presentToday = present,
            absentToday = absent,
            onLeaveToday = onLeave,
            halfDayToday = halfDay,
            lateToday = late,
            attendanceMissing = missing,
            pendingLeavesCount = pendingLeaves,
            pendingAdvancesCount = pendingAdvances,
            expiringDocsCount = expiringDocs,
            overdueTasksCount = overdueTasks
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardSummary())

    fun setSelectedAttendanceDate(date: String) {
        _selectedAttendanceDate.value = date
    }

    fun setSelectedPayrollMonth(month: String) {
        _selectedPayrollMonth.value = month
    }

    fun setCurrentUserRole(role: UserRole, empId: Long? = null) {
        _currentUserRole.value = role
        _currentEmployeeId.value = empId
        showMessage("Switched role to ${role.displayName}")
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearUserMessage() {
        _userMessage.value = null
    }

    // Attendance actions
    fun markAttendance(
        employeeId: Long,
        employeeName: String,
        status: AttendanceStatus,
        inTime: String = "",
        outTime: String = "",
        lateMinutes: Int = 0,
        overtimeHours: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            repository.markAttendance(
                employeeId = employeeId,
                employeeName = employeeName,
                date = _selectedAttendanceDate.value,
                status = status,
                inTime = inTime,
                outTime = outTime,
                lateMinutes = lateMinutes,
                overtimeHours = overtimeHours,
                notes = notes,
                performedBy = _currentUserRole.value
            )
            showMessage("Marked $employeeName as ${status.displayName}")
        }
    }

    fun markAllUnmarkedPresent() {
        viewModelScope.launch {
            val date = _selectedAttendanceDate.value
            val emps = allEmployees.value.filter { it.isActive }
            val existing = attendanceForSelectedDate.value
            repository.markAllUnmarkedPresent(date, emps, existing, _currentUserRole.value)
            showMessage("Marked remaining active staff as Present")
        }
    }

    // Employee actions
    fun saveEmployee(employee: EmployeeEntity, isEdit: Boolean) {
        viewModelScope.launch {
            if (isEdit) {
                repository.updateEmployee(employee, _currentUserRole.value)
                showMessage("Updated employee ${employee.fullName}")
            } else {
                repository.addEmployee(employee, _currentUserRole.value)
                showMessage("Added new employee ${employee.fullName}")
            }
        }
    }

    fun toggleEmployeeActive(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.toggleEmployeeActive(employee, _currentUserRole.value)
            val state = if (!employee.isActive) "activated" else "deactivated"
            showMessage("Employee ${employee.fullName} $state")
        }
    }

    fun setEmployeeActive(employee: EmployeeEntity, isActive: Boolean) {
        viewModelScope.launch {
            repository.setEmployeeActiveStatus(employee, isActive, _currentUserRole.value)
            val state = if (isActive) "reactivated" else "deactivated"
            showMessage("Employee ${employee.fullName} $state (all historical records preserved)")
        }
    }

    fun deleteEmployeePermanently(employee: EmployeeEntity) {
        viewModelScope.launch {
            repository.deleteEmployeePermanently(employee, _currentUserRole.value)
            showMessage("Employee ${employee.fullName} permanently deleted")
        }
    }

    // Leave actions
    fun requestLeave(leave: LeaveEntity, employeeName: String) {
        viewModelScope.launch {
            repository.addLeave(leave, employeeName, _currentUserRole.value)
            showMessage("Leave request submitted for $employeeName")
        }
    }

    fun updateLeaveStatus(leave: LeaveEntity, newStatus: LeaveStatus, remarks: String, employeeName: String) {
        viewModelScope.launch {
            repository.updateLeaveStatus(leave, newStatus, remarks, employeeName, _currentUserRole.value)
            showMessage("Leave ${newStatus.name.lowercase()} for $employeeName")
        }
    }

    fun deleteLeave(leave: LeaveEntity) {
        viewModelScope.launch {
            repository.deleteLeave(leave)
            showMessage("Leave record deleted")
        }
    }

    // Holiday actions
    fun addHoliday(name: String, date: String, description: String) {
        viewModelScope.launch {
            repository.addHoliday(HolidayEntity(name = name, date = date, description = description), _currentUserRole.value)
            showMessage("Holiday $name added")
        }
    }

    fun deleteHoliday(holiday: HolidayEntity) {
        viewModelScope.launch {
            repository.deleteHoliday(holiday)
            showMessage("Holiday removed")
        }
    }

    // Advance actions
    fun recordAdvance(advance: AdvanceEntity, employeeName: String) {
        viewModelScope.launch {
            repository.recordAdvance(advance, employeeName, _currentUserRole.value)
            showMessage("Advance of ₹${advance.amount} recorded for $employeeName")
        }
    }

    fun recordAdvanceRepayment(advance: AdvanceEntity, repaymentAmount: Double, employeeName: String) {
        viewModelScope.launch {
            repository.recordAdvanceRepayment(advance, repaymentAmount, employeeName, _currentUserRole.value)
            showMessage("Repayment of ₹$repaymentAmount recorded for $employeeName")
        }
    }

    // Overtime actions
    fun recordOvertime(overtime: OvertimeEntity, employeeName: String) {
        viewModelScope.launch {
            repository.recordOvertime(overtime, employeeName, _currentUserRole.value)
            showMessage("Overtime of ${overtime.totalHours} hrs recorded for $employeeName")
        }
    }

    fun updateOvertimeStatus(overtime: OvertimeEntity, status: OvertimeStatus, employeeName: String) {
        viewModelScope.launch {
            repository.updateOvertimeStatus(overtime, status, employeeName, _currentUserRole.value)
            showMessage("Overtime ${status.name.lowercase()} for $employeeName")
        }
    }

    // Payroll actions
    fun savePayroll(payroll: PayrollEntity, employeeName: String) {
        viewModelScope.launch {
            repository.savePayroll(payroll, employeeName, _currentUserRole.value)
            showMessage("Salary updated for $employeeName")
        }
    }

    // Task actions
    fun addTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.addTask(task)
            showMessage("Task '${task.title}' added")
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            showMessage("Task deleted")
        }
    }

    // Document actions
    fun addDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.addDocument(doc)
            showMessage("Document added for employee")
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            showMessage("Document removed")
        }
    }

    // Sample data & reset
    fun clearSampleData() {
        viewModelScope.launch {
            repository.clearSampleData()
            showMessage("Sample employees cleared")
        }
    }

    fun resetStoreData() {
        viewModelScope.launch {
            repository.clearAllData()
            repository.seedSampleDataIfNeeded()
            showMessage("Store data reloaded with fresh sample records")
        }
    }

    // "Ask Your Business" Query processor
    fun askBusiness(question: String) {
        val q = question.trim().lowercase(Locale.getDefault())
        val emps = allEmployees.value.filter { it.isActive }
        val atts = attendanceToday.value
        val attMap = atts.associateBy { it.employeeId }
        val empMap = emps.associateBy { it.id }
        val advances = allAdvances.value
        val leaves = allLeaves.value
        val payrolls = payrollForSelectedMonth.value
        val otList = allOvertime.value
        val docs = allDocuments.value
        val tasks = allTasks.value

        val answer = when {
            q.contains("who is absent") || q.contains("absent today") -> {
                val absentList = atts.filter { it.status == AttendanceStatus.ABSENT.name }
                if (absentList.isEmpty()) {
                    "No employees are marked absent today."
                } else {
                    val names = absentList.mapNotNull { empMap[it.employeeId]?.fullName }.joinToString(", ")
                    "${absentList.size} employee(s) marked absent today: $names."
                }
            }

            q.contains("missing") || q.contains("attendance missing") || q.contains("not entered") -> {
                val missingEmps = emps.filter { attMap[it.id] == null }
                if (missingEmps.isEmpty()) {
                    "All active employees have attendance entered for today! None missing."
                } else {
                    val details = missingEmps.joinToString("; ") { "${it.fullName} (${it.empCode} - ${it.department})" }
                    "${missingEmps.size} employee(s) have attendance missing today:\n$details.\nRemember: Never mark absent automatically; use the Mark Attendance screen to record actual status."
                }
            }

            q.contains("present today") || q.contains("who is present") -> {
                val presentList = atts.filter { it.status == AttendanceStatus.PRESENT.name || it.status == AttendanceStatus.LATE.name }
                if (presentList.isEmpty()) {
                    "No employees have been marked Present yet today."
                } else {
                    val names = presentList.mapNotNull { empMap[it.employeeId]?.fullName }.joinToString(", ")
                    "${presentList.size} employee(s) are present today: $names."
                }
            }

            q.contains("leave") || q.contains("who is on leave") -> {
                val onLeaveToday = atts.filter { it.status == AttendanceStatus.LEAVE.name }
                val pendingLeaves = leaves.filter { it.status == LeaveStatus.PENDING.name }
                val sb = java.lang.StringBuilder()
                if (onLeaveToday.isEmpty()) {
                    sb.append("No employee is on leave today. ")
                } else {
                    val names = onLeaveToday.mapNotNull { empMap[it.employeeId]?.fullName }.joinToString(", ")
                    sb.append("On leave today: $names. ")
                }
                if (pendingLeaves.isNotEmpty()) {
                    sb.append("\nAlso, there are ${pendingLeaves.size} pending leave request(s) awaiting your approval.")
                }
                sb.toString()
            }

            q.contains("salary") || q.contains("pending salary") || q.contains("payroll") -> {
                var pendingTotal = 0.0
                var pendingCount = 0
                for (emp in emps) {
                    val p = payrolls.firstOrNull { it.employeeId == emp.id }
                    if (p == null || p.status == PayrollStatus.PENDING.name) {
                        val base = emp.monthlySalary
                        pendingTotal += p?.netSalary ?: base
                        pendingCount++
                    }
                }
                "For month ${selectedPayrollMonth.value}, $pendingCount employee(s) have salary pending, totaling approximately ₹${String.format(Locale.getDefault(), "%,.0f", pendingTotal)}."
            }

            q.contains("advance") || q.contains("outstanding advance") || q.contains("loan") -> {
                val activeAdvances = advances.filter { it.status == AdvanceStatus.ACTIVE.name && (it.amount - it.recoveredAmount) > 0 }
                if (activeAdvances.isEmpty()) {
                    "There are no outstanding employee advances at this time."
                } else {
                    val totalBalance = activeAdvances.sumOf { it.amount - it.recoveredAmount }
                    val details = activeAdvances.joinToString("\n") {
                        val empName = empMap[it.employeeId]?.fullName ?: "Employee #${it.employeeId}"
                        "• $empName: ₹${it.amount - it.recoveredAmount} remaining (taken for ${it.reason})"
                    }
                    "Total outstanding advances: ₹${String.format(Locale.getDefault(), "%,.0f", totalBalance)} across ${activeAdvances.size} advance(s):\n$details"
                }
            }

            q.contains("overtime") -> {
                val approvedOT = otList.filter { it.status == OvertimeStatus.APPROVED.name }
                val totalHours = approvedOT.sumOf { it.totalHours }
                val totalAmount = approvedOT.sumOf { it.totalAmount }
                val pendingOT = otList.count { it.status == OvertimeStatus.PENDING.name }
                "Recorded approved overtime: ${totalHours} hours (₹${String.format(Locale.getDefault(), "%,.0f", totalAmount)}).\nThere are $pendingOT pending overtime approval request(s)."
            }

            q.contains("task") || q.contains("overdue") -> {
                val pendingTasks = tasks.filter { it.status != TaskStatus.COMPLETED.name }
                if (pendingTasks.isEmpty()) {
                    "All store tasks are completed! No pending tasks."
                } else {
                    val details = pendingTasks.joinToString("\n") {
                        val empName = empMap[it.employeeId]?.fullName ?: "Unassigned"
                        "• ${it.title} (${it.priority}) - Assigned to $empName"
                    }
                    "${pendingTasks.size} pending task(s):\n$details"
                }
            }

            q.contains("document") || q.contains("expir") -> {
                val now = Calendar.getInstance().time
                val in30Days = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 30) }.time
                val expiring = docs.filter { doc ->
                    if (doc.expiryDate.isBlank()) false
                    else {
                        try {
                            val exp = dateFormat.parse(doc.expiryDate)
                            exp != null && exp.before(in30Days)
                        } catch (e: Exception) { false }
                    }
                }
                if (expiring.isEmpty()) {
                    "All staff identity and employment documents are currently valid."
                } else {
                    val details = expiring.joinToString("\n") {
                        val empName = empMap[it.employeeId]?.fullName ?: "Staff #${it.employeeId}"
                        "• $empName: ${it.documentName} expires on ${it.expiryDate}"
                    }
                    "⚠ ${expiring.size} document(s) expiring within 30 days:\n$details"
                }
            }

            q.contains("birthday") -> {
                val cal = Calendar.getInstance()
                val currentMonth = cal.get(Calendar.MONTH) + 1
                val bdays = emps.filter { emp ->
                    if (emp.dob.isBlank()) false
                    else {
                        try {
                            val parts = emp.dob.split("-")
                            if (parts.size >= 2) parts[1].toInt() == currentMonth else false
                        } catch (e: Exception) { false }
                    }
                }
                if (bdays.isEmpty()) {
                    "No employee birthdays this month."
                } else {
                    val names = bdays.joinToString(", ") { "${it.fullName} (${it.dob})" }
                    "🎂 Birthdays this month: $names."
                }
            }

            else -> {
                // Fallback smart overview
                val activeCount = emps.size
                val missingCount = emps.count { attMap[it.id] == null }
                val presentCount = atts.count { it.status == AttendanceStatus.PRESENT.name }
                "At Vijay General Store right now:\n• Total active employees: $activeCount\n• Present today: $presentCount\n• Attendance missing: $missingCount\n\nYou can ask: 'Who is absent today?', 'Who has attendance missing?', 'How much salary is pending?', 'Who has an outstanding advance?', or 'Which documents are expiring?'."
            }
        }

        val result = AskResult(question = question, answer = answer)
        _askHistory.value = listOf(result) + _askHistory.value
    }
}
