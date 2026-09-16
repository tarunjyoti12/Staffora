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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.EmptyStateView
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel

@Composable
fun SearchScreen(
    viewModel: StoreViewModel,
    onBackClicked: () -> Unit,
    onEmployeeClicked: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    val employees by viewModel.allEmployees.collectAsState()
    val leaves by viewModel.allLeaves.collectAsState()
    val tasks by viewModel.allTasks.collectAsState()
    val advances by viewModel.allAdvances.collectAsState()

    val matchedEmployees = if (query.isBlank()) emptyList() else employees.filter {
        it.fullName.contains(query, ignoreCase = true) ||
                it.empCode.contains(query, ignoreCase = true) ||
                it.phone.contains(query, ignoreCase = true) ||
                it.department.contains(query, ignoreCase = true) ||
                it.designation.contains(query, ignoreCase = true)
    }

    val matchedTasks = if (query.isBlank()) emptyList() else tasks.filter {
        it.title.contains(query, ignoreCase = true) || it.description.contains(query, ignoreCase = true)
    }

    val matchedLeaves = if (query.isBlank()) emptyList() else leaves.filter {
        it.reason.contains(query, ignoreCase = true) || it.leaveType.contains(query, ignoreCase = true)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Search Header
        Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClicked) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Search employees, tasks, leaves...") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (query.isBlank()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Search,
                        title = "Global Search",
                        message = "Type an employee name, code (e.g. EMP001), phone number, task, or department."
                    )
                }
            } else if (matchedEmployees.isEmpty() && matchedTasks.isEmpty() && matchedLeaves.isEmpty()) {
                item {
                    EmptyStateView(
                        icon = Icons.Default.Search,
                        title = "No results found",
                        message = "No records matching '$query' were found in the store database."
                    )
                }
            }

            if (matchedEmployees.isNotEmpty()) {
                item {
                    Text(
                        text = "Employees (${matchedEmployees.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = StoreAmber)
                    )
                }
                items(matchedEmployees) { emp ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEmployeeClicked(emp.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(StoreNavyPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(emp.fullName.take(1), color = StoreAmber, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(emp.fullName, fontWeight = FontWeight.Bold)
                                Text("${emp.empCode} • ${emp.department} • Phone: ${emp.phone}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (matchedTasks.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tasks (${matchedTasks.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = StoreAmber)
                    )
                }
                items(matchedTasks) { task ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Assignment, contentDescription = null, tint = StoreAmber)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(task.title, fontWeight = FontWeight.Bold)
                                Text("Due: ${task.dueDate} • Priority: ${task.priority}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}
