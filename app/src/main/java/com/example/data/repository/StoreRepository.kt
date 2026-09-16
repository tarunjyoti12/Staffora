package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.model.AdvanceEntity
import com.example.data.model.AdvanceStatus
import com.example.data.model.AttendanceEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.AuditLogEntity
import com.example.data.model.Department
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentType
import com.example.data.model.EmployeeEntity
import com.example.data.model.EmploymentType
import com.example.data.model.HolidayEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.data.model.OvertimeEntity
import com.example.data.model.OvertimeStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.PayrollEntity
import com.example.data.model.PayrollStatus
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.data.model.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoreRepository(private val db: AppDatabase) {

    private val employeeDao = db.employeeDao()
    private val attendanceDao = db.attendanceDao()
    private val leaveDao = db.leaveDao()
    private val holidayDao = db.holidayDao()
    private val advanceDao = db.advanceDao()
    private val overtimeDao = db.overtimeDao()
    private val payrollDao = db.payrollDao()
    private val taskDao = db.taskDao()
    private val documentDao = db.documentDao()
    private val auditLogDao = db.auditLogDao()

    val allEmployees: Flow<List<EmployeeEntity>> = employeeDao.getAllEmployees()
    val allLeaves: Flow<List<LeaveEntity>> = leaveDao.getAllLeaves()
    val allHolidays: Flow<List<HolidayEntity>> = holidayDao.getAllHolidays()
    val allAdvances: Flow<List<AdvanceEntity>> = advanceDao.getAllAdvances()
    val allOvertime: Flow<List<OvertimeEntity>> = overtimeDao.getAllOvertime()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()
    val recentAuditLogs: Flow<List<AuditLogEntity>> = auditLogDao.getRecentAuditLogs()

    fun getAttendanceForDate(date: String): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceByDate(date)

    fun getAttendanceForEmployee(empId: Long): Flow<List<AttendanceEntity>> =
        attendanceDao.getAttendanceForEmployee(empId)

    fun getLeavesForEmployee(empId: Long): Flow<List<LeaveEntity>> =
        leaveDao.getLeavesForEmployee(empId)

    fun getAdvancesForEmployee(empId: Long): Flow<List<AdvanceEntity>> =
        advanceDao.getAdvancesForEmployee(empId)

    fun getOvertimeForEmployee(empId: Long): Flow<List<OvertimeEntity>> =
        overtimeDao.getOvertimeForEmployee(empId)

    fun getTasksForEmployee(empId: Long): Flow<List<TaskEntity>> =
        taskDao.getTasksForEmployee(empId)

    fun getDocumentsForEmployee(empId: Long): Flow<List<DocumentEntity>> =
        documentDao.getDocumentsForEmployee(empId)

    fun getPayrollForMonth(monthYear: String): Flow<List<PayrollEntity>> =
        payrollDao.getPayrollForMonth(monthYear)

    fun getPayrollForEmployee(empId: Long): Flow<List<PayrollEntity>> =
        payrollDao.getPayrollForEmployee(empId)

    suspend fun addEmployee(employee: EmployeeEntity, performedBy: UserRole = UserRole.OWNER): Long {
        val id = employeeDao.insertEmployee(employee)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "EMPLOYEE_CREATED",
                affectedRecord = "${employee.empCode} - ${employee.fullName}",
                details = "Added new employee in ${employee.department}, salary ₹${employee.monthlySalary}"
            )
        )
        return id
    }

    suspend fun updateEmployee(employee: EmployeeEntity, performedBy: UserRole = UserRole.OWNER) {
        employeeDao.updateEmployee(employee)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "EMPLOYEE_UPDATED",
                affectedRecord = "${employee.empCode} - ${employee.fullName}",
                details = "Updated profile details, status active=${employee.isActive}"
            )
        )
    }

    suspend fun toggleEmployeeActive(employee: EmployeeEntity, performedBy: UserRole = UserRole.OWNER) {
        val updated = employee.copy(isActive = !employee.isActive)
        employeeDao.updateEmployee(updated)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = if (updated.isActive) "EMPLOYEE_ACTIVATED" else "EMPLOYEE_DEACTIVATED",
                affectedRecord = "${employee.empCode} - ${employee.fullName}",
                details = "Status changed to ${if (updated.isActive) "Active" else "Inactive"}"
            )
        )
    }

    suspend fun setEmployeeActiveStatus(employee: EmployeeEntity, isActive: Boolean, performedBy: UserRole = UserRole.OWNER) {
        val updated = employee.copy(isActive = isActive)
        employeeDao.updateEmployee(updated)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = if (isActive) "EMPLOYEE_ACTIVATED" else "EMPLOYEE_DEACTIVATED",
                affectedRecord = "${employee.empCode} - ${employee.fullName}",
                details = "Employee ${employee.fullName} marked as ${if (isActive) "Active" else "Deactivated/Inactive"}. Historical records preserved."
            )
        )
    }

    suspend fun deleteEmployeePermanently(employee: EmployeeEntity, performedBy: UserRole = UserRole.OWNER) {
        // Safely cascade deletion of all child records to prevent broken references or orphaned rows
        attendanceDao.deleteForEmployee(employee.id)
        leaveDao.deleteForEmployee(employee.id)
        advanceDao.deleteForEmployee(employee.id)
        overtimeDao.deleteForEmployee(employee.id)
        payrollDao.deleteForEmployee(employee.id)
        taskDao.deleteForEmployee(employee.id)
        documentDao.deleteForEmployee(employee.id)
        employeeDao.deleteEmployee(employee)

        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "EMPLOYEE_DELETED",
                affectedRecord = "${employee.empCode} - ${employee.fullName}",
                details = "Permanently deleted employee ${employee.fullName} and safely cleaned up related records."
            )
        )
    }

    suspend fun markAttendance(
        employeeId: Long,
        employeeName: String,
        date: String,
        status: AttendanceStatus,
        inTime: String = "",
        outTime: String = "",
        lateMinutes: Int = 0,
        overtimeHours: Double = 0.0,
        notes: String = "",
        performedBy: UserRole = UserRole.OWNER
    ) {
        val existing = attendanceDao.getAttendanceRecord(employeeId, date)
        val entity = existing?.copy(
            status = status.name,
            inTime = inTime.ifBlank { existing.inTime },
            outTime = outTime.ifBlank { existing.outTime },
            lateMinutes = lateMinutes,
            overtimeHours = overtimeHours,
            notes = notes,
            updatedAt = System.currentTimeMillis()
        ) ?: AttendanceEntity(
            employeeId = employeeId,
            date = date,
            status = status.name,
            inTime = inTime,
            outTime = outTime,
            lateMinutes = lateMinutes,
            overtimeHours = overtimeHours,
            notes = notes
        )
        attendanceDao.insertOrUpdateAttendance(entity)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "ATTENDANCE_MARKED",
                affectedRecord = "$employeeName on $date",
                details = "Marked as ${status.displayName}, in: $inTime, out: $outTime"
            )
        )
    }

    suspend fun markAllUnmarkedPresent(
        date: String,
        activeEmployees: List<EmployeeEntity>,
        existingRecords: List<AttendanceEntity>,
        performedBy: UserRole = UserRole.OWNER
    ) {
        val markedEmpIds = existingRecords.map { it.employeeId }.toSet()
        val toInsert = mutableListOf<AttendanceEntity>()
        for (emp in activeEmployees) {
            if (!markedEmpIds.contains(emp.id)) {
                toInsert.add(
                    AttendanceEntity(
                        employeeId = emp.id,
                        date = date,
                        status = AttendanceStatus.PRESENT.name,
                        inTime = "09:00 AM",
                        outTime = "08:30 PM",
                        lateMinutes = 0,
                        overtimeHours = 0.0,
                        notes = "Bulk marked Present"
                    )
                )
            }
        }
        if (toInsert.isNotEmpty()) {
            attendanceDao.insertAll(toInsert)
            auditLogDao.insertLog(
                AuditLogEntity(
                    userRole = performedBy.name,
                    action = "BULK_ATTENDANCE_MARKED",
                    affectedRecord = "$date (${toInsert.size} employees)",
                    details = "Marked remaining active staff Present"
                )
            )
        }
    }

    suspend fun addLeave(leave: LeaveEntity, employeeName: String, performedBy: UserRole = UserRole.OWNER) {
        leaveDao.insertLeave(leave)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "LEAVE_REQUESTED",
                affectedRecord = "$employeeName (${leave.leaveType})",
                details = "From ${leave.startDate} to ${leave.endDate} (${leave.numberOfDays} days)"
            )
        )
    }

    suspend fun updateLeaveStatus(
        leave: LeaveEntity,
        newStatus: LeaveStatus,
        remarks: String,
        employeeName: String,
        performedBy: UserRole = UserRole.OWNER
    ) {
        val updated = leave.copy(status = newStatus.name, remarks = remarks)
        leaveDao.updateLeave(updated)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "LEAVE_${newStatus.name}",
                affectedRecord = "$employeeName (${leave.leaveType})",
                details = "Status changed to ${newStatus.name}. Remarks: $remarks"
            )
        )
    }

    suspend fun deleteLeave(leave: LeaveEntity) {
        leaveDao.deleteLeave(leave)
    }

    suspend fun addHoliday(holiday: HolidayEntity, performedBy: UserRole = UserRole.OWNER) {
        holidayDao.insertHoliday(holiday)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "HOLIDAY_ADDED",
                affectedRecord = "${holiday.name} on ${holiday.date}",
                details = holiday.description
            )
        )
    }

    suspend fun deleteHoliday(holiday: HolidayEntity) {
        holidayDao.deleteHoliday(holiday)
    }

    suspend fun recordAdvance(
        advance: AdvanceEntity,
        employeeName: String,
        performedBy: UserRole = UserRole.OWNER
    ) {
        advanceDao.insertAdvance(advance)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "ADVANCE_RECORDED",
                affectedRecord = "$employeeName - ₹${advance.amount}",
                details = "Reason: ${advance.reason}, Monthly deduction: ₹${advance.monthlyDeduction}"
            )
        )
    }

    suspend fun recordAdvanceRepayment(
        advance: AdvanceEntity,
        repaymentAmount: Double,
        employeeName: String,
        performedBy: UserRole = UserRole.OWNER
    ) {
        val newRecovered = advance.recoveredAmount + repaymentAmount
        val newStatus = if (newRecovered >= advance.amount) AdvanceStatus.REPAID.name else AdvanceStatus.ACTIVE.name
        val updated = advance.copy(recoveredAmount = newRecovered, status = newStatus)
        advanceDao.updateAdvance(updated)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "ADVANCE_REPAID",
                affectedRecord = "$employeeName - ₹$repaymentAmount",
                details = "Total recovered ₹$newRecovered / ₹${advance.amount}. Status: $newStatus"
            )
        )
    }

    suspend fun recordOvertime(
        overtime: OvertimeEntity,
        employeeName: String,
        performedBy: UserRole = UserRole.OWNER
    ) {
        overtimeDao.insertOvertime(overtime)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "OVERTIME_RECORDED",
                affectedRecord = "$employeeName on ${overtime.date}",
                details = "${overtime.totalHours} hrs @ ₹${overtime.hourlyRate}/hr = ₹${overtime.totalAmount}"
            )
        )
    }

    suspend fun updateOvertimeStatus(
        overtime: OvertimeEntity,
        status: OvertimeStatus,
        employeeName: String,
        performedBy: UserRole = UserRole.OWNER
    ) {
        val updated = overtime.copy(status = status.name)
        overtimeDao.updateOvertime(updated)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = "OVERTIME_${status.name}",
                affectedRecord = "$employeeName on ${overtime.date}",
                details = "${overtime.totalHours} hrs (₹${overtime.totalAmount}) status: ${status.name}"
            )
        )
    }

    suspend fun savePayroll(payroll: PayrollEntity, employeeName: String, performedBy: UserRole = UserRole.OWNER) {
        payrollDao.insertPayroll(payroll)
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = performedBy.name,
                action = if (payroll.status == PayrollStatus.PAID.name) "PAYROLL_PAID" else "PAYROLL_CALCULATED",
                affectedRecord = "$employeeName for ${payroll.monthYear}",
                details = "Net Salary: ₹${payroll.netSalary}, Status: ${payroll.status}"
            )
        )
    }

    suspend fun addTask(task: TaskEntity) {
        taskDao.insertTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun addDocument(document: DocumentEntity) {
        documentDao.insertDocument(document)
    }

    suspend fun deleteDocument(document: DocumentEntity) {
        documentDao.deleteDocument(document)
    }

    suspend fun seedSampleDataIfNeeded() {
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        // Check if employees exist
        val existing = employeeDao.getEmployeeByCode("EMP001")
        if (existing != null) return // Already seeded

        // Seed 4 realistic store employees
        val emp1 = EmployeeEntity(
            empCode = "EMP001",
            fullName = "Raj Kumar",
            phone = "+91 98765 43210",
            alternatePhone = "+91 98765 43211",
            dob = "1994-08-15",
            gender = "Male",
            address = "Shop Quarter #3, Main Market, Delhi",
            emergencyContact = "Sunita Kumar (Wife)",
            emergencyContactPhone = "+91 98765 43219",
            joiningDate = "2023-01-10",
            department = Department.SALES.name,
            designation = "Senior Sales Assistant",
            employmentType = EmploymentType.FULL_TIME.name,
            monthlySalary = 18500.0,
            paymentMethod = PaymentMethod.BANK_TRANSFER.name,
            bankAccount = "109823487612",
            ifsc = "SBIN0001234",
            salaryEffectiveDate = "2023-01-10",
            notes = "Handles FMCG, pulses, and grain counter. Very polite with customers.",
            isActive = true,
            isSample = true
        )

        val emp2 = EmployeeEntity(
            empCode = "EMP002",
            fullName = "Aman Sharma",
            phone = "+91 98111 22334",
            alternatePhone = "",
            dob = "1998-03-22",
            gender = "Male",
            address = "House 45, Gali 2, Shanti Nagar, Delhi",
            emergencyContact = "Ramesh Sharma (Father)",
            emergencyContactPhone = "+91 98111 22339",
            joiningDate = "2023-06-01",
            department = Department.BILLING.name,
            designation = "Cashier & Billing Executive",
            employmentType = EmploymentType.FULL_TIME.name,
            monthlySalary = 20000.0,
            paymentMethod = PaymentMethod.UPI.name,
            bankAccount = "348920194823",
            ifsc = "HDFC0004567",
            salaryEffectiveDate = "2023-06-01",
            notes = "Manages billing software and cash counter reconciliation.",
            isActive = true,
            isSample = true
        )

        val emp3 = EmployeeEntity(
            empCode = "EMP003",
            fullName = "Simran Kaur",
            phone = "+91 98222 33445",
            alternatePhone = "",
            dob = "1999-11-04",
            gender = "Female",
            address = "Flat 12B, Guru Nanak Enclave, Delhi",
            emergencyContact = "Harpreet Kaur (Mother)",
            emergencyContactPhone = "+91 98222 33449",
            joiningDate = "2023-09-15",
            department = Department.INVENTORY.name,
            designation = "Store & Inventory Assistant",
            employmentType = EmploymentType.FULL_TIME.name,
            monthlySalary = 16000.0,
            paymentMethod = PaymentMethod.CASH.name,
            bankAccount = "",
            ifsc = "",
            salaryEffectiveDate = "2023-09-15",
            notes = "Stock tracking, expiries check, packaging.",
            isActive = true,
            isSample = true
        )

        val emp4 = EmployeeEntity(
            empCode = "EMP004",
            fullName = "Manpreet Singh",
            phone = "+91 98333 44556",
            alternatePhone = "",
            dob = "1996-07-19",
            gender = "Male",
            address = "Sector 8, Rohini, Delhi",
            emergencyContact = "Gurpreet Singh (Brother)",
            emergencyContactPhone = "+91 98333 44559",
            joiningDate = "2024-02-01",
            department = Department.DELIVERY.name,
            designation = "Delivery & Logistics Assistant",
            employmentType = EmploymentType.FULL_TIME.name,
            monthlySalary = 15000.0,
            paymentMethod = PaymentMethod.CASH.name,
            bankAccount = "",
            ifsc = "",
            salaryEffectiveDate = "2024-02-01",
            notes = "Home delivery van and two-wheeler deliveries.",
            isActive = true,
            isSample = true
        )

        val id1 = employeeDao.insertEmployee(emp1)
        val id2 = employeeDao.insertEmployee(emp2)
        val id3 = employeeDao.insertEmployee(emp3)
        val id4 = employeeDao.insertEmployee(emp4)

        // Attendance for today:
        // Emp 1: Present (in 09:00 AM)
        // Emp 3: Leave
        // Emp 4: Present (in 09:15 AM, late 15m)
        // Emp 2: NOT ENTERED -> explicitly demonstrates ATTENDANCE MISSING rule!
        attendanceDao.insertOrUpdateAttendance(
            AttendanceEntity(
                employeeId = id1,
                date = today,
                status = AttendanceStatus.PRESENT.name,
                inTime = "09:00 AM",
                outTime = "08:30 PM",
                lateMinutes = 0,
                overtimeHours = 0.0,
                notes = "On time"
            )
        )
        attendanceDao.insertOrUpdateAttendance(
            AttendanceEntity(
                employeeId = id3,
                date = today,
                status = AttendanceStatus.LEAVE.name,
                inTime = "",
                outTime = "",
                lateMinutes = 0,
                overtimeHours = 0.0,
                notes = "Approved casual leave"
            )
        )
        attendanceDao.insertOrUpdateAttendance(
            AttendanceEntity(
                employeeId = id4,
                date = today,
                status = AttendanceStatus.LATE.name,
                inTime = "09:20 AM",
                outTime = "08:30 PM",
                lateMinutes = 20,
                overtimeHours = 1.0,
                notes = "Traffic delay on delivery vehicle"
            )
        )

        // Seed sample leave request (Pending)
        leaveDao.insertLeave(
            LeaveEntity(
                employeeId = id3,
                leaveType = LeaveType.CASUAL.name,
                startDate = today,
                endDate = today,
                numberOfDays = 1.0,
                reason = "Family religious function",
                status = LeaveStatus.APPROVED.name,
                appliedDate = today,
                remarks = "Approved by Vijay Ji"
            )
        )
        leaveDao.insertLeave(
            LeaveEntity(
                employeeId = id2,
                leaveType = LeaveType.SICK.name,
                startDate = today,
                endDate = today,
                numberOfDays = 1.0,
                reason = "Mild fever & headache",
                status = LeaveStatus.PENDING.name,
                appliedDate = today,
                remarks = ""
            )
        )

        // Seed sample holidays
        holidayDao.insertHoliday(
            HolidayEntity(
                name = "Diwali (Deepavali)",
                date = "2026-11-08",
                description = "Grand Festival of Lights - Store Closed"
            )
        )
        holidayDao.insertHoliday(
            HolidayEntity(
                name = "Gandhi Jayanti",
                date = "2026-10-02",
                description = "National Holiday"
            )
        )
        holidayDao.insertHoliday(
            HolidayEntity(
                name = "Vijay General Store Annual Puja",
                date = "2026-10-20",
                description = "Annual shop blessings and sweet distribution"
            )
        )

        // Seed sample advances
        advanceDao.insertAdvance(
            AdvanceEntity(
                employeeId = id2,
                amount = 5000.0,
                date = today,
                reason = "Urgent medical expense for mother",
                monthlyDeduction = 1500.0,
                recoveredAmount = 1500.0,
                status = AdvanceStatus.ACTIVE.name,
                notes = "Agreed to deduct ₹1,500 monthly from salary"
            )
        )
        advanceDao.insertAdvance(
            AdvanceEntity(
                employeeId = id4,
                amount = 3000.0,
                date = today,
                reason = "Vehicle tire repair and maintenance",
                monthlyDeduction = 1000.0,
                recoveredAmount = 0.0,
                status = AdvanceStatus.ACTIVE.name,
                notes = "₹3,000 pending"
            )
        )

        // Seed overtime
        overtimeDao.insertOvertime(
            OvertimeEntity(
                employeeId = id1,
                date = today,
                startTime = "08:30 PM",
                endTime = "10:30 PM",
                totalHours = 2.0,
                hourlyRate = 120.0,
                totalAmount = 240.0,
                status = OvertimeStatus.APPROVED.name,
                notes = "Festival stocking extra hours"
            )
        )

        // Seed sample tasks
        taskDao.insertTask(
            TaskEntity(
                title = "Restock Basmati Rice & Cooking Oil",
                employeeId = id1,
                description = "Unload 20 bags from godown to shop floor",
                priority = TaskPriority.HIGH.name,
                dueDate = today,
                status = TaskStatus.IN_PROGRESS.name
            )
        )
        taskDao.insertTask(
            TaskEntity(
                title = "Evening Cash Register Tally",
                employeeId = id2,
                description = "Verify cash drawer match with POS printout",
                priority = TaskPriority.URGENT.name,
                dueDate = today,
                status = TaskStatus.PENDING.name
            )
        )
        taskDao.insertTask(
            TaskEntity(
                title = "Inspect Expiry Dates on Spices Rack",
                employeeId = id3,
                description = "Tag items expiring before end of month",
                priority = TaskPriority.MEDIUM.name,
                dueDate = today,
                status = TaskStatus.PENDING.name
            )
        )

        // Seed sample documents
        documentDao.insertDocument(
            DocumentEntity(
                employeeId = id1,
                documentType = DocumentType.AADHAAR.name,
                documentName = "Aadhaar Card Copy",
                documentNumber = "XXXX-XXXX-4819",
                uploadDate = "2023-01-10",
                expiryDate = "2032-01-01",
                notes = "Verified with original"
            )
        )
        documentDao.insertDocument(
            DocumentEntity(
                employeeId = id2,
                documentType = DocumentType.PAN.name,
                documentName = "PAN Card",
                documentNumber = "ABCPS1234K",
                uploadDate = "2023-06-01",
                expiryDate = "",
                notes = "Verified for bank transfers"
            )
        )
        // Add a document that is expiring soon for alert demonstration
        documentDao.insertDocument(
            DocumentEntity(
                employeeId = id4,
                documentType = DocumentType.CONTRACT.name,
                documentName = "Delivery Driving License & Police Verification",
                documentNumber = "DL-04201900123",
                uploadDate = "2023-02-01",
                expiryDate = "2026-09-30", // within 30 days!
                notes = "Needs renewal this month"
            )
        )

        // Seed initial audit log
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = UserRole.OWNER.name,
                action = "SYSTEM_INITIALIZED",
                affectedRecord = "Vijay General Store",
                details = "System loaded with default staff and store settings"
            )
        )
    }

    suspend fun clearSampleData() {
        employeeDao.deleteSampleEmployees()
        // If there were sample records or demo data created, clear them completely
        attendanceDao.deleteAll()
        leaveDao.deleteAll()
        holidayDao.deleteAll()
        advanceDao.deleteAll()
        overtimeDao.deleteAll()
        payrollDao.deleteAll()
        taskDao.deleteAll()
        documentDao.deleteAll()
        auditLogDao.deleteAll()
    }

    suspend fun clearAllData() {
        employeeDao.deleteAll()
        attendanceDao.deleteAll()
        leaveDao.deleteAll()
        holidayDao.deleteAll()
        advanceDao.deleteAll()
        overtimeDao.deleteAll()
        payrollDao.deleteAll()
        taskDao.deleteAll()
        documentDao.deleteAll()
        auditLogDao.deleteAll()
        auditLogDao.insertLog(
            AuditLogEntity(
                userRole = UserRole.OWNER.name,
                action = "DATABASE_RESET",
                affectedRecord = "All tables",
                details = "All store records were purged"
            )
        )
    }

    suspend fun exportDataAsJson(): String {
        val root = JSONObject()
        root.put("appName", "Vijay General Store")
        root.put("exportTime", System.currentTimeMillis())
        root.put("version", 1)

        val emps = allEmployees.first()
        val empArray = JSONArray()
        for (e in emps) {
            val obj = JSONObject()
            obj.put("empCode", e.empCode)
            obj.put("fullName", e.fullName)
            obj.put("phone", e.phone)
            obj.put("alternatePhone", e.alternatePhone)
            obj.put("dob", e.dob)
            obj.put("gender", e.gender)
            obj.put("address", e.address)
            obj.put("emergencyContact", e.emergencyContact)
            obj.put("emergencyContactPhone", e.emergencyContactPhone)
            obj.put("joiningDate", e.joiningDate)
            obj.put("department", e.department)
            obj.put("designation", e.designation)
            obj.put("employmentType", e.employmentType)
            obj.put("monthlySalary", e.monthlySalary)
            obj.put("paymentMethod", e.paymentMethod)
            obj.put("bankAccount", e.bankAccount)
            obj.put("ifsc", e.ifsc)
            obj.put("salaryEffectiveDate", e.salaryEffectiveDate)
            obj.put("notes", e.notes)
            obj.put("isActive", e.isActive)
            empArray.put(obj)
        }
        root.put("employees", empArray)

        val holidays = allHolidays.first()
        val holArray = JSONArray()
        for (h in holidays) {
            val obj = JSONObject()
            obj.put("name", h.name)
            obj.put("date", h.date)
            obj.put("description", h.description)
            holArray.put(obj)
        }
        root.put("holidays", holArray)

        return root.toString(2)
    }

    suspend fun importDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("employees")) {
                val array = root.getJSONArray("employees")
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val empCode = obj.optString("empCode", "EMP${100 + i}")
                    val existing = employeeDao.getEmployeeByCode(empCode)
                    val emp = EmployeeEntity(
                        id = existing?.id ?: 0,
                        empCode = empCode,
                        fullName = obj.optString("fullName", "Unknown"),
                        phone = obj.optString("phone", ""),
                        alternatePhone = obj.optString("alternatePhone", ""),
                        dob = obj.optString("dob", ""),
                        gender = obj.optString("gender", "Male"),
                        address = obj.optString("address", ""),
                        emergencyContact = obj.optString("emergencyContact", ""),
                        emergencyContactPhone = obj.optString("emergencyContactPhone", ""),
                        joiningDate = obj.optString("joiningDate", ""),
                        department = obj.optString("department", Department.SALES.name),
                        designation = obj.optString("designation", ""),
                        employmentType = obj.optString("employmentType", EmploymentType.FULL_TIME.name),
                        monthlySalary = obj.optDouble("monthlySalary", 0.0),
                        paymentMethod = obj.optString("paymentMethod", PaymentMethod.CASH.name),
                        bankAccount = obj.optString("bankAccount", ""),
                        ifsc = obj.optString("ifsc", ""),
                        salaryEffectiveDate = obj.optString("salaryEffectiveDate", ""),
                        notes = obj.optString("notes", ""),
                        isActive = obj.optBoolean("isActive", true),
                        isSample = false
                    )
                    employeeDao.insertEmployee(emp)
                }
            }
            auditLogDao.insertLog(
                AuditLogEntity(
                    userRole = UserRole.OWNER.name,
                    action = "BACKUP_RESTORED",
                    affectedRecord = "JSON Data Import",
                    details = "Data restored from backup payload"
                )
            )
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
