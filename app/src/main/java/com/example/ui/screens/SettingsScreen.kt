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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.clip
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.components.ConfirmationDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.StatusAbsentRed
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
    viewModel: StoreViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val currentRole by viewModel.currentUserRole.collectAsState()
    val auditLogs by viewModel.recentAuditLogs.collectAsState()

    var showBackupDialog by remember { mutableStateOf(false) }
    var backupJsonStr by remember { mutableStateOf("") }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var restoreJsonInput by remember { mutableStateOf("") }
    var showClearSampleConfirm by remember { mutableStateOf(false) }
    var showResetStoreConfirm by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // App Info Header
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = StoreNavyPrimary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Vijay General Store",
                        style = MaterialTheme.typography.titleLarge.copy(color = StoreAmber, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "Employee Management System • Version 1.0",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Designed for simple, reliable shop operation with offline-first persistence.",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.White.copy(alpha = 0.6f), fontSize = 11.sp)
                    )
                }
            }
        }

        // Active Role Switcher
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = StoreAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Active User Role & Access Mode", fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Switch roles to test access control. Sensitive data (salary, bank details) is masked for unauthorized roles.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        UserRole.values().forEach { role ->
                            FilterChip(
                                selected = currentRole == role,
                                onClick = { viewModel.setCurrentUserRole(role) },
                                label = { Text(role.displayName, fontSize = 11.sp) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Backup & Restore
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Backup, contentDescription = null, tint = StoreAmber)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Data Backup & Restore", fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Backup all store data to a JSON format or restore from an existing backup payload.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    backupJsonStr = viewModel.repository.exportDataAsJson()
                                    showBackupDialog = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp), tint = StoreAmber)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup Data")
                        }

                        OutlinedButton(
                            onClick = { showRestoreDialog = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore Data")
                        }
                    }
                }
            }
        }

        // Sample Data & Database Management
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Store Ledger & Demo Controls", fontWeight = FontWeight.Bold)
                    Text(
                        text = "Easily clear seeded demo employees or reload fresh store setup.",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showClearSampleConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Demo Data", fontSize = 11.sp)
                        }

                        OutlinedButton(
                            onClick = { showResetStoreConfirm = true },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Sample Data", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        // Audit Trail Logs
        item {
            SectionHeader(
                title = "Security & Audit Trail",
                subtitle = "History of recent administrative operations"
            )
        }

        items(auditLogs.take(10)) { log ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = StoreAmber, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(log.action, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            Text(log.userRole, fontSize = 10.sp, color = StoreAmber, fontWeight = FontWeight.Bold)
                        }
                        Text(log.details, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val logDate = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(log.timestamp))
                        Text(logDate, fontSize = 10.sp, color = Color.Gray)
                    }
                }
            }
        }
    }

    // Backup Dialog
    if (showBackupDialog) {
        AlertDialog(
            onDismissRequest = { showBackupDialog = false },
            title = { Text("Backup Data (JSON)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Copy or save this JSON payload to safely backup your store data.")
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        Text(
                            text = backupJsonStr,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("Store Backup JSON", backupJsonStr))
                        viewModel.showMessage("Backup copied to clipboard!")
                        showBackupDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy to Clipboard")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showBackupDialog = false }) { Text("Close") }
            }
        )
    }

    // Restore Dialog
    if (showRestoreDialog) {
        AlertDialog(
            onDismissRequest = { showRestoreDialog = false },
            title = { Text("Restore Store Data", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste your backup JSON payload below:")
                    OutlinedTextField(
                        value = restoreJsonInput,
                        onValueChange = { restoreJsonInput = it },
                        placeholder = { Text("{\n  \"appName\": \"Vijay General Store\"...\n}") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val success = viewModel.repository.importDataFromJson(restoreJsonInput)
                            if (success) {
                                viewModel.showMessage("Data restored successfully!")
                            } else {
                                viewModel.showMessage("Failed to parse JSON backup payload.")
                            }
                            showRestoreDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StoreNavyPrimary)
                ) {
                    Text("Import & Restore")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showRestoreDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Confirmation dialogs
    if (showClearSampleConfirm) {
        ConfirmationDialog(
            title = "Clear Demo Employees?",
            message = "This will remove the 4 sample employees (Ramesh, Priya, Suresh, Anita) and their demo records.",
            confirmText = "Clear Demo Data",
            onConfirm = {
                viewModel.clearSampleData()
                showClearSampleConfirm = false
            },
            onDismiss = { showClearSampleConfirm = false }
        )
    }

    if (showResetStoreConfirm) {
        ConfirmationDialog(
            title = "Reset Store Sample Data?",
            message = "This will replace current data with a clean set of Vijay General Store staff, attendance, and tasks.",
            confirmText = "Reset",
            onConfirm = {
                viewModel.resetStoreData()
                showResetStoreConfirm = false
            },
            onDismiss = { showResetStoreConfirm = false }
        )
    }
}
