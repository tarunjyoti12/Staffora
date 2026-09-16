package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "employees",
    indices = [Index(value = ["empCode"], unique = true)]
)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val empCode: String,
    val fullName: String,
    val phone: String,
    val alternatePhone: String = "",
    val dob: String = "", // YYYY-MM-DD
    val gender: String = "Male",
    val address: String = "",
    val emergencyContact: String = "",
    val emergencyContactPhone: String = "",
    val joiningDate: String = "", // YYYY-MM-DD
    val department: String = Department.SALES.name,
    val designation: String = "",
    val employmentType: String = EmploymentType.FULL_TIME.name,
    val monthlySalary: Double = 0.0,
    val paymentMethod: String = PaymentMethod.CASH.name,
    val bankAccount: String = "",
    val ifsc: String = "",
    val salaryEffectiveDate: String = "",
    val notes: String = "",
    val isActive: Boolean = true,
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "attendance",
    indices = [Index(value = ["employeeId", "date"], unique = true)]
)
data class AttendanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val date: String, // YYYY-MM-DD
    val status: String, // AttendanceStatus.name
    val inTime: String = "", // HH:MM AM/PM
    val outTime: String = "", // HH:MM AM/PM
    val lateMinutes: Int = 0,
    val overtimeHours: Double = 0.0,
    val notes: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "leaves")
data class LeaveEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val leaveType: String, // LeaveType.name
    val startDate: String, // YYYY-MM-DD
    val endDate: String, // YYYY-MM-DD
    val numberOfDays: Double = 1.0,
    val reason: String = "",
    val status: String = LeaveStatus.PENDING.name,
    val appliedDate: String = "",
    val remarks: String = ""
)

@Entity(tableName = "holidays")
data class HolidayEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val date: String, // YYYY-MM-DD
    val description: String = ""
)

@Entity(tableName = "advances")
data class AdvanceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val amount: Double,
    val date: String, // YYYY-MM-DD
    val reason: String = "",
    val monthlyDeduction: Double = 0.0,
    val recoveredAmount: Double = 0.0,
    val status: String = AdvanceStatus.ACTIVE.name, // Active or Repaid
    val notes: String = ""
)

@Entity(tableName = "overtime")
data class OvertimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val date: String, // YYYY-MM-DD
    val startTime: String = "",
    val endTime: String = "",
    val totalHours: Double = 0.0,
    val hourlyRate: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = OvertimeStatus.PENDING.name,
    val notes: String = ""
)

@Entity(
    tableName = "payroll",
    indices = [Index(value = ["employeeId", "monthYear"], unique = true)]
)
data class PayrollEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val monthYear: String, // YYYY-MM
    val baseSalary: Double = 0.0,
    val bonus: Double = 0.0,
    val overtimeAmount: Double = 0.0,
    val attendanceDeduction: Double = 0.0,
    val unpaidLeaveDeduction: Double = 0.0,
    val advanceDeduction: Double = 0.0,
    val otherDeduction: Double = 0.0,
    val netSalary: Double = 0.0,
    val status: String = PayrollStatus.PENDING.name,
    val paymentDate: String = "",
    val paymentMethod: String = PaymentMethod.CASH.name,
    val paymentReference: String = "",
    val notes: String = ""
)

@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val employeeId: Long,
    val description: String = "",
    val priority: String = TaskPriority.MEDIUM.name,
    val dueDate: String = "", // YYYY-MM-DD
    val status: String = TaskStatus.PENDING.name,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employeeId: Long,
    val documentType: String = DocumentType.AADHAAR.name,
    val documentName: String,
    val documentNumber: String = "",
    val uploadDate: String = "", // YYYY-MM-DD
    val expiryDate: String = "", // YYYY-MM-DD
    val notes: String = ""
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userRole: String,
    val action: String,
    val affectedRecord: String,
    val details: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
