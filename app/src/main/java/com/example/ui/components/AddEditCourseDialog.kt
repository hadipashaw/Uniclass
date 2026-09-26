package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Topic
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.DayOfWeekPersian
import com.example.data.model.CourseClass
import com.example.data.model.UserRole
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditCourseDialog(
    initialCourse: CourseClass?,
    initialDay: DayOfWeekPersian,
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onSave: (CourseClass) -> Unit
) {
    var selectedRole by remember {
        mutableStateOf(
            if (initialCourse != null) UserRole.fromId(initialCourse.roleType) else currentRole
        )
    }

    var courseName by remember { mutableStateOf(initialCourse?.courseName ?: "") }
    var instructor by remember { mutableStateOf(initialCourse?.instructor ?: "") }
    var location by remember { mutableStateOf(initialCourse?.location ?: "") }
    var building by remember { mutableStateOf(initialCourse?.building ?: "") }
    var floor by remember { mutableStateOf(initialCourse?.floor ?: "") }
    var roomNumber by remember { mutableStateOf(initialCourse?.roomNumber ?: "") }
    var classRep by remember { mutableStateOf(initialCourse?.classRepContact ?: "") }
    var topicToday by remember { mutableStateOf(initialCourse?.topicToday ?: "") }

    var selectedDay by remember {
        mutableStateOf(
            if (initialCourse != null) DayOfWeekPersian.fromDayIndex(initialCourse.dayOfWeek)
            else initialDay
        )
    }
    var startHour by remember { mutableIntStateOf(initialCourse?.startHour ?: 8) }
    var startMinute by remember { mutableIntStateOf(initialCourse?.startMinute ?: 0) }
    var endHour by remember { mutableIntStateOf(initialCourse?.endHour ?: 10) }
    var endMinute by remember { mutableIntStateOf(initialCourse?.endMinute ?: 0) }

    var remindMinutes by remember {
        mutableIntStateOf(initialCourse?.remindMinutesBefore ?: if (selectedRole == UserRole.PROFESSOR) 20 else 15)
    }
    var selectedColor by remember {
        mutableStateOf(initialCourse?.colorHex ?: if (selectedRole == UserRole.PROFESSOR) "#7C3AED" else "#2563EB")
    }
    var notes by remember { mutableStateOf(initialCourse?.notes ?: "") }
    var nameError by remember { mutableStateOf(false) }

    val colors = listOf(
        "#2563EB", "#059669", "#D97706", "#7C3AED",
        "#E11D48", "#0D9488", "#EA580C", "#4F46E5"
    )

    val reminderOptions = listOf(5, 10, 15, 20, 30, 45, 60)
    val isProfessor = selectedRole == UserRole.PROFESSOR

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("add_edit_course_dialog"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header & Role Switcher
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (initialCourse == null) "افزودن کلاس جدید" else "ویرایش کلاس",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    // Role Chip Toggle
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = selectedRole == UserRole.STUDENT,
                            onClick = { selectedRole = UserRole.STUDENT },
                            label = { Text("دانشجو 🎓", fontSize = 11.sp) }
                        )
                        FilterChip(
                            selected = selectedRole == UserRole.PROFESSOR,
                            onClick = { selectedRole = UserRole.PROFESSOR },
                            label = { Text("استاد 👨‍🏫", fontSize = 11.sp) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Course Name
                OutlinedTextField(
                    value = courseName,
                    onValueChange = {
                        courseName = it
                        if (it.isNotBlank()) nameError = false
                    },
                    label = { Text(if (isProfessor) "نام درس تدریسی (ضروری)" else "نام درس (ضروری)") },
                    placeholder = { Text(if (isProfessor) "مثال: ریاضی مهندسی یا مدار ۱" else "مثال: ریاضی عمومی ۱") },
                    leadingIcon = { Icon(Icons.Default.Book, contentDescription = null) },
                    isError = nameError,
                    supportingText = { if (nameError) Text("نام درس نمی‌تواند خالی باشد") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_course_name"),
                    singleLine = true
                )

                if (isProfessor) {
                    // Professor Specific: Classroom & Building Navigation Fields
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = building,
                        onValueChange = { building = it },
                        label = { Text("ساختمان / دانشکده") },
                        placeholder = { Text("مثال: دانشکده فنی، ساختمان ابوریحان") },
                        leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = floor,
                            onValueChange = { floor = it },
                            label = { Text("طبقه") },
                            placeholder = { Text("مثال: ۲") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = roomNumber,
                            onValueChange = { roomNumber = it },
                            label = { Text("شماره کلاس / سایت") },
                            placeholder = { Text("مثال: ۳۰۴") },
                            leadingIcon = { Icon(Icons.Default.MeetingRoom, contentDescription = null) },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = classRep,
                        onValueChange = { classRep = it },
                        label = { Text("نام و شماره نماینده کلاس") },
                        placeholder = { Text("مثال: علی رضایی ۰۹۱۲۳۴۵۶۷۸۹") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = topicToday,
                        onValueChange = { topicToday = it },
                        label = { Text("سرفصل تدریس / موضوع امروز (اختیاری)") },
                        placeholder = { Text("مثال: تدریس فصل سوم، حل تمرین") },
                        leadingIcon = { Icon(Icons.Default.Topic, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    // Student Specific Fields
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = instructor,
                        onValueChange = { instructor = it },
                        label = { Text("نام استاد / مدرس") },
                        placeholder = { Text("مثال: دکتر رضایی") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_instructor"),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("محل برگزاری / شماره کلاس") },
                        placeholder = { Text("مثال: کلاس ۳۰۲ - دانشکده علوم") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_location"),
                        singleLine = true
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Day of Week
                Text(
                    text = "روز هفته:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (day in DayOfWeekPersian.entries) {
                        FilterChip(
                            selected = selectedDay == day,
                            onClick = { selectedDay = day },
                            label = { Text(day.faName, fontSize = 12.sp) },
                            modifier = Modifier.testTag("day_chip_${day.dayIndex}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Time Pickers (Start and End)
                Text(
                    text = "ساعت برگزاری کلاس:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Start Time Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "ساعت شروع", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", startHour, startMinute),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { startHour = (startHour + 1) % 24 },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { startHour = if (startHour > 0) startHour - 1 else 23 },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { startMinute = (startMinute + 15) % 60 },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(":15", fontSize = 9.sp)
                                }
                            }
                        }
                    }

                    Text("تا", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    // End Time Box
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(2.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(text = "ساعت پایان", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = String.format(Locale.getDefault(), "%02d:%02d", endHour, endMinute),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Button(
                                    onClick = { endHour = (endHour + 1) % 24 },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { endHour = if (endHour > 0) endHour - 1 else 23 },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(4.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("-", fontSize = 12.sp)
                                }
                                Button(
                                    onClick = { endMinute = (endMinute + 15) % 60 },
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(2.dp),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text(":15", fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Reminder Options
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isProfessor) "اعلان قبل از شروع کلاس تدریس:" else "هشدار قبل از شروع کلاس:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    for (mins in reminderOptions) {
                        FilterChip(
                            selected = remindMinutes == mins,
                            onClick = { remindMinutes = mins },
                            label = { Text("$mins دقیقه", fontSize = 12.sp) },
                            modifier = Modifier.testTag("reminder_chip_$mins")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Color Tag
                Text(
                    text = "رنگ برچسب درس:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    for (hex in colors) {
                        val colorObj = Color(android.graphics.Color.parseColor(hex))
                        val isSelected = selectedColor == hex
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(colorObj, CircleShape)
                                .clickable { selectedColor = hex }
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Optional Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(if (isProfessor) "یادداشت‌های تدریس و تکالیف دانشجوها" else "یادداشت یا تکالیف (اختیاری)") },
                    placeholder = { Text("توضیحات، پروژه‌ها یا لینک کلاس...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_notes"),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(
                        onClick = {
                            if (courseName.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val finalLocation = if (isProfessor) {
                                listOf(building, if (floor.isNotBlank()) "طبقه $floor" else "", if (roomNumber.isNotBlank()) "کلاس $roomNumber" else "")
                                    .filter { it.isNotBlank() }
                                    .joinToString("، ")
                            } else {
                                location
                            }

                            val updated = (initialCourse ?: CourseClass(
                                courseName = courseName.trim(),
                                dayOfWeek = selectedDay.dayIndex,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                roleType = selectedRole.id
                            )).copy(
                                courseName = courseName.trim(),
                                instructor = instructor.trim(),
                                location = finalLocation.ifBlank { location.trim() },
                                building = building.trim(),
                                floor = floor.trim(),
                                roomNumber = roomNumber.trim(),
                                classRepContact = classRep.trim(),
                                topicToday = topicToday.trim(),
                                dayOfWeek = selectedDay.dayIndex,
                                startHour = startHour,
                                startMinute = startMinute,
                                endHour = endHour,
                                endMinute = endMinute,
                                remindMinutesBefore = remindMinutes,
                                colorHex = selectedColor,
                                notes = notes.trim(),
                                isReminderEnabled = true,
                                roleType = selectedRole.id
                            )
                            onSave(updated)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("save_course_button")
                    ) {
                        Text(if (initialCourse == null) "ثبت کلاس" else "ذخیره تغییرات")
                    }
                }
            }
        }
    }
}
