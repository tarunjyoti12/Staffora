package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.data.model.UserRole
import com.example.ui.components.StoreTopBar
import com.example.ui.screens.AdvancesScreen
import com.example.ui.screens.AskBusinessScreen
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.EmployeesScreen
import com.example.ui.screens.HolidaysScreen
import com.example.ui.screens.LeavesScreen
import com.example.ui.screens.PayrollScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.TasksScreen
import com.example.ui.theme.StoreAmber
import com.example.ui.theme.StoreNavyPrimary
import com.example.ui.viewmodel.StoreViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Home", Icons.Default.Home)
    object Attendance : Screen("attendance", "Attendance", Icons.Default.CheckCircle)
    object Employees : Screen("employees", "Staff", Icons.Default.Group)
    object Payroll : Screen("payroll", "Payroll", Icons.Default.CurrencyRupee)
    object AskBusiness : Screen("ask_business", "Ask Assistant", Icons.Default.AutoAwesome)

    // Secondary screens accessible from quick actions or overflow menu
    object Leaves : Screen("leaves", "Leaves", Icons.Default.EventNote)
    object Advances : Screen("advances", "Advances", Icons.Default.Payment)
    object Tasks : Screen("tasks", "Tasks", Icons.Default.Assignment)
    object Holidays : Screen("holidays", "Holidays", Icons.Default.CalendarToday)
    object Reports : Screen("reports", "Reports", Icons.Default.Assessment)
    object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    object Search : Screen("search", "Search", Icons.Default.Search)
}

val primaryNavItems = listOf(
    Screen.Dashboard,
    Screen.Attendance,
    Screen.Employees,
    Screen.Payroll,
    Screen.AskBusiness
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: StoreViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

    val userRole by viewModel.currentUserRole.collectAsState()
    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var moreMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isWideScreen = maxWidth >= 640.dp

        Row(modifier = Modifier.fillMaxSize()) {
            // Adaptive Navigation Rail for Tablets / Wide screens
            if (isWideScreen && currentRoute != Screen.Search.route) {
                NavigationRail(
                    containerColor = StoreNavyPrimary,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Spacer(modifier = Modifier.height(16.dp))
                    primaryNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationRailItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }
            }

            // Main Content Area
            Scaffold(
                modifier = Modifier.weight(1f),
                topBar = {
                    if (currentRoute == Screen.Search.route) {
                        // Handled internally in SearchScreen
                    } else if (primaryNavItems.any { it.route == currentRoute }) {
                        StoreTopBar(
                            title = "Vijay General Store",
                            subtitle = "Employee Management System",
                            currentRole = userRole,
                            onRoleSelected = { viewModel.setCurrentUserRole(it) },
                            onSearchClicked = { navController.navigate(Screen.Search.route) }
                        )
                    } else {
                        // Secondary screen top bar with Back Button
                        val title = when (currentRoute) {
                            Screen.Leaves.route -> "Staff Leaves & Holidays"
                            Screen.Advances.route -> "Staff Loans & Advances"
                            Screen.Tasks.route -> "Daily Store Duties & Tasks"
                            Screen.Holidays.route -> "Store Holiday Calendar"
                            Screen.Reports.route -> "Store Reports & Register"
                            Screen.Settings.route -> "Store Settings & Backup"
                            else -> "Vijay General Store"
                        }
                        TopAppBar(
                            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                            navigationIcon = {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                                    Icon(Icons.Default.Search, contentDescription = "Search")
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            )
                        )
                    }
                },
                bottomBar = {
                    if (!isWideScreen && currentRoute != Screen.Search.route) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 8.dp
                        ) {
                            primaryNavItems.forEach { screen ->
                                val selected = currentRoute == screen.route
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        navController.navigate(screen.route) {
                                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                    icon = { Icon(screen.icon, contentDescription = screen.title) },
                                    label = { Text(screen.title, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = StoreNavyPrimary,
                                        selectedTextColor = StoreNavyPrimary,
                                        indicatorColor = StoreAmber.copy(alpha = 0.25f),
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                },
                snackbarHost = { SnackbarHost(snackbarHostState) }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(Screen.Dashboard.route) {
                        DashboardScreen(
                            viewModel = viewModel,
                            onNavigateToAttendance = { navController.navigate(Screen.Attendance.route) },
                            onNavigateToEmployees = { navController.navigate(Screen.Employees.route) },
                            onNavigateToLeaves = { navController.navigate(Screen.Leaves.route) },
                            onNavigateToPayroll = { navController.navigate(Screen.Payroll.route) },
                            onNavigateToAdvances = { navController.navigate(Screen.Advances.route) },
                            onNavigateToTasks = { navController.navigate(Screen.Tasks.route) },
                            onNavigateToHolidays = { navController.navigate(Screen.Holidays.route) },
                            onAddEmployeeClicked = { navController.navigate(Screen.Employees.route) },
                            onAddLeaveClicked = { navController.navigate(Screen.Leaves.route) },
                            onRecordAdvanceClicked = { navController.navigate(Screen.Advances.route) }
                        )
                    }

                    composable(Screen.Attendance.route) {
                        AttendanceScreen(viewModel = viewModel)
                    }

                    composable(Screen.Employees.route) {
                        EmployeesScreen(viewModel = viewModel)
                    }

                    composable(Screen.Payroll.route) {
                        PayrollScreen(viewModel = viewModel)
                    }

                    composable(Screen.AskBusiness.route) {
                        AskBusinessScreen(viewModel = viewModel)
                    }

                    composable(Screen.Leaves.route) {
                        LeavesScreen(viewModel = viewModel)
                    }

                    composable(Screen.Advances.route) {
                        AdvancesScreen(viewModel = viewModel)
                    }

                    composable(Screen.Tasks.route) {
                        TasksScreen(viewModel = viewModel)
                    }

                    composable(Screen.Holidays.route) {
                        HolidaysScreen(viewModel = viewModel)
                    }

                    composable(Screen.Reports.route) {
                        ReportsScreen(viewModel = viewModel)
                    }

                    composable(Screen.Settings.route) {
                        SettingsScreen(viewModel = viewModel)
                    }

                    composable(Screen.Search.route) {
                        SearchScreen(
                            viewModel = viewModel,
                            onBackClicked = { navController.popBackStack() },
                            onEmployeeClicked = { _ ->
                                navController.navigate(Screen.Employees.route)
                            }
                        )
                    }
                }
            }
        }
    }
}
