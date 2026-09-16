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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceEntity
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmployeeEntity
import com.example.ui.components.EmptyStateView
import com.example.ui.components.StatusBadge
import com.example.ui.theme.StatusAbsentBg
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusHalfDayBg
import com.example.ui.theme.StatusHalfDayPurple
import com.example.ui.theme.StatusLateBg
import com.example.ui.theme.StatusLateOrange
import com.example.ui.theme.StatusLeaveBg
import com.example.ui.theme.StatusLeaveBlue
import com.example.ui.theme.StatusMissingBg
import com.example.ui.theme.StatusMissingAmber
import com.example.ui.theme.StatusPresentBg
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun AttendanceScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val selectedDate by viewModel.selectedAttendanceDate.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val attendanceList by viewModel.attendanceForSelectedDate.collectAsState()

    val activeEmployees = employees.filter { it.isActive }
    val attMap = attendanceList.associateBy { it.employeeId }
    val missingCount = activeEmployees.count { attMap[it.id] == null }

    var selectedEmployeeForEdit by remember { mutableStateOf<Pair<EmployeeEntity, AttendanceEntity?>?>(null) }
    var filterStatus by remember { mutableStateOf<String>("ALL") }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    fun shiftDate(days: Int) {
        try {
            val cal = Calendar.getInstance()
            val parsed = dateFormat.parse(selectedDate)
            if (parsed != null) cal.time = parsed
            cal.add(Calendar.DAY_OF_YEAR, days)
            viewModel.setSelectedAttendanceDate(dateFormat.format(cal.time))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Date Selector Bar
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
                    IconButton(onClick = { shiftDate(-1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (selectedDate == viewModel.todayDateStr) "Today" else "Selected Date",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (selectedDate == viewModel.todayDateStr) StoreAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = selectedDate,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold)
                        )
                    }

                    IconButton(onClick = { shiftDate(1) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
                    }
                }
            }
        }

        // 2. Attendance Missing Alert Banner & Bulk Mark Button
        if (missingCount > 0) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusMissingBg),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = StatusMissingAmber,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "$missingCount Employee(s) Missing Attendance!",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = StatusMissingAmber
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Important Rule: Missing attendance is never automatically marked Absent. Only owner/manager marks status.",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF7C2D12))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { viewModel.markAllUnmarkedPresent() },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StoreAmber)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Mark All Unmarked as Present", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 3. Quick Status Filter Chips
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val filters = listOf("ALL", "MISSING", "PRESENT", "ABSENT", "LEAVE", "HALF_DAY", "LATE")
                items(filters) { f ->
                    FilterChip(
                        selected = filterStatus == f,
                        onClick = { filterStatus = f },
                        label = {
                            Text(
                                text = when (f) {
                                    "ALL" -> "All (${activeEmployees.size})"
                                    "MISSING" -> "Missing ($missingCount)"
                                    else -> f.lowercase().replaceFirstChar { it.uppercase() }
                                },
                                fontWeight = if (filterStatus == f) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }

        // 4. Employee Attendance List
        val filteredList = activeEmployees.filter { emp ->
            val att = attMap[emp.id]
            when (filterStatus) {
                "ALL" -> true
                "MISSING" -> att == null
                else -> att?.status == filterStatus
            }
        }

        if (filteredList.isEmpty()) {
            item {
                EmptyStateView(
                    icon = Icons.Default.Today,
                    title = "No employees match filter",
                    message = "Switch to 'All' or change selected date to view records."
                )
            }
        }

        items(filteredList) { emp ->
            val att = attMap[emp.id]
            val status = att?.status ?: AttendanceStatus.MISSING.name

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (att == null) StatusMissingBg.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Employee Info Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(StoreNavyPrimary),
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
                                Text(
                                    text = emp.fullName,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${emp.empCode} • ${emp.designation.ifBlank { emp.department }}",
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            StatusBadge(status = status)
                            IconButton(onClick = { selectedEmployeeForEdit = Pair(emp, att) }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit Attendance", tint = StoreAmber)
                            }
                        }
                    }

                    // Times and Details if entered
                    if (att != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "In: ${att.inTime.ifBlank { "--" }}  |  Out: ${att.outTime.ifBlank { "--" }}",
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                            if (att.lateMinutes > 0) {
                                Text(
                                    text = "${att.lateMinutes} min late",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StatusLateOrange, fontWeight = FontWeight.Bold)
                                )
                            }
                            if (att.overtimeHours > 0) {
                                Text(
                                    text = "+${att.overtimeHours}h OT",
                                    style = MaterialTheme.typography.bodySmall.copy(color = StatusPresentGreen, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    // Quick 1-Tap Attendance Buttons
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "1-Tap Quick Mark:",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Present Button
                        Button(
                            onClick = {
                                viewModel.markAttendance(
                                    employeeId = emp.id,
                                    employeeName = emp.fullName,
                                    status = AttendanceStatus.PRESENT,
                                    inTime = "09:00 AM",
                                    outTime = "08:30 PM"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == AttendanceStatus.PRESENT.name) StatusPresentGreen else StatusPresentBg
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Present",
                                color = if (status == AttendanceStatus.PRESENT.name) Color.White else StatusPresentGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Absent Button (Explicitly marked by owner)
                        Button(
                            onClick = {
                                viewModel.markAttendance(
                                    employeeId = emp.id,
                                    employeeName = emp.fullName,
                                    status = AttendanceStatus.ABSENT
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == AttendanceStatus.ABSENT.name) StatusAbsentRed else StatusAbsentBg
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Absent",
                                color = if (status == AttendanceStatus.ABSENT.name) Color.White else StatusAbsentRed,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Half Day Button
                        Button(
                            onClick = {
                                viewModel.markAttendance(
                                    employeeId = emp.id,
                                    employeeName = emp.fullName,
                                    status = AttendanceStatus.HALF_DAY,
                                    inTime = "09:00 AM",
                                    outTime = "02:00 PM"
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == AttendanceStatus.HALF_DAY.name) StatusHalfDayPurple else StatusHalfDayBg
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Half Day",
                                color = if (status == AttendanceStatus.HALF_DAY.name) Color.White else StatusHalfDayPurple,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Late Button
                        Button(
                            onClick = {
                                viewModel.markAttendance(
                                    employeeId = emp.id,
                                    employeeName = emp.fullName,
                                    status = AttendanceStatus.LATE,
                                    inTime = "09:30 AM",
                                    outTime = "08:30 PM",
                                    lateMinutes = 30
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == AttendanceStatus.LATE.name) StatusLateOrange else StatusLateBg
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Late",
                                color = if (status == AttendanceStatus.LATE.name) Color.White else StatusLateOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }

                        // Leave Button
                        Button(
                            onClick = {
                                viewModel.markAttendance(
                                    employeeId = emp.id,
                                    employeeName = emp.fullName,
                                    status = AttendanceStatus.LEAVE
                                )
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (status == AttendanceStatus.LEAVE.name) StatusLeaveBlue else StatusLeaveBg
                            ),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "Leave",
                                color = if (status == AttendanceStatus.LEAVE.name) Color.White else StatusLeaveBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }

    // Edit Attendance Dialog
    selectedEmployeeForEdit?.let { (emp, att) ->
        var editStatus by remember { mutableStateOf(att?.status ?: AttendanceStatus.PRESENT.name) }
        var inTime by remember { mutableStateOf(att?.inTime ?: "09:00 AM") }
        var outTime by remember { mutableStateOf(att?.outTime ?: "08:30 PM") }
        var lateMinutesStr by remember { mutableStateOf(att?.lateMinutes?.toString() ?: "0") }
        var otHoursStr by remember { mutableStateOf(att?.overtimeHours?.toString() ?: "0.0") }
        var notes by remember { mutableStateOf(att?.notes ?: "") }

        AlertDialog(
            onDismissRequest = { selectedEmployeeForEdit = null },
            title = {
                Text(
                    text = "Edit Attendance: ${emp.fullName}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Date: $selectedDate",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    // Status Dropdown / Buttons
                    Text("Select Status:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(AttendanceStatus.values().filter { it != AttendanceStatus.MISSING }) { st ->
                            FilterChip(
                                selected = editStatus == st.name,
                                onClick = { editStatus = st.name },
                                label = { Text(st.displayName, fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = inTime,
                            onValueChange = { inTime = it },
                            label = { Text("In Time") },
                            placeholder = { Text("09:00 AM") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = outTime,
                            onValueChange = { outTime = it },
                            label = { Text("Out Time") },
                            placeholder = { Text("08:30 PM") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = lateMinutesStr,
                            onValueChange = { lateMinutesStr = it },
                            label = { Text("Late Minutes") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = otHoursStr,
                            onValueChange = { otHoursStr = it },
                            label = { Text("Overtime (Hours)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes / Reason") },
                        placeholder = { Text("e.g. Delivery delay, festival overtime") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val lateMin = lateMinutesStr.toIntOrNull() ?: 0
                        val otH = otHoursStr.toDoubleOrNull() ?: 0.0
                        viewModel.markAttendance(
                            employeeId = emp.id,
                            employeeName = emp.fullName,
                            status = AttendanceStatus.valueOf(editStatus),
                            inTime = inTime,
                            outTime = outTime,
                            lateMinutes = lateMin,
                            overtimeHours = otH,
                            notes = notes
                        )
                        selectedEmployeeForEdit = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { selectedEmployeeForEdit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
