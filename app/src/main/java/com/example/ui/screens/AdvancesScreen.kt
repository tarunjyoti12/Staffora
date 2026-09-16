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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.model.AdvanceEntity
import com.example.data.model.AdvanceStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SensitiveText
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancesScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val advances by viewModel.allAdvances.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    val empMap = employees.associateBy { it.id }
    val activeEmps = employees.filter { it.isActive }

    var showAddAdvanceDialog by remember { mutableStateOf(false) }
    var advanceForRepayment by remember { mutableStateOf<AdvanceEntity?>(null) }

    val totalActiveBalance = advances.filter { it.status == AdvanceStatus.ACTIVE.name }
        .sumOf { it.amount - it.recoveredAmount }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total Outstanding Advance Summary Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StoreNavyPrimary),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Total Outstanding Staff Advances",
                            style = MaterialTheme.typography.labelMedium.copy(color = Color.White.copy(alpha = 0.8f))
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SensitiveText(
                            actualValue = "₹${String.format(Locale.getDefault(), "%,.0f", totalActiveBalance)}",
                            userRole = userRole,
                            style = MaterialTheme.typography.headlineMedium.copy(color = StoreAmber, fontWeight = FontWeight.ExtraBold)
                        )
                        Text(
                            text = "Monthly recovery happens automatically via Payroll deductions.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        )
                    }
                }
            }

            if (advances.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.CurrencyRupee,
                        title = "No advances recorded",
                        message = "Staff advances and loans will appear here with remaining recovery balances.",
                        actionText = "+ Record Advance",
                        onActionClicked = { showAddAdvanceDialog = true }
                    )
                }
            }

            items(advances) { adv ->
                val emp = empMap[adv.employeeId]
                val empName = emp?.fullName ?: "Staff #${adv.employeeId}"
                val remaining = adv.amount - adv.recoveredAmount
                val isFullyPaid = remaining <= 0

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(empName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Given on ${adv.date}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatusBadge(status = if (isFullyPaid) "PAID" else "ACTIVE")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Advance Amount", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                SensitiveText(actualValue = "₹${adv.amount}", userRole = userRole, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                            }
                            Column {
                                Text("Recovered", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                SensitiveText(actualValue = "₹${adv.recoveredAmount}", userRole = userRole, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = StatusPresentGreen))
                            }
                            Column {
                                Text("Remaining Balance", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                SensitiveText(
                                    actualValue = "₹$remaining",
                                    userRole = userRole,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = if (remaining > 0) StatusAbsentRed else StatusPresentGreen)
                                )
                            }
                        }

                        if (adv.reason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Reason: ${adv.reason}", fontSize = 12.sp)
                        }

                        if (!isFullyPaid) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { advanceForRepayment = adv },
                                colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Record Partial / Full Repayment", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddAdvanceDialog = true },
            containerColor = StoreAmber,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Advance")
        }
    }

    // Add Advance Dialog
    if (showAddAdvanceDialog) {
        var selectedEmpId by remember { mutableStateOf(activeEmps.firstOrNull()?.id ?: 0L) }
        var amountStr by remember { mutableStateOf("2000") }
        var date by remember { mutableStateOf(viewModel.todayDateStr) }
        var reason by remember { mutableStateOf("") }
        var monthlyDedStr by remember { mutableStateOf("1000") }
        var empDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddAdvanceDialog = false },
            title = { Text("Record Staff Advance", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select Employee:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    ExposedDropdownMenuBox(
                        expanded = empDropdownExpanded,
                        onExpandedChange = { empDropdownExpanded = !empDropdownExpanded }
                    ) {
                        val currentEmpName = empMap[selectedEmpId]?.fullName ?: "Select Employee"
                        OutlinedTextField(
                            value = currentEmpName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = empDropdownExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = empDropdownExpanded,
                            onDismissRequest = { empDropdownExpanded = false }
                        ) {
                            activeEmps.forEach { emp ->
                                DropdownMenuItem(
                                    text = { Text(emp.fullName) },
                                    onClick = {
                                        selectedEmpId = emp.id
                                        empDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("Advance Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date Given") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = monthlyDedStr,
                        onValueChange = { monthlyDedStr = it },
                        label = { Text("Suggested Monthly Deduction (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason / Purpose") },
                        placeholder = { Text("e.g. Festival advance, Medical emergency") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = amountStr.toDoubleOrNull() ?: 0.0
                        val monthlyDed = monthlyDedStr.toDoubleOrNull() ?: 0.0
                        val empName = empMap[selectedEmpId]?.fullName ?: "Employee"

                        viewModel.recordAdvance(
                            advance = AdvanceEntity(
                                employeeId = selectedEmpId,
                                amount = amount,
                                date = date,
                                reason = reason,
                                monthlyDeduction = monthlyDed,
                                status = AdvanceStatus.ACTIVE.name
                            ),
                            employeeName = empName
                        )
                        showAddAdvanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
                ) {
                    Text("Save Advance", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddAdvanceDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Repayment Dialog
    advanceForRepayment?.let { adv ->
        val remaining = adv.amount - adv.recoveredAmount
        val emp = empMap[adv.employeeId]
        val empName = emp?.fullName ?: "Staff"
        var repaymentAmountStr by remember { mutableStateOf(remaining.toString()) }

        AlertDialog(
            onDismissRequest = { advanceForRepayment = null },
            title = { Text("Record Advance Repayment", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Employee: $empName", fontWeight = FontWeight.Bold)
                    Text("Remaining Balance: ₹$remaining")

                    OutlinedTextField(
                        value = repaymentAmountStr,
                        onValueChange = { repaymentAmountStr = it },
                        label = { Text("Repayment Amount (₹)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val repAmt = repaymentAmountStr.toDoubleOrNull() ?: 0.0
                        viewModel.recordAdvanceRepayment(adv, repAmt, empName)
                        advanceForRepayment = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen)
                ) {
                    Text("Confirm Repayment", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { advanceForRepayment = null }) { Text("Cancel") }
            }
        )
    }
}
