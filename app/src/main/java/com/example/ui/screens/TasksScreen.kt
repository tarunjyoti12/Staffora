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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
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
import com.example.data.model.TaskEntity
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StatusPresentGreen
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasksScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val tasks by viewModel.allTasks.collectAsState()
    val employees by viewModel.allEmployees.collectAsState()

    val empMap = employees.associateBy { it.id }
    val activeEmps = employees.filter { it.isActive }

    var showAddTaskDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Store Tasks & Responsibilities",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "Assign daily and weekly tasks to store staff",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }

            if (tasks.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Assignment,
                        title = "No tasks assigned",
                        message = "Create shop duties such as inventory check, aisle restock, and payment reconciliations.",
                        actionText = "+ Create Task",
                        onActionClicked = { showAddTaskDialog = true }
                    )
                }
            }

            items(tasks) { task ->
                val emp = empMap[task.employeeId]
                val empName = emp?.fullName ?: "Unassigned Staff"
                val isDone = task.status == TaskStatus.COMPLETED.name

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                val nextStatus = if (isDone) TaskStatus.PENDING.name else TaskStatus.COMPLETED.name
                                viewModel.updateTask(task.copy(status = nextStatus))
                            }
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                contentDescription = "Toggle Complete",
                                tint = if (isDone) StatusPresentGreen else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            if (task.description.isNotBlank()) {
                                Text(
                                    text = task.description,
                                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (task.priority) {
                                        TaskPriority.URGENT.name -> StatusAbsentRed.copy(alpha = 0.15f)
                                        TaskPriority.HIGH.name -> StoreAmber.copy(alpha = 0.15f)
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                ) {
                                    Text(
                                        text = task.priority,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = when (task.priority) {
                                            TaskPriority.URGENT.name -> StatusAbsentRed
                                            TaskPriority.HIGH.name -> StoreAmber
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        },
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Assigned to: $empName • Due: ${task.dueDate}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        IconButton(onClick = { viewModel.deleteTask(task) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Task", tint = Color.Gray, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(72.dp)) }
        }

        FloatingActionButton(
            onClick = { showAddTaskDialog = true },
            containerColor = StoreAmber,
            contentColor = Color.Black,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Task")
        }
    }

    // Add Task Dialog
    if (showAddTaskDialog) {
        var selectedEmpId by remember { mutableStateOf(activeEmps.firstOrNull()?.id ?: 0L) }
        var title by remember { mutableStateOf("") }
        var description by remember { mutableStateOf("") }
        var dueDate by remember { mutableStateOf(viewModel.todayDateStr) }
        var priority by remember { mutableStateOf(TaskPriority.MEDIUM.name) }
        var empDropdownExpanded by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            title = { Text("Add Store Task", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        placeholder = { Text("e.g. Rice aisle inventory count") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Assign to Staff:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    ExposedDropdownMenuBox(
                        expanded = empDropdownExpanded,
                        onExpandedChange = { empDropdownExpanded = !empDropdownExpanded }
                    ) {
                        val currentEmpName = empMap[selectedEmpId]?.fullName ?: "Select Staff"
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
                        value = dueDate,
                        onValueChange = { dueDate = it },
                        label = { Text("Due Date") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Priority:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TaskPriority.values().forEach { pr ->
                            FilterChip(
                                selected = priority == pr.name,
                                onClick = { priority = pr.name },
                                label = { Text(pr.name, fontSize = 11.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            viewModel.addTask(
                                TaskEntity(
                                    title = title.trim(),
                                    description = description.trim(),
                                    employeeId = selectedEmpId,
                                    dueDate = dueDate,
                                    priority = priority
                                )
                            )
                            showAddTaskDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
                ) {
                    Text("Add Task", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showAddTaskDialog = false }) { Text("Cancel") }
            }
        )
    }
}
