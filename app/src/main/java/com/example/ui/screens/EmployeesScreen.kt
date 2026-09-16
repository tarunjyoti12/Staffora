package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Department
import com.example.data.model.EmployeeEntity
import com.example.data.model.EmploymentType
import com.example.data.model.PaymentMethod
import com.example.data.model.UserRole
import com.example.ui.components.EmptyStateView
import com.example.ui.components.SensitiveText
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel
import java.util.Locale

@Composable
fun EmployeesScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val employees by viewModel.allEmployees.collectAsState()
    val userRole by viewModel.currentUserRole.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedDept by remember { mutableStateOf("ALL") }
    var sortBy by remember { mutableStateOf("CODE") } // CODE, NAME, DATE

    var showAddEditDialog by remember { mutableStateOf(false) }
    var employeeToEdit by remember { mutableStateOf<EmployeeEntity?>(null) }
    var selectedEmployeeForDetail by remember { mutableStateOf<EmployeeEntity?>(null) }

    val departments = listOf("ALL") + Department.values().map { it.name }

    val filtered = employees.filter { emp ->
        val matchesSearch = searchQuery.isBlank() ||
                emp.fullName.contains(searchQuery, ignoreCase = true) ||
                emp.empCode.contains(searchQuery, ignoreCase = true) ||
                emp.phone.contains(searchQuery, ignoreCase = true) ||
                emp.designation.contains(searchQuery, ignoreCase = true) ||
                emp.department.contains(searchQuery, ignoreCase = true)

        val matchesDept = selectedDept == "ALL" || emp.department == selectedDept
        matchesSearch && matchesDept
    }.sortedWith { a, b ->
        when (sortBy) {
            "NAME" -> a.fullName.compareTo(b.fullName, ignoreCase = true)
            "DATE" -> b.joiningDate.compareTo(a.joiningDate)
            else -> a.empCode.compareTo(b.empCode)
        }
    }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search & Sort Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, ID, phone, role...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }

            // Department Filters
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(departments) { dept ->
                        FilterChip(
                            selected = selectedDept == dept,
                            onClick = { selectedDept = dept },
                            label = {
                                Text(
                                    if (dept == "ALL") "All Departments"
                                    else Department.valueOf(dept).displayName,
                                    fontSize = 12.sp
                                )
                            }
                        )
                    }
                }
            }

            // Employee Count Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${filtered.size} Employee(s)",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Sort: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilterChip(
                            selected = sortBy == "CODE",
                            onClick = { sortBy = "CODE" },
                            label = { Text("ID", fontSize = 11.sp) }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        FilterChip(
                            selected = sortBy == "NAME",
                            onClick = { sortBy = "NAME" },
                            label = { Text("Name", fontSize = 11.sp) }
                        )
                    }
                }
            }

            if (filtered.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Person,
                        title = "No employees found",
                        message = "Try clearing your search query or add a new employee.",
                        actionText = "+ Add Employee",
                        onActionClicked = {
                            employeeToEdit = null
                            showAddEditDialog = true
                        }
                    )
                }
            }

            items(filtered) { emp ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { selectedEmployeeForDetail = emp }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (emp.isActive) StoreNavyPrimary else Color.Gray),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = emp.fullName.take(1).uppercase(),
                                        color = StoreAmber,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = emp.fullName,
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (emp.isSample) {
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = StoreAmber.copy(alpha = 0.2f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "DEMO",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = StoreAmber,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = "${emp.empCode} • ${emp.designation.ifBlank { emp.department }}",
                                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (emp.isActive) StatusPresentBg else StatusAbsentBg
                            ) {
                                Text(
                                    text = if (emp.isActive) "Active" else "Inactive",
                                    color = if (emp.isActive) StatusPresentGreen else StatusAbsentRed,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(emp.phone, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            // Sensitive salary displayed securely!
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Salary: ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                SensitiveText(
                                    actualValue = "₹${String.format(Locale.getDefault(), "%,.0f", emp.monthlySalary)}/mo",
                                    userRole = userRole,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        // Add Employee FAB
        FloatingActionButton(
            onClick = {
                employeeToEdit = null
                showAddEditDialog = true
            },
            containerColor = StoreAmber,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Employee")
        }
    }

    // Add / Edit Employee Dialog
    if (showAddEditDialog) {
        AddEditEmployeeDialog(
            initialEmployee = employeeToEdit,
            totalEmployees = employees.size,
            onDismiss = { showAddEditDialog = false },
            onSave = { emp, isEdit ->
                viewModel.saveEmployee(emp, isEdit)
                showAddEditDialog = false
            }
        )
    }

    // Employee Detailed Profile Dialog with 9 Tabs
    selectedEmployeeForDetail?.let { emp ->
        EmployeeDetailDialog(
            employee = emp,
            viewModel = viewModel,
            userRole = userRole,
            onDismiss = { selectedEmployeeForDetail = null },
            onEdit = {
                employeeToEdit = emp
                selectedEmployeeForDetail = null
                showAddEditDialog = true
            },
            onToggleActive = {
                viewModel.toggleEmployeeActive(emp)
                selectedEmployeeForDetail = emp.copy(isActive = !emp.isActive)
            }
        )
    }
}

@Composable
fun AddEditEmployeeDialog(
    initialEmployee: EmployeeEntity?,
    totalEmployees: Int,
    onDismiss: () -> Unit,
    onSave: (EmployeeEntity, Boolean) -> Unit
) {
    val isEdit = initialEmployee != null

    var empCode by remember { mutableStateOf(initialEmployee?.empCode ?: "EMP00${totalEmployees + 1}") }
    var fullName by remember { mutableStateOf(initialEmployee?.fullName ?: "") }
    var phone by remember { mutableStateOf(initialEmployee?.phone ?: "") }
    var alternatePhone by remember { mutableStateOf(initialEmployee?.alternatePhone ?: "") }
    var dob by remember { mutableStateOf(initialEmployee?.dob ?: "") }
    var gender by remember { mutableStateOf(initialEmployee?.gender ?: "Male") }
    var address by remember { mutableStateOf(initialEmployee?.address ?: "") }
    var emergencyContact by remember { mutableStateOf(initialEmployee?.emergencyContact ?: "") }
    var emergencyContactPhone by remember { mutableStateOf(initialEmployee?.emergencyContactPhone ?: "") }

    var joiningDate by remember { mutableStateOf(initialEmployee?.joiningDate ?: "2026-09-01") }
    var department by remember { mutableStateOf(initialEmployee?.department ?: Department.SALES.name) }
    var designation by remember { mutableStateOf(initialEmployee?.designation ?: "") }
    var employmentType by remember { mutableStateOf(initialEmployee?.employmentType ?: EmploymentType.FULL_TIME.name) }

    var monthlySalaryStr by remember { mutableStateOf(initialEmployee?.monthlySalary?.toString() ?: "15000") }
    var paymentMethod by remember { mutableStateOf(initialEmployee?.paymentMethod ?: PaymentMethod.CASH.name) }
    var bankAccount by remember { mutableStateOf(initialEmployee?.bankAccount ?: "") }
    var ifsc by remember { mutableStateOf(initialEmployee?.ifsc ?: "") }
    var notes by remember { mutableStateOf(initialEmployee?.notes ?: "") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEdit) "Edit Employee" else "Add New Employee",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = StatusAbsentRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text("Personal Information", fontWeight = FontWeight.Bold, color = StoreAmber, fontSize = 13.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = empCode,
                        onValueChange = { empCode = it },
                        label = { Text("Employee ID *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = fullName,
                        onValueChange = { fullName = it },
                        label = { Text("Full Name *") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number *") },
                        placeholder = { Text("+91 98765 43210") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = alternatePhone,
                        onValueChange = { alternatePhone = it },
                        label = { Text("Alt Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Date of Birth") },
                        placeholder = { Text("YYYY-MM-DD") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = { Text("Emergency Contact") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = emergencyContactPhone,
                        onValueChange = { emergencyContactPhone = it },
                        label = { Text("Contact Phone") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Text("Employment Details", fontWeight = FontWeight.Bold, color = StoreAmber, fontSize = 13.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = joiningDate,
                        onValueChange = { joiningDate = it },
                        label = { Text("Joining Date") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = { Text("Designation") },
                        placeholder = { Text("e.g. Cashier") },
                        modifier = Modifier.weight(1.2f),
                        singleLine = true
                    )
                }

                Text("Department:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(Department.values()) { d ->
                        FilterChip(
                            selected = department == d.name,
                            onClick = { department = d.name },
                            label = { Text(d.displayName, fontSize = 11.sp) }
                        )
                    }
                }

                Text("Salary & Banking", fontWeight = FontWeight.Bold, color = StoreAmber, fontSize = 13.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = monthlySalaryStr,
                        onValueChange = { monthlySalaryStr = it },
                        label = { Text("Monthly Salary (₹) *") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

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

                if (paymentMethod != PaymentMethod.CASH.name) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = bankAccount,
                            onValueChange = { bankAccount = it },
                            label = { Text("Bank A/C or UPI ID") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = ifsc,
                            onValueChange = { ifsc = it },
                            label = { Text("IFSC Code") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes / Store Role") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (empCode.isBlank()) {
                        errorMessage = "Employee ID cannot be empty."
                        return@Button
                    }
                    if (fullName.isBlank()) {
                        errorMessage = "Full Name cannot be empty."
                        return@Button
                    }
                    if (phone.isBlank()) {
                        errorMessage = "Phone number is required."
                        return@Button
                    }
                    val salary = monthlySalaryStr.toDoubleOrNull() ?: 0.0

                    val newEmp = (initialEmployee ?: EmployeeEntity(
                        empCode = empCode,
                        fullName = fullName,
                        phone = phone
                    )).copy(
                        empCode = empCode.trim(),
                        fullName = fullName.trim(),
                        phone = phone.trim(),
                        alternatePhone = alternatePhone.trim(),
                        dob = dob.trim(),
                        gender = gender.trim(),
                        address = address.trim(),
                        emergencyContact = emergencyContact.trim(),
                        emergencyContactPhone = emergencyContactPhone.trim(),
                        joiningDate = joiningDate.trim(),
                        department = department,
                        designation = designation.trim(),
                        employmentType = employmentType,
                        monthlySalary = salary,
                        paymentMethod = paymentMethod,
                        bankAccount = bankAccount.trim(),
                        ifsc = ifsc.trim(),
                        notes = notes.trim()
                    )
                    onSave(newEmp, isEdit)
                },
                colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
            ) {
                Text(if (isEdit) "Save Changes" else "Create Employee", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun EmployeeDetailDialog(
    employee: EmployeeEntity,
    viewModel: StoreViewModel,
    userRole: UserRole,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    val attendance by viewModel.repository.getAttendanceForEmployee(employee.id).collectAsState(emptyList())
    val leaves by viewModel.repository.getLeavesForEmployee(employee.id).collectAsState(emptyList())
    val advances by viewModel.repository.getAdvancesForEmployee(employee.id).collectAsState(emptyList())
    val overtime by viewModel.repository.getOvertimeForEmployee(employee.id).collectAsState(emptyList())
    val payrolls by viewModel.repository.getPayrollForEmployee(employee.id).collectAsState(emptyList())
    val tasks by viewModel.repository.getTasksForEmployee(employee.id).collectAsState(emptyList())
    val documents by viewModel.repository.getDocumentsForEmployee(employee.id).collectAsState(emptyList())

    val tabs = listOf(
        "Overview", "Attendance", "Leaves", "Salary", "Advances",
        "Overtime", "Documents", "Tasks", "History"
    )
    var selectedTabIndex by remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(StoreNavyPrimary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = employee.fullName.take(1).uppercase(),
                            color = StoreAmber,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(employee.fullName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("${employee.empCode} • ${employee.department}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    edgePadding = 0.dp,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = { Text(title, fontSize = 12.sp, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.height(340.dp).verticalScroll(rememberScrollState())) {
                    when (selectedTabIndex) {
                        // 1. Overview Tab
                        0 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text("Personal Details", fontWeight = FontWeight.Bold, color = StoreAmber)
                                Text("• Phone: ${employee.phone}")
                                if (employee.alternatePhone.isNotBlank()) Text("• Alt Phone: ${employee.alternatePhone}")
                                if (employee.dob.isNotBlank()) Text("• Date of Birth: ${employee.dob}")
                                if (employee.address.isNotBlank()) Text("• Address: ${employee.address}")
                                if (employee.emergencyContact.isNotBlank()) Text("• Emergency Contact: ${employee.emergencyContact} (${employee.emergencyContactPhone})")

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Employment Details", fontWeight = FontWeight.Bold, color = StoreAmber)
                                Text("• Department: ${employee.department}")
                                Text("• Designation: ${employee.designation.ifBlank { "Staff" }}")
                                Text("• Type: ${employee.employmentType}")
                                Text("• Joining Date: ${employee.joiningDate.ifBlank { "Not recorded" }}")

                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Salary Information", fontWeight = FontWeight.Bold, color = StoreAmber)
                                Row {
                                    Text("• Monthly Salary: ")
                                    SensitiveText(
                                        actualValue = "₹${String.format(Locale.getDefault(), "%,.0f", employee.monthlySalary)}",
                                        userRole = userRole,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text("• Payment Mode: ${employee.paymentMethod}")
                                if (employee.bankAccount.isNotBlank()) {
                                    Row {
                                        Text("• Bank A/C: ")
                                        SensitiveText(
                                            actualValue = "${employee.bankAccount} (${employee.ifsc})",
                                            userRole = userRole
                                        )
                                    }
                                }
                                if (employee.notes.isNotBlank()) {
                                    Text("• Notes: ${employee.notes}")
                                }
                            }
                        }

                        // 2. Attendance Tab
                        1 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                val presentCount = attendance.count { it.status == "PRESENT" }
                                val absentCount = attendance.count { it.status == "ABSENT" }
                                val lateCount = attendance.count { it.status == "LATE" }
                                Text("Attendance Records: ${attendance.size} entries", fontWeight = FontWeight.Bold)
                                Text("Summary: $presentCount Present, $absentCount Absent, $lateCount Late")
                                Spacer(modifier = Modifier.height(4.dp))
                                if (attendance.isEmpty()) {
                                    Text("No attendance records found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    attendance.take(15).forEach { att ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(att.date, fontWeight = FontWeight.Medium)
                                            Text(att.status, fontWeight = FontWeight.Bold, color = if (att.status == "PRESENT") StatusPresentGreen else StatusAbsentRed)
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Leaves Tab
                        2 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Leave Requests & History", fontWeight = FontWeight.Bold)
                                if (leaves.isEmpty()) {
                                    Text("No leave requests found for this employee.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    leaves.forEach { l ->
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Text(l.leaveType, fontWeight = FontWeight.Bold)
                                                    Text(l.status, fontWeight = FontWeight.Bold, color = if (l.status == "APPROVED") StatusPresentGreen else StoreAmber)
                                                }
                                                Text("${l.startDate} to ${l.endDate} (${l.numberOfDays} days)", fontSize = 12.sp)
                                                if (l.reason.isNotBlank()) Text("Reason: ${l.reason}", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 4. Salary Tab
                        3 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Salary & Payroll Slips", fontWeight = FontWeight.Bold)
                                Row {
                                    Text("Base Monthly Salary: ")
                                    SensitiveText(
                                        actualValue = "₹${employee.monthlySalary}",
                                        userRole = userRole,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                                if (payrolls.isEmpty()) {
                                    Text("No generated payroll entries yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    payrolls.forEach { p ->
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("Month: ${p.monthYear}", fontWeight = FontWeight.Bold)
                                                    Text(p.status, fontWeight = FontWeight.Bold, color = if (p.status == "PAID") StatusPresentGreen else StoreAmber)
                                                }
                                                Row {
                                                    Text("Net: ")
                                                    SensitiveText(
                                                        actualValue = "₹${p.netSalary} (Base ₹${p.baseSalary} + OT ₹${p.overtimeAmount} - Ded ₹${p.advanceDeduction + p.attendanceDeduction})",
                                                        userRole = userRole
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 5. Advances Tab
                        4 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Employee Loans & Advances", fontWeight = FontWeight.Bold)
                                if (advances.isEmpty()) {
                                    Text("No advances taken.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    advances.forEach { a ->
                                        val remaining = a.amount - a.recoveredAmount
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                    Text("₹${a.amount} on ${a.date}", fontWeight = FontWeight.Bold)
                                                    Text("Bal: ₹$remaining", fontWeight = FontWeight.Bold, color = if (remaining > 0) StatusAbsentRed else StatusPresentGreen)
                                                }
                                                Text("Reason: ${a.reason}", fontSize = 11.sp)
                                                Text("Recovered: ₹${a.recoveredAmount} / ₹${a.amount}", fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 6. Overtime Tab
                        5 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Overtime Records", fontWeight = FontWeight.Bold)
                                if (overtime.isEmpty()) {
                                    Text("No overtime recorded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    overtime.forEach { ot ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text("${ot.date}: ${ot.totalHours} hrs", fontWeight = FontWeight.Bold)
                                                Text("@ ₹${ot.hourlyRate}/hr = ₹${ot.totalAmount}", fontSize = 11.sp)
                                            }
                                            Text(ot.status, fontWeight = FontWeight.Bold, color = if (ot.status == "APPROVED") StatusPresentGreen else StoreAmber)
                                        }
                                    }
                                }
                            }
                        }

                        // 7. Documents Tab
                        6 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Employee Documents", fontWeight = FontWeight.Bold)
                                if (documents.isEmpty()) {
                                    Text("No documents uploaded.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    documents.forEach { doc ->
                                        Card(
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(modifier = Modifier.padding(8.dp)) {
                                                Text("${doc.documentType}: ${doc.documentName}", fontWeight = FontWeight.Bold)
                                                if (doc.documentNumber.isNotBlank()) Text("Doc #: ${doc.documentNumber}", fontSize = 11.sp)
                                                if (doc.expiryDate.isNotBlank()) Text("Expires: ${doc.expiryDate}", fontSize = 11.sp, color = StoreAmber)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 8. Tasks Tab
                        7 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Assigned Store Tasks", fontWeight = FontWeight.Bold)
                                if (tasks.isEmpty()) {
                                    Text("No assigned tasks.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                } else {
                                    tasks.forEach { t ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                                .padding(8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column {
                                                Text(t.title, fontWeight = FontWeight.Bold)
                                                Text("Due: ${t.dueDate} • Priority: ${t.priority}", fontSize = 11.sp)
                                            }
                                            Text(t.status, fontWeight = FontWeight.Bold, color = if (t.status == "COMPLETED") StatusPresentGreen else StoreAmber)
                                        }
                                    }
                                }
                            }
                        }

                        // 9. History Tab
                        8 -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text("Audit Trail & Log", fontWeight = FontWeight.Bold)
                                Text("Employee created on ${employee.joiningDate.ifBlank { "Initial setup" }}")
                                Text("Current active status: ${if (employee.isActive) "Active" else "Inactive"}")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Edit Profile")
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onToggleActive) {
                Text(if (employee.isActive) "Deactivate" else "Activate")
            }
        }
    )
}
