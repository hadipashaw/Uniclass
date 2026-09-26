package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.DayOfWeekPersian
import com.example.data.model.CourseClass
import com.example.data.model.UserRole
import com.example.ui.MainViewModel
import com.example.ui.components.AddEditCourseDialog
import com.example.ui.components.CourseItemCard
import com.example.ui.components.NextClassBanner
import com.example.ui.components.SmartImportBottomSheet
import com.example.ui.components.WeeklyScheduleDialog
import com.example.util.ScheduleTextParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentRole by viewModel.currentRole.collectAsStateWithLifecycle()
    val allCourses by viewModel.allCourses.collectAsStateWithLifecycle()
    val coursesForActiveRole by viewModel.coursesForActiveRole.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val coursesForSelectedDay by viewModel.coursesForSelectedDay.collectAsStateWithLifecycle()
    val nextClassState by viewModel.nextClassState.collectAsStateWithLifecycle()
    val snackbarMsg by viewModel.snackbarMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingCourse by remember { mutableStateOf<CourseClass?>(null) }
    var showSmartImportSheet by remember { mutableStateOf(false) }
    var showWeeklyDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }

    val isProfessor = currentRole == UserRole.PROFESSOR

    // Notification Permission Handling
    var hasNotificationPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotificationPermission = isGranted
    }

    LaunchedEffect(snackbarMsg) {
        snackbarMsg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackbarMessage()
        }
    }

    Scaffold(
        modifier = modifier.testTag("dashboard_scaffold"),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "UniClass",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary
                            ) {
                                Text(
                                    text = currentRole.faTitle,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isProfessor)
                                "راهنمای محل کلاس و یادآور زمان تدریس استاد"
                            else
                                "مدیریت برنامه هفتگی و هشدارهای شروع کلاس",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    // Test notification
                    IconButton(
                        onClick = { viewModel.testNotification() },
                        modifier = Modifier.testTag("topbar_test_notification_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "آزمایش اعلان",
                            tint = if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Smart Import
                    IconButton(
                        onClick = { showSmartImportSheet = true },
                        modifier = Modifier.testTag("topbar_smart_import_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "ورود هوشمند برنامه",
                            tint = if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // Weekly Overview
                    IconButton(
                        onClick = { showWeeklyDialog = true },
                        modifier = Modifier.testTag("topbar_weekly_overview_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarViewWeek,
                            contentDescription = "نمای هفتگی",
                            tint = if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary
                        )
                    }

                    // More menu
                    IconButton(onClick = { showMenu = true }) {
                        Icon(imageVector = Icons.Default.MoreVert, contentDescription = "منو")
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("بارگذاری نمونه برنامه تدریس استاد 👨‍🏫") },
                            onClick = {
                                showMenu = false
                                viewModel.loadSamplePreset(ScheduleTextParser.PROFESSOR_PRESETS[0])
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("بارگذاری نمونه برنامه کلاسی دانشجو 🎓") },
                            onClick = {
                                showMenu = false
                                viewModel.loadSamplePreset(ScheduleTextParser.STUDENT_PRESETS[0])
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("ارسال اعلان آزمایشی 🔔") },
                            onClick = {
                                showMenu = false
                                viewModel.testNotification()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("پاک‌سازی کلاس‌های این پنل", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                viewModel.clearCoursesForCurrentRole()
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCourse = null
                    showAddEditDialog = true
                },
                containerColor = if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("add_class_fab")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "افزودن کلاس")
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isProfessor) "افزودن کلاس تدریس" else "افزودن درس جدید",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Role Switcher Segmented Bar
            val studentCount = allCourses.count { it.roleType.equals(UserRole.STUDENT.id, ignoreCase = true) }
            val professorCount = allCourses.count { it.roleType.equals(UserRole.PROFESSOR.id, ignoreCase = true) }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    // Student Tab Button
                    val isStudentSelected = currentRole == UserRole.STUDENT
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isStudentSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setRole(UserRole.STUDENT) }
                            .padding(vertical = 8.dp)
                            .testTag("role_switch_student"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🎓 پنل دانشجو",
                                fontWeight = if (isStudentSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isStudentSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            if (studentCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isStudentSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$studentCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isStudentSelected) Color.White else MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Professor Tab Button
                    val isProfSelected = currentRole == UserRole.PROFESSOR
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                color = if (isProfSelected) Color(0xFF7C3AED) else Color.Transparent,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.setRole(UserRole.PROFESSOR) }
                            .padding(vertical = 8.dp)
                            .testTag("role_switch_professor"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "👨‍🏫 پنل استاد / مدرس",
                                fontWeight = if (isProfSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isProfSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                fontSize = 13.sp
                            )
                            if (professorCount > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = CircleShape,
                                    color = if (isProfSelected) Color.White.copy(alpha = 0.25f) else MaterialTheme.colorScheme.surface,
                                    modifier = Modifier.size(18.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = "$professorCount",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isProfSelected) Color.White else Color(0xFF7C3AED)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Notification permission banner if missing
            AnimatedVisibility(visible = !hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .testTag("permission_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "دسترسی به اعلان‌ها لازم است",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFF92400E)
                            )
                            Text(
                                text = "برای اطلاع‌رسانی به موقع شروع کلاس‌ها، اجازه اعلان را تایید کنید.",
                                fontSize = 11.sp,
                                color = Color(0xFFB45309)
                            )
                        }
                        Button(
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("grant_permission_button")
                        ) {
                            Text("فعال‌سازی", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }

            // Next Class Countdown & Quick Test Alert Banner
            NextClassBanner(
                nextClassState = nextClassState,
                userRole = currentRole,
                onTestNotification = { viewModel.testNotification() },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Day Selector TabRow (Saturday to Friday)
            val todayDay = DayOfWeekPersian.today()
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedDay.dayIndex - 1,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (day in DayOfWeekPersian.entries) {
                    val count = coursesForActiveRole.count { it.dayOfWeek == day.dayIndex }
                    val isToday = day == todayDay
                    val isSelected = selectedDay == day

                    Tab(
                        selected = isSelected,
                        onClick = { viewModel.selectDay(day) },
                        modifier = Modifier.testTag("tab_day_${day.dayIndex}"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = day.faName,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                                if (count > 0) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isSelected) {
                                            if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant
                                        },
                                        modifier = Modifier.size(18.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$count",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                                if (isToday) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .background(Color(0xFF10B981), CircleShape)
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Courses for the selected day
            if (coursesForSelectedDay.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isProfessor) Color(0xFF7C3AED).copy(alpha = 0.15f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (isProfessor)
                                "کلاس تدریسی برای ${selectedDay.faName} ثبت نشده است"
                            else
                                "کلاسی برای ${selectedDay.faName} ثبت نشده است",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isProfessor)
                                "جلسات تدریس خود را با ذکر ساختمان، طبقه و شماره کلاس وارد کنید تا دیگر نگران گم کردن کلاس‌ها یا فراموشی ساعت تدریس نباشید."
                            else
                                "می‌توانید کلاس جدیدی دستی اضافه کنید یا متن برنامه دانشگاه را پیست کنید.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.outline,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(18.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = {
                                    editingCourse = null
                                    showAddEditDialog = true
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isProfessor) Color(0xFF7C3AED) else MaterialTheme.colorScheme.primary
                                ),
                                modifier = Modifier.testTag("empty_state_add_button")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isProfessor) "افزودن کلاس تدریس" else "افزودن کلاس")
                            }

                            Button(
                                onClick = { showSmartImportSheet = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("empty_state_import_button")
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("ورود هوشمند")
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(coursesForSelectedDay, key = { it.id }) { course ->
                        CourseItemCard(
                            course = course,
                            selectedDay = selectedDay,
                            userRole = currentRole,
                            onEdit = {
                                editingCourse = course
                                showAddEditDialog = true
                            },
                            onDelete = {
                                viewModel.deleteCourse(course)
                            },
                            onToggleReminder = { isChecked ->
                                viewModel.toggleReminder(course, isChecked)
                            }
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp)) // Space for FAB
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddEditDialog) {
        AddEditCourseDialog(
            initialCourse = editingCourse,
            initialDay = selectedDay,
            currentRole = currentRole,
            onDismiss = { showAddEditDialog = false },
            onSave = { updatedCourse ->
                if (editingCourse == null) {
                    viewModel.addCourse(updatedCourse)
                } else {
                    viewModel.updateCourse(updatedCourse)
                }
                showAddEditDialog = false
            }
        )
    }

    // Smart Import BottomSheet
    if (showSmartImportSheet) {
        SmartImportBottomSheet(
            currentRole = currentRole,
            sheetState = sheetState,
            onDismiss = { showSmartImportSheet = false },
            onImportConfirmed = { list ->
                viewModel.importParsedCourses(list)
            }
        )
    }

    // Weekly Overview Matrix Dialog
    if (showWeeklyDialog) {
        WeeklyScheduleDialog(
            allCourses = coursesForActiveRole,
            userRole = currentRole,
            onDismiss = { showWeeklyDialog = false }
        )
    }
}
