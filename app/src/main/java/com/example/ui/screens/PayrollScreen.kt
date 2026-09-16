package com.example.ui.screens

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmployeeEntity
import com.example.data.model.PaymentMethod
import com.example.data.model.PayrollEntity
import com.example.data.model.PayrollStatus
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SensitiveText
import com.example.ui.components.StatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun PayrollScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val selectedMonth by viewModel.selectedPayrollMonth.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val payrollList by viewModel.payrollForSelectedMonth.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    val empMap = employees.associateBy { it.id }
    val payrollMap = payrollList.associateBy { it.employeeId }
    val activeEmployees = employees.filter { it.isActive }

    var selectedPayrollForPayment by remember { mutableStateOf<Pair<EmployeeEntity, PayrollEntity?>?>(null) }
    var selectedSlipToView by remember { mutableStateOf<Pair<EmployeeEntity, PayrollEntity?>?>(null) }

    val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

    fun shiftMonth(offset: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = monthFormat.parse(selectedMonth)
            if (parsed != null) cal.time = parsed
            cal.add(Calendar.MONTH, offset)
            viewModel.setSelectedPayrollMonth(monthFormat.format(cal.time))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Calculations
    var totalPayroll = 0.0
    var totalPaid = 0.0
    var totalPending = 0.0

    for (emp in activeEmployees) {
        val p = payrollMap[emp.id]
        val net = p?.netSalary ?: emp.monthlySalary
        totalPayroll += net
        if (p?.status == PayrollStatus.PAID.name) {
            totalPaid += net
        } else {
            totalPending += net
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Selector Bar
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = { shiftMonth(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Month")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Payroll Month",
                            style = MaterialTheme.typography.labelSmall.copy(color = StoreAmber, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = selectedMonth,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    IconButton(onClick = { shiftMonth(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Month")
                    }
                }
            }
        }

        // Summary Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Month Net",
                    value = if (userRole == UserRole.OWNER || userRole == UserRole.ADMIN) "₹${String.format(Locale.getDefault(), "%,.0f", totalPayroll)}" else "••••••",
                    subtitle = "${activeEmployees.size} employees",
                    icon = Icons.Default.CurrencyRupee,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Paid Amount",
                    value = if (userRole == UserRole.OWNER || userRole == UserRole.ADMIN) "₹${String.format(Locale.getDefault(), "%,.0f", totalPaid)}" else "••••••",
                    subtitle = "Disbursed",
                    accentColor = StatusPresentGreen,
                    backgroundColor = StatusPresentBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Pending Amount",
                    value = if (userRole == UserRole.OWNER || userRole == UserRole.ADMIN) "₹${String.format(Locale.getDefault(), "%,.0f", totalPending)}" else "••••••",
                    subtitle = "To be paid",
                    accentColor = StoreAmber,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (activeEmployees.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Payment,
                    title = "No active employees",
                    message = "Add active employees to generate monthly payroll."
                )
            }
        }

        items(activeEmployees) { emp ->
            val payroll = payrollMap[emp.id]
            val isPaid = payroll?.status == PayrollStatus.PAID.name
            val netSalary = payroll?.netSalary ?: emp.monthlySalary
            val baseSalary = payroll?.baseSalary ?: emp.monthlySalary
            val bonus = payroll?.bonus ?: 0.0
            val overtimeAmount = payroll?.overtimeAmount ?: 0.0
            val advanceDed = payroll?.advanceDeduction ?: 0.0
            val attDed = payroll?.attendanceDeduction ?: 0.0
            val unpaidLeaveDed = payroll?.unpaidLeaveDeduction ?: 0.0
            val otherDed = payroll?.otherDeduction ?: 0.0

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(emp.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("${emp.empCode} • ${emp.designation.ifBlank { emp.department }}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        StatusBadge(status = if (isPaid) "PAID" else "PENDING")
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Salary calculation breakdown box
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Base Salary:", fontSize = 12.sp)
                                SensitiveText(actualValue = "₹${String.format(Locale.getDefault(), "%,.0f", baseSalary)}", userRole = userRole, style = MaterialTheme.typography.bodySmall)
                            }
                            if (bonus > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("+ Festival/Performance Bonus:", fontSize = 12.sp, color = StatusPresentGreen)
                                    SensitiveText(actualValue = "+₹$bonus", userRole = userRole, style = MaterialTheme.typography.bodySmall.copy(color = StatusPresentGreen))
                                }
                            }
                            if (overtimeAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("+ Overtime Pay:", fontSize = 12.sp, color = StatusPresentGreen)
                                    SensitiveText(actualValue = "+₹$overtimeAmount", userRole = userRole, style = MaterialTheme.typography.bodySmall.copy(color = StatusPresentGreen))
                                }
                            }
                            if (advanceDed > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("- Advance Repayment:", fontSize = 12.sp, color = StoreAmber)
                                    SensitiveText(actualValue = "-₹$advanceDed", userRole = userRole, style = MaterialTheme.typography.bodySmall.copy(color = StoreAmber))
                                }
                            }
                            if (attDed + unpaidLeaveDed + otherDed > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("- Absent / Leave Deductions:", fontSize = 12.sp, color = Color.Red)
                                    SensitiveText(actualValue = "-₹${attDed + unpaidLeaveDed + otherDed}", userRole = userRole, style = MaterialTheme.typography.bodySmall.copy(color = Color.Red))
                                }
                            }
                            Divider(modifier = Modifier.padding(vertical = 4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Net Payable Salary:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                SensitiveText(
                                    actualValue = "₹${String.format(Locale.getDefault(), "%,.0f", netSalary)}",
                                    userRole = userRole,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold, color = StoreNavyPrimary)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { selectedSlipToView = Pair(emp, payroll) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("View Slip", fontSize = 12.sp)
                        }

                        if (!isPaid && (userRole == UserRole.OWNER || userRole == UserRole.ADMIN)) {
                            Button(
                                onClick = { selectedPayrollForPayment = Pair(emp, payroll) },
                                colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Mark as Paid", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        } else if (isPaid) {
                            Surface(
                                color = StatusPresentBg,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.2f).padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusPresentGreen, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Paid on ${payroll?.paymentDate ?: "Date"}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusPresentGreen
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        item { Spacer(modifier = Modifier.height(32.dp)) }
    }

    // Mark As Paid Dialog
    selectedPayrollForPayment?.let { (emp, p) ->
        var paymentDate by remember { mutableStateOf(viewModel.todayDateStr) }
        var paymentMethod by remember { mutableStateOf(emp.paymentMethod) }
        var paymentRef by remember { mutableStateOf(p?.paymentReference ?: "CASH-${System.currentTimeMillis() % 100000}") }
        var notes by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { selectedPayrollForPayment = null },
            title = { Text("Disburse Salary: ${emp.fullName}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Month: $selectedMonth")
                    SensitiveText(
                        actualValue = "Amount to Disburse: ₹${p?.netSalary ?: emp.monthlySalary}",
                        userRole = userRole,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusPresentGreen)
                    )

                    OutlinedTextField(
                        value = paymentDate,
                        onValueChange = { paymentDate = it },
                        label = { Text("Payment Date") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Payment Method:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(PaymentMethod.values()) { pm ->
                            FilterChip(
                                selected = paymentMethod == pm.name,
                                onClick = { paymentMethod = pm.name },
                                label = { Text(pm.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = paymentRef,
                        onValueChange = { paymentRef = it },
                        label = { Text("Payment Reference / Txn ID") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Payment Remarks") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val base = p?.baseSalary ?: emp.monthlySalary
                        val net = p?.netSalary ?: emp.monthlySalary
                        val payrollEntity = (p ?: PayrollEntity(
                            employeeId = emp.id,
                            monthYear = selectedMonth,
                            baseSalary = base,
                            netSalary = net
                        )).copy(
                            status = PayrollStatus.PAID.name,
                            paymentDate = paymentDate,
                            paymentMethod = paymentMethod,
                            paymentReference = paymentRef,
                            notes = notes
                        )
                        viewModel.savePayroll(payrollEntity, emp.fullName)
                        selectedPayrollForPayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen)
                ) {
                    Text("Confirm Payment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedPayrollForPayment = null }) { Text("Cancel") }
            }
        )
    }

    // View Salary Slip Dialog
    selectedSlipToView?.let { (emp, p) ->
        val net = p?.netSalary ?: emp.monthlySalary
        val base = p?.baseSalary ?: emp.monthlySalary
        val isPaid = p?.status == PayrollStatus.PAID.name

        AlertDialog(
            onDismissRequest = { selectedSlipToView = null },
            title = {
                Column {
                    Text("Vijay General Store", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = StoreNavyPrimary)
                    Text("Salary Slip - $selectedMonth", fontSize = 12.sp, color = StoreAmber)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Divider()
                    Text("Employee: ${emp.fullName} (${emp.empCode})", fontWeight = FontWeight.Bold)
                    Text("Department: ${emp.department} • ${emp.designation}")
                    Text("Payment Mode: ${emp.paymentMethod}")
                    if (emp.bankAccount.isNotBlank()) Text("A/C: ${emp.bankAccount} (${emp.ifsc})")
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Gross Base:")
                        SensitiveText(actualValue = "₹$base", userRole = userRole)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Overtime & Bonus:")
                        SensitiveText(actualValue = "+₹${(p?.overtimeAmount ?: 0.0) + (p?.bonus ?: 0.0)}", userRole = userRole)
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Total Deductions:")
                        SensitiveText(actualValue = "-₹${(p?.advanceDeduction ?: 0.0) + (p?.attendanceDeduction ?: 0.0)}", userRole = userRole)
                    }
                    Divider()
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Payable:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        SensitiveText(actualValue = "₹$net", userRole = userRole, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                    }
                    Text("Status: ${if (isPaid) "PAID on ${p?.paymentDate} (Ref: ${p?.paymentReference})" else "PENDING DISBURSEMENT"}", fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(onClick = { selectedSlipToView = null }) {
                    Text("Close")
                }
            }
        )
    }
}
