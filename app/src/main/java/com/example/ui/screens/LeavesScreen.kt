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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
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
import com.example.data.model.LeaveEntity
import com.example.data.model.LeaveStatus
import com.example.data.model.LeaveType
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeavesScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val leaves by viewModel.allLeaves.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    val empMap = employees.associateBy { it.id }
    var filterStatus by remember { mutableStateOf("ALL") }
    var showAddLeaveDialog by remember { mutableStateOf(false) }

    val filteredLeaves = leaves.filter {
        filterStatus == "ALL" || it.status == filterStatus
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val statuses = listOf("ALL", "PENDING", "APPROVED", "REJECTED")
                    items(statuses) { s ->
                        FilterChip(
                            selected = filterStatus == s,
                            onClick = { filterStatus = s },
                            label = {
                                Text(
                                    when (s) {
                                        "ALL" -> "All (${leaves.size})"
                                        "PENDING" -> "Pending (${leaves.count { it.status == "PENDING" }})"
                                        else -> s.lowercase().replaceFirstChar { it.uppercase() }
                                    },
                                    fontWeight = if (filterStatus == s) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            if (filteredLeaves.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.EventNote,
                        title = "No leave applications found",
                        message = "Employees can submit leave requests or store manager can add on their behalf.",
                        actionText = "+ Apply Leave",
                        onActionClicked = { showAddLeaveDialog = true }
                    )
                }
            }

            items(filteredLeaves) { leave ->
                val emp = empMap[leave.employeeId]
                val empName = emp?.fullName ?: "Staff #${leave.employeeId}"

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
                                Text(
                                    "${leave.leaveType} Leave • ${leave.numberOfDays} Day(s)",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            StatusBadge(status = leave.status)
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("From: ${leave.startDate}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("To: ${leave.endDate}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        }

                        if (leave.reason.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Reason: ${leave.reason}", fontSize = 12.sp)
                        }

                        if (leave.remarks.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Remarks: ${leave.remarks}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        // Owner / Manager Approval Actions
                        if (leave.status == LeaveStatus.PENDING.name && (userRole == UserRole.OWNER || userRole == UserRole.ADMIN || userRole == UserRole.MANAGER)) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.updateLeaveStatus(
                                            leave = leave,
                                            newStatus = LeaveStatus.APPROVED,
                                            remarks = "Approved by Store Owner",
                                            employeeName = empName
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusPresentGreen),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Approve", fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        viewModel.updateLeaveStatus(
                                            leave = leave,
                                            newStatus = LeaveStatus.REJECTED,
                                            remarks = "Store requirement / peak hours",
                                            employeeName = empName
                                        )
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusAbsentRed),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Reject", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddLeaveDialog = true },
            containerColor = StoreAmber,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Leave")
        }
    }

    // Add Leave Request Dialog
    if (showAddLeaveDialog) {
        val activeEmps = employees.filter { it.isActive }
        var selectedEmpId by remember { mutableStateOf(activeEmps.firstOrNull()?.id ?: 0L) }
        var leaveType by remember { mutableStateOf(LeaveType.CASUAL.name) }
        var startDate by remember { mutableStateOf(viewModel.todayDateStr) }
        var endDate by remember { mutableStateOf(viewModel.todayDateStr) }
        var numberOfDaysStr by remember { mutableStateOf("1.0") }
        var reason by remember { mutableStateOf("") }
        var empDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddLeaveDialog = false },
            title = { Text("Record Leave Request", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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

                    Text("Leave Type:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(LeaveType.values()) { lt ->
                            FilterChip(
                                selected = leaveType == lt.name,
                                onClick = { leaveType = lt.name },
                                label = { Text(lt.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = startDate,
                            onValueChange = { startDate = it },
                            label = { Text("Start Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = endDate,
                            onValueChange = { endDate = it },
                            label = { Text("End Date") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = numberOfDaysStr,
                        onValueChange = { numberOfDaysStr = it },
                        label = { Text("Number of Days") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for Leave") },
                        placeholder = { Text("e.g. Family function, illness") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val days = numberOfDaysStr.toDoubleOrNull() ?: 1.0
                        val empName = empMap[selectedEmpId]?.fullName ?: "Employee"
                        viewModel.requestLeave(
                            leave = LeaveEntity(
                                employeeId = selectedEmpId,
                                leaveType = leaveType,
                                startDate = startDate,
                                endDate = endDate,
                                numberOfDays = days,
                                reason = reason,
                                status = LeaveStatus.APPROVED.name, // When recorded by manager, direct approval
                                appliedDate = viewModel.todayDateStr,
                                remarks = "Recorded directly in store ledger"
                            ),
                            employeeName = empName
                        )
                        showAddLeaveDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
                ) {
                    Text("Save Leave", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddLeaveDialog = false }) { Text("Cancel") }
            }
        )
    }
}
