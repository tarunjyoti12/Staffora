package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.AdvanceEntity
import com.example.data.model.AttendanceEntity
import com.example.data.model.AuditLogEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.EmployeeEntity
import com.example.data.model.HolidayEntity
import com.example.data.model.LeaveEntity
import com.example.data.model.OvertimeEntity
import com.example.data.model.PayrollEntity
import com.example.data.model.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Query("SELECT * FROM employees ORDER BY empCode ASC")
    fun getAllEmployees(): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE id = :id LIMIT 1")
    suspend fun getEmployeeById(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE empCode = :code LIMIT 1")
    suspend fun getEmployeeByCode(code: String): EmployeeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEmployee(employee: EmployeeEntity): Long

    @Update
    suspend fun updateEmployee(employee: EmployeeEntity)

    @Delete
    suspend fun deleteEmployee(employee: EmployeeEntity)

    @Query("DELETE FROM employees WHERE isSample = 1")
    suspend fun deleteSampleEmployees()

    @Query("DELETE FROM employees")
    suspend fun deleteAll()
}

@Dao
interface AttendanceDao {
    @Query("SELECT * FROM attendance WHERE date = :date")
    fun getAttendanceByDate(date: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAttendanceForEmployee(employeeId: Long): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getAttendanceRecord(employeeId: Long, date: String): AttendanceEntity?

    @Query("SELECT * FROM attendance WHERE date LIKE :monthPrefix || '%'")
    fun getAttendanceForMonth(monthPrefix: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendance")
    fun getAllAttendance(): Flow<List<AttendanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAttendance(attendance: AttendanceEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(list: List<AttendanceEntity>)

    @Delete
    suspend fun deleteAttendance(attendance: AttendanceEntity)

    @Query("DELETE FROM attendance")
    suspend fun deleteAll()
}

@Dao
interface LeaveDao {
    @Query("SELECT * FROM leaves ORDER BY startDate DESC")
    fun getAllLeaves(): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leaves WHERE employeeId = :employeeId ORDER BY startDate DESC")
    fun getLeavesForEmployee(employeeId: Long): Flow<List<LeaveEntity>>

    @Query("SELECT * FROM leaves WHERE status = 'PENDING'")
    fun getPendingLeaves(): Flow<List<LeaveEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLeave(leave: LeaveEntity): Long

    @Update
    suspend fun updateLeave(leave: LeaveEntity)

    @Delete
    suspend fun deleteLeave(leave: LeaveEntity)

    @Query("DELETE FROM leaves")
    suspend fun deleteAll()
}

@Dao
interface HolidayDao {
    @Query("SELECT * FROM holidays ORDER BY date ASC")
    fun getAllHolidays(): Flow<List<HolidayEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHoliday(holiday: HolidayEntity): Long

    @Update
    suspend fun updateHoliday(holiday: HolidayEntity)

    @Delete
    suspend fun deleteHoliday(holiday: HolidayEntity)

    @Query("DELETE FROM holidays")
    suspend fun deleteAll()
}

@Dao
interface AdvanceDao {
    @Query("SELECT * FROM advances ORDER BY date DESC")
    fun getAllAdvances(): Flow<List<AdvanceEntity>>

    @Query("SELECT * FROM advances WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getAdvancesForEmployee(employeeId: Long): Flow<List<AdvanceEntity>>

    @Query("SELECT * FROM advances WHERE status = 'ACTIVE' ORDER BY date DESC")
    fun getActiveAdvances(): Flow<List<AdvanceEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAdvance(advance: AdvanceEntity): Long

    @Update
    suspend fun updateAdvance(advance: AdvanceEntity)

    @Delete
    suspend fun deleteAdvance(advance: AdvanceEntity)

    @Query("DELETE FROM advances")
    suspend fun deleteAll()
}

@Dao
interface OvertimeDao {
    @Query("SELECT * FROM overtime ORDER BY date DESC")
    fun getAllOvertime(): Flow<List<OvertimeEntity>>

    @Query("SELECT * FROM overtime WHERE employeeId = :employeeId ORDER BY date DESC")
    fun getOvertimeForEmployee(employeeId: Long): Flow<List<OvertimeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOvertime(overtime: OvertimeEntity): Long

    @Update
    suspend fun updateOvertime(overtime: OvertimeEntity)

    @Delete
    suspend fun deleteOvertime(overtime: OvertimeEntity)

    @Query("DELETE FROM overtime")
    suspend fun deleteAll()
}

@Dao
interface PayrollDao {
    @Query("SELECT * FROM payroll WHERE monthYear = :monthYear ORDER BY employeeId ASC")
    fun getPayrollForMonth(monthYear: String): Flow<List<PayrollEntity>>

    @Query("SELECT * FROM payroll WHERE employeeId = :employeeId ORDER BY monthYear DESC")
    fun getPayrollForEmployee(employeeId: Long): Flow<List<PayrollEntity>>

    @Query("SELECT * FROM payroll ORDER BY monthYear DESC")
    fun getAllPayroll(): Flow<List<PayrollEntity>>

    @Query("SELECT * FROM payroll WHERE employeeId = :employeeId AND monthYear = :monthYear LIMIT 1")
    suspend fun getPayroll(employeeId: Long, monthYear: String): PayrollEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayroll(payroll: PayrollEntity): Long

    @Update
    suspend fun updatePayroll(payroll: PayrollEntity)

    @Delete
    suspend fun deletePayroll(payroll: PayrollEntity)

    @Query("DELETE FROM payroll")
    suspend fun deleteAll()
}

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY dueDate ASC")
    fun getAllTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE employeeId = :employeeId ORDER BY dueDate ASC")
    fun getTasksForEmployee(employeeId: Long): Flow<List<TaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY uploadDate DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE employeeId = :employeeId ORDER BY uploadDate DESC")
    fun getDocumentsForEmployee(employeeId: Long): Flow<List<DocumentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents")
    suspend fun deleteAll()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Query("SELECT * FROM audit_logs WHERE affectedRecord LIKE '%' || :key || '%' ORDER BY timestamp DESC")
    fun getAuditLogsForRecord(key: String): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: AuditLogEntity): Long

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}
