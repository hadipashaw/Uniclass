package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CourseClass
import com.example.data.model.UserRole
import com.example.util.ScheduleTextParser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartImportBottomSheet(
    currentRole: UserRole,
    sheetState: SheetState,
    onDismiss: () -> Unit,
    onImportConfirmed: (List<CourseClass>) -> Unit
) {
    var rawText by remember { mutableStateOf("") }
    val parsedCourses = remember { mutableStateListOf<CourseClass>() }
    val selectedIndices = remember { mutableStateListOf<Int>() }
    var importRole by remember { mutableStateOf(currentRole) }

    fun runParse(text: String, role: UserRole) {
        val result = ScheduleTextParser.parseScheduleText(text, role)
        parsedCourses.clear()
        selectedIndices.clear()
        parsedCourses.addAll(result)
        for (i in result.indices) {
            selectedIndices.add(i)
        }
    }

    val availablePresets = if (importRole == UserRole.PROFESSOR) {
        ScheduleTextParser.PROFESSOR_PRESETS
    } else {
        ScheduleTextParser.STUDENT_PRESETS
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("smart_import_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "ورود هوشمند برنامه دانشگاهی",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "متن سامانه، گلستان، پیام‌رسان یا سرفصل‌های تدریس را پیست کنید",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "بستن")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Role selection for imported classes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "افزودن به پنل:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.outline
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = importRole == UserRole.STUDENT,
                        onClick = {
                            importRole = UserRole.STUDENT
                            if (rawText.isNotBlank()) runParse(rawText, UserRole.STUDENT)
                        },
                        label = { Text("دانشجو 🎓", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = importRole == UserRole.PROFESSOR,
                        onClick = {
                            importRole = UserRole.PROFESSOR
                            if (rawText.isNotBlank()) runParse(rawText, UserRole.PROFESSOR)
                        },
                        label = { Text("استاد 👨‍🏫", fontSize = 11.sp) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Presets row
            Text(
                text = "یا یک برنامه نمونه آماده را امتحان کنید:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(availablePresets) { preset ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            rawText = preset.sampleText
                            importRole = preset.role
                            runParse(preset.sampleText, preset.role)
                        },
                        label = { Text(preset.title, fontSize = 11.sp) },
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Input Field
            OutlinedTextField(
                value = rawText,
                onValueChange = {
                    rawText = it
                    if (it.length > 5) runParse(it, importRole)
                },
                placeholder = {
                    Text(
                        if (importRole == UserRole.PROFESSOR)
                            "متن برنامه تدریس یا جلسات خود را پیست کنید...\nمثال:\nریاضی مهندسی - شنبه و دوشنبه ۰۸:۰۰ تا ۱۰:۰۰ - دانشکده فنی، طبقه ۳، کلاس ۳۰۴ - نماینده: رضایی ۰۹۱۲۳۴۵۶۷۸۹"
                        else
                            "متن برنامه کلاسی خود را پیست کنید...\nمثال:\nریاضی ۱ - شنبه و دوشنبه ۰۸:۰۰ تا ۱۰:۰۰ - کلاس ۳۰۲ - دکتر رضایی\nفیزیک ۲ - یکشنبه ۱۰:۰۰ تا ۱۲:۰۰ - کلاس ۱۰۱",
                        fontSize = 12.sp
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .testTag("import_text_input"),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Parse Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (parsedCourses.isNotEmpty()) "${parsedCourses.size} کلاس شناسایی شد" else "",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )

                Button(
                    onClick = { runParse(rawText, importRole) },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("analyze_schedule_button")
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("تحلیل و استخراج", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Parsed List Preview
            if (parsedCourses.isNotEmpty()) {
                Text(
                    text = "کلاس‌های شناسایی شده (قابل تیک زدن):",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(parsedCourses.indices.toList()) { index ->
                        val item = parsedCourses[index]
                        val isChecked = selectedIndices.contains(index)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("parsed_item_$index"),
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isChecked) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isChecked,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedIndices.add(index)
                                        else selectedIndices.remove(index)
                                    }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.courseName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "${item.dayPersian.faName} | ${item.formattedTimeRange}" +
                                                (if (item.displayLocation.isNotBlank()) " | ${item.displayLocation}" else "") +
                                                (if (item.classRepContact.isNotBlank()) " | نماینده: ${item.classRepContact}" else "") +
                                                (if (item.instructor.isNotBlank()) " | ${item.instructor}" else ""),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Bottom Import Button
                Button(
                    onClick = {
                        val toImport = selectedIndices.map { parsedCourses[it] }
                        onImportConfirmed(toImport)
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("confirm_import_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = selectedIndices.isNotEmpty()
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("افزودن ${selectedIndices.size} کلاس به پنل ${importRole.faTitle}")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
