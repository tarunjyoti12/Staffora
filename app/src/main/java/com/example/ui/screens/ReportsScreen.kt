package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.data.model.PayrollStatus
import com.example.data.model.UserRole
import com.example.ui.components.SensitiveText
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel
import java.util.Locale

@Composable
fun ReportsScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val employees by viewModel.allEmployees.collectAsState()
    val attendanceToday by viewModel.attendanceToday.collectAsState()
    val leaves by viewModel.allLeaves.collectAsState()
    val advances by viewModel.allAdvances.collectAsState()
    val payrollList by viewModel.payrollForSelectedMonth.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    var selectedReportType by remember { mutableStateOf("ATTENDANCE") }

    val activeEmps = employees.filter { it.isActive }
    val empMap = employees.associateBy { it.id }
    val attMap = attendanceToday.associateBy { it.employeeId }

    // Generate formatted report text for copying / export
    val reportText = remember(selectedReportType, employees, attendanceToday, leaves, advances, payrollList) {
        val sb = StringBuilder()
        sb.append("VIJAY GENERAL STORE - BUSINESS REPORT\n")
        sb.append("Generated on: ${viewModel.todayDateStr}\n")
        sb.append("=========================================\n\n")

        when (selectedReportType) {
            "ATTENDANCE" -> {
                sb.append("DAILY ATTENDANCE SUMMARY (${viewModel.todayDateStr})\n\n")
                activeEmps.forEach { emp ->
                    val att = attMap[emp.id]
                    val st = att?.status ?: "MISSING"
                    val inTime = att?.inTime ?: "--"
                    val outTime = att?.outTime ?: "--"
                    sb.append("• ${emp.fullName} [${emp.empCode}] - $st (In: $inTime, Out: $outTime)\n")
                }
            }
            "PAYROLL" -> {
                sb.append("PAYROLL SUMMARY (${viewModel.selectedPayrollMonth.value})\n\n")
                activeEmps.forEach { emp ->
                    val p = payrollList.firstOrNull { it.employeeId == emp.id }
                    val net = p?.netSalary ?: emp.monthlySalary
                    val st = p?.status ?: "PENDING"
                    sb.append("• ${emp.fullName}: Net ₹$net [$st]\n")
                }
            }
            "ADVANCES" -> {
                sb.append("OUTSTANDING ADVANCES & LOANS\n\n")
                advances.forEach { a ->
                    val rem = a.amount - a.recoveredAmount
                    val name = empMap[a.employeeId]?.fullName ?: "Staff"
                    sb.append("• $name: Taken ₹${a.amount}, Recovered ₹${a.recoveredAmount}, Balance ₹$rem\n")
                }
            }
            "EMPLOYEES" -> {
                sb.append("EMPLOYEE MASTER REGISTER\n\n")
                employees.forEach { e ->
                    val st = if (e.isActive) "Active" else "Inactive"
                    sb.append("• ${e.fullName} [${e.empCode}] - ${e.department} (${e.designation}) | Phone: ${e.phone} | $st\n")
                }
            }
        }
        sb.toString()
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Store Reports & Register",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Export and print store records for bookkeeping and auditing",
                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
        }

        // Report Category Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val types = listOf(
                    Pair("ATTENDANCE", "Attendance Report"),
                    Pair("PAYROLL", "Payroll Report"),
                    Pair("ADVANCES", "Advances Ledger"),
                    Pair("EMPLOYEES", "Staff Register")
                )
                items(types) { (key, label) ->
                    FilterChip(
                        selected = selectedReportType == key,
                        onClick = { selectedReportType = key },
                        label = { Text(label, fontWeight = if (selectedReportType == key) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }
        }

        // Report Preview Box
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Report Preview",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )

                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Store Report", reportText)
                                clipboard.setPrimaryClip(clip)
                                viewModel.showMessage("Report copied to clipboard!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp), tint = StoreAmber)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reportText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }
    }
}
