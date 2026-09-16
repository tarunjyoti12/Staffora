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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AttendanceStatus
import com.example.ui.components.AttentionItemCard
import com.example.ui.components.SectionHeader
import com.example.ui.components.StatCard
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
import java.util.Calendar

@Composable
fun DashboardScreen(
    viewModel: StoreViewModel,
    onNavigateToAttendance: () -> Unit,
    onNavigateToEmployees: () -> Unit,
    onNavigateToLeaves: () -> Unit,
    onNavigateToPayroll: () -> Unit,
    onNavigateToAdvances: () -> Unit,
    onNavigateToTasks: () -> Unit,
    onNavigateToHolidays: () -> Unit,
    onAddEmployeeClicked: () -> Unit,
    onAddLeaveClicked: () -> Unit,
    onRecordAdvanceClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary by viewModel.dashboardSummary.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()
    val attendanceToday by viewModel.attendanceToday.collectAsState()
    val holidays by viewModel.allHolidays.collectAsState()
    val advances by viewModel.allAdvances.collectAsState()

    val empMap = employees.associateBy { it.id }
    val attMap = attendanceToday.associateBy { it.employeeId }
    val activeEmps = employees.filter { it.isActive }

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        currentHour < 12 -> "Good Morning 👋"
        currentHour < 17 -> "Good Afternoon ☀️"
        else -> "Good Evening 🌙"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Header & Greeting
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = StoreNavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = greeting,
                        style = MaterialTheme.typography.titleLarge.copy(
                            color = StoreAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Vijay General Store",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold
                        )
                    )
                    Text(
                        text = "Today's Staff Situation • ${viewModel.todayDateStr}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    )
                }
            }
        }

        // 2. Large Summary Stat Cards (Row 1: Total, Present, Absent)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Total Staff",
                    value = "${summary.totalEmployees}",
                    subtitle = "Active in store",
                    icon = Icons.Default.Group,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToEmployees
                )
                StatCard(
                    title = "Present",
                    value = "${summary.presentToday}",
                    subtitle = "Checked in",
                    icon = Icons.Default.CheckCircle,
                    accentColor = StatusPresentGreen,
                    backgroundColor = StatusPresentBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAttendance
                )
                StatCard(
                    title = "Absent",
                    value = "${summary.absentToday}",
                    subtitle = "Marked by owner",
                    icon = Icons.Default.Warning,
                    accentColor = StatusAbsentRed,
                    backgroundColor = StatusAbsentBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAttendance
                )
            }
        }

        // Stat Cards (Row 2: On Leave, Half Day, Late)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "On Leave",
                    value = "${summary.onLeaveToday}",
                    subtitle = "Approved",
                    accentColor = StatusLeaveBlue,
                    backgroundColor = StatusLeaveBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLeaves
                )
                StatCard(
                    title = "Half Day",
                    value = "${summary.halfDayToday}",
                    subtitle = "Shift partial",
                    accentColor = StatusHalfDayPurple,
                    backgroundColor = StatusHalfDayBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAttendance
                )
                StatCard(
                    title = "Late Arrival",
                    value = "${summary.lateToday}",
                    subtitle = "Delayed",
                    accentColor = StatusLateOrange,
                    backgroundColor = StatusLateBg.copy(alpha = 0.5f),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToAttendance
                )
            }
        }

        // CRITICAL ATTENDANCE RULE CARD: Attendance Missing
        // Emphasized prominently so the owner sees unentered employees immediately!
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (summary.attendanceMissing > 0) StatusMissingBg else MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToAttendance() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (summary.attendanceMissing > 0) StatusMissingAmber else StatusPresentGreen),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (summary.attendanceMissing > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Attendance Missing",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (summary.attendanceMissing > 0) StatusMissingAmber else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (summary.attendanceMissing > 0)
                                    "${summary.attendanceMissing} staff records not yet entered for today"
                                else
                                    "All active employees have attendance recorded!",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Text(
                        text = "${summary.attendanceMissing}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (summary.attendanceMissing > 0) StatusMissingAmber else StatusPresentGreen
                        )
                    )
                }
            }
        }

        // 3. Quick Actions
        item {
            SectionHeader(title = "Quick Actions", subtitle = "Common daily shop tasks")
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                item {
                    Button(
                        onClick = onNavigateToAttendance,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StoreAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Mark Attendance", fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Button(
                        onClick = onAddEmployeeClicked,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Add Employee", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Button(
                        onClick = onAddLeaveClicked,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.EventNote, contentDescription = null, tint = StatusLeaveBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("+ Add Leave", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Button(
                        onClick = onRecordAdvanceClicked,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.CurrencyRupee, contentDescription = null, tint = StoreAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Record Advance", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
                item {
                    Button(
                        onClick = onNavigateToPayroll,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.height(48.dp)
                    ) {
                        Icon(Icons.Default.Assignment, contentDescription = null, tint = StatusPresentGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Payroll", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // 4. Attention Required Section
        item {
            SectionHeader(
                title = "Attention Required",
                subtitle = "Items needing store manager action"
            )
        }

        if (summary.attendanceMissing > 0) {
            item {
                AttentionItemCard(
                    title = "${summary.attendanceMissing} Attendance Missing",
                    subtitle = "Never mark Absent automatically. Record real status.",
                    actionText = "Enter Now",
                    onActionClicked = onNavigateToAttendance
                )
            }
        }

        if (summary.pendingLeavesCount > 0) {
            item {
                AttentionItemCard(
                    title = "${summary.pendingLeavesCount} Pending Leave Request(s)",
                    subtitle = "Employees waiting for leave approval",
                    actionText = "Review",
                    onActionClicked = onNavigateToLeaves
                )
            }
        }

        if (summary.pendingAdvancesCount > 0) {
            item {
                AttentionItemCard(
                    title = "${summary.pendingAdvancesCount} Active Staff Advance(s)",
                    subtitle = "Check monthly recovery during payroll",
                    actionText = "View Loans",
                    onActionClicked = onNavigateToAdvances
                )
            }
        }

        if (summary.expiringDocsCount > 0) {
            item {
                AttentionItemCard(
                    title = "${summary.expiringDocsCount} Document(s) Expiring Soon",
                    subtitle = "Identity or driving documents expire within 30 days",
                    actionText = "View Docs",
                    onActionClicked = onNavigateToEmployees
                )
            }
        }

        if (summary.overdueTasksCount > 0) {
            item {
                AttentionItemCard(
                    title = "${summary.overdueTasksCount} Overdue Store Task(s)",
                    subtitle = "Tasks pending past their due date",
                    actionText = "View Tasks",
                    onActionClicked = onNavigateToTasks
                )
            }
        }

        if (summary.attendanceMissing == 0 && summary.pendingLeavesCount == 0 &&
            summary.pendingAdvancesCount == 0 && summary.expiringDocsCount == 0 && summary.overdueTasksCount == 0) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusPresentGreen)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "All clear! No urgent attention required at this moment.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        // 5. Today's Attendance Snapshot
        item {
            SectionHeader(
                title = "Today's Attendance",
                subtitle = "Active employees status snapshot",
                actionText = "View Full List",
                onActionClicked = onNavigateToAttendance
            )
        }

        items(activeEmps) { emp ->
            val att = attMap[emp.id]
            val status = att?.status ?: AttendanceStatus.MISSING.name

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StoreNavyPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emp.fullName.take(1).uppercase(),
                                color = StoreAmber,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = emp.fullName,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${emp.empCode} • ${emp.designation.ifBlank { emp.department }}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        StatusBadge(status = status)
                        if (att != null && att.inTime.isNotBlank()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "In: ${att.inTime}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // 6. Upcoming (Holidays, Birthdays, Advances)
        item {
            SectionHeader(title = "Upcoming", subtitle = "Store calendar & events")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Upcoming Holidays card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onNavigateToHolidays() },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = StoreAmber, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Holidays", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        if (holidays.isEmpty()) {
                            Text("No holidays scheduled", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val nextHoli = holidays.first()
                            Text(nextHoli.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(nextHoli.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                // Employee Birthdays card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Cake, contentDescription = null, tint = StatusLeaveBlue, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Birthdays", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val bdays = activeEmps.filter { it.dob.isNotBlank() }
                        if (bdays.isEmpty()) {
                            Text("No upcoming birthdays", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            val b = bdays.first()
                            Text(b.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            Text(b.dob, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
