package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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

@Database(
    entities = [
        EmployeeEntity::class,
        AttendanceEntity::class,
        LeaveEntity::class,
        HolidayEntity::class,
        AdvanceEntity::class,
        OvertimeEntity::class,
        PayrollEntity::class,
        TaskEntity::class,
        DocumentEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun employeeDao(): EmployeeDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun leaveDao(): LeaveDao
    abstract fun holidayDao(): HolidayDao
    abstract fun advanceDao(): AdvanceDao
    abstract fun overtimeDao(): OvertimeDao
    abstract fun payrollDao(): PayrollDao
    abstract fun taskDao(): TaskDao
    abstract fun documentDao(): DocumentDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "vijay_store_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
