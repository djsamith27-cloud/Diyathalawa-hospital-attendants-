package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AttendanceStatus
import com.example.data.model.EmployeeUtils
import com.example.data.model.ShiftMode
import com.example.ui.AttendanceRowUi
import com.example.ui.AttendanceUiState
import com.example.ui.components.GlassCard
import com.example.ui.components.IsoDatePickerDialog
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAttendanceColors

@Composable
fun AttendanceScreen(
    uiState: AttendanceUiState,
    onSelectDate: (String) -> Unit,
    onShiftDateByDays: (Long) -> Unit,
    onSetShiftMode: (ShiftMode) -> Unit,
    onSetEmployeeStatus: (String, AttendanceStatus) -> Unit,
    onMarkAll: (AttendanceStatus) -> Unit,
    onConfirmToggleLock: (Boolean) -> Unit,
    onDialPhone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalAttendanceColors.current
    var showDatePicker by remember { mutableStateOf(false) }
    var showLockConfirmDialog by remember { mutableStateOf(false) }

    if (showDatePicker) {
        IsoDatePickerDialog(
            initialIsoDate = uiState.selectedDate,
            onDismiss = { showDatePicker = false },
            onDateSelected = { picked ->
                showDatePicker = false
                onSelectDate(picked)
            }
        )
    }

    if (showLockConfirmDialog) {
        val locking = !uiState.isLocked
        AlertDialog(
            onDismissRequest = { showLockConfirmDialog = false },
            icon = {
                Icon(
                    imageVector = if (locking) Icons.Filled.Lock else Icons.Filled.LockOpen,
                    contentDescription = null,
                    tint = if (locking) semantic.absent else semantic.present
                )
            },
            title = {
                Text(
                    text = if (locking) "දිනය අගුළු දැමීම" else "අගුළු හැරීම",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = if (locking) {
                        "මෙම දිනයේ පැමිණීම් සටහන් අගුළු දැමීමට (Lock) අවශ්‍ය බව ස්ථිරද? අගුළු දැමූ පසු වෙනස් කළ නොහැක."
                    } else {
                        "මෙම දිනය නැවත අගුළු හැරීමට (Unlock) අවශ්‍ය බව ස්ථිරද?"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showLockConfirmDialog = false
                        onConfirmToggleLock(locking)
                    }
                ) {
                    Text(if (locking) "අගුළු දමන්න" else "අගුළු හරින්න")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLockConfirmDialog = false }) {
                    Text("අවලංගු කරන්න")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Controls Card: Date, Shift Switcher, Bulk Actions, Lock Toggle
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Date Selector Row
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "දිනය තෝරන්න:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { onShiftDateByDays(-1) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "පෙර දිනය",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { showDatePicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("attendance_date_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = uiState.selectedDate,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.CalendarToday,
                                        contentDescription = stringResource(id = R.string.cd_select_date),
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { onShiftDateByDays(1) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "ඊළඟ දිනය",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Shift Switcher
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "වැඩ මුරය (Shift):",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                ShiftPillButton(
                                    label = "දිවා (Day)",
                                    icon = Icons.Filled.WbSunny,
                                    selected = uiState.currentShift == ShiftMode.DAY,
                                    selectedColor = semantic.dayShift,
                                    onClick = { onSetShiftMode(ShiftMode.DAY) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("shift_day_button")
                                )
                                ShiftPillButton(
                                    label = "රාත්‍රි (Night)",
                                    icon = Icons.Filled.NightsStay,
                                    selected = uiState.currentShift == ShiftMode.NIGHT,
                                    selectedColor = semantic.nightShift,
                                    onClick = { onSetShiftMode(ShiftMode.NIGHT) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("shift_night_button")
                                )
                            }
                        }
                    }

                    // Bulk actions (only when unlocked) & Lock toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (!uiState.isLocked) {
                            Surface(
                                onClick = { onMarkAll(AttendanceStatus.PRESENT) },
                                shape = RoundedCornerShape(12.dp),
                                color = semantic.presentContainer,
                                border = BorderStroke(1.dp, semantic.present.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("mark_all_present_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.DoneAll,
                                        contentDescription = null,
                                        tint = semantic.onPresentContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "සියල්ල පැමිණ ඇත",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = semantic.onPresentContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            Surface(
                                onClick = { onMarkAll(AttendanceStatus.ABSENT) },
                                shape = RoundedCornerShape(12.dp),
                                color = semantic.absentContainer,
                                border = BorderStroke(1.dp, semantic.absent.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("mark_all_absent_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = null,
                                        tint = semantic.onAbsentContainer,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "සියල්ල නොපැමිණි",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = semantic.onAbsentContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Surface(
                            onClick = { showLockConfirmDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            color = if (uiState.isLocked) semantic.absent else semantic.present,
                            modifier = Modifier
                                .then(if (uiState.isLocked) Modifier.fillMaxWidth() else Modifier)
                                .minimumInteractiveComponentSize()
                                .testTag("lock_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (uiState.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (uiState.isLocked) {
                                        "අගුළු හැරීමට (Unlock)"
                                    } else {
                                        "අගුළු දමන්න (Lock)"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3-column Stats Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCounterCard(
                    label = "පැමිණි (Present)",
                    count = uiState.presentCount,
                    accentColor = semantic.present,
                    textColor = semantic.onPresentContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCounterCard(
                    label = "නොපැමිණි (Absent)",
                    count = uiState.absentCount,
                    accentColor = semantic.absent,
                    textColor = semantic.onAbsentContainer,
                    modifier = Modifier.weight(1f)
                )
                StatCounterCard(
                    label = "නිවාඩු (Leave)",
                    count = uiState.leaveCount,
                    accentColor = semantic.leave,
                    textColor = semantic.onLeaveContainer,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Attendance List Header Card
        item {
            GlassCard(contentPadding = PaddingValues(0.dp)) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(semantic.indigoContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            val isDay = uiState.currentShift == ShiftMode.DAY
                            Icon(
                                imageVector = if (isDay) Icons.Filled.WbSunny else Icons.Filled.NightsStay,
                                contentDescription = null,
                                tint = if (isDay) semantic.dayShift else semantic.nightShift,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isDay) "දිවා වැඩ මුරය" else "රාත්‍රි වැඩ මුරය",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (isDay) semantic.dayShift else semantic.nightShift,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (uiState.isLocked) semantic.absentContainer else semantic.presentContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (uiState.isLocked) Icons.Filled.Lock else Icons.Filled.LockOpen,
                                        contentDescription = null,
                                        tint = if (uiState.isLocked) semantic.onAbsentContainer else semantic.onPresentContainer,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (uiState.isLocked) "Locked" else "Unlocked",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (uiState.isLocked) semantic.onAbsentContainer else semantic.onPresentContainer
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(100.dp),
                                color = semantic.indigoContainer
                            ) {
                                Text(
                                    text = "පැමිණි: ${uiState.presentCount} | නොපැමිණි: ${uiState.absentCount}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = semantic.onIndigoContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    if (uiState.rows.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EventBusy,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "මෙම වැඩ මුරයට අදාළ සේවකයන් නොමැත.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Individual Employee Attendance Rows
        items(uiState.rows, key = { it.employee.id }) { row ->
            AttendanceEmployeeCard(
                row = row,
                isLocked = uiState.isLocked,
                onStatusSelected = { status -> onSetEmployeeStatus(row.employee.id, status) },
                onDialPhone = onDialPhone
            )
        }
    }
}

@Composable
private fun ShiftPillButton(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(10.dp),
        color = if (selected) selectedColor else Color.Transparent,
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier.minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatCounterCard(
    label: String,
    count: Int,
    accentColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    val semantic = LocalAttendanceColors.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = semantic.cardSurface,
        border = BorderStroke(1.dp, semantic.cardBorder),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(64.dp)
                    .background(accentColor)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = count.toString(),
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
        }
    }
}

@Composable
private fun AttendanceEmployeeCard(
    row: AttendanceRowUi,
    isLocked: Boolean,
    onStatusSelected: (AttendanceStatus) -> Unit,
    onDialPhone: (String) -> Unit
) {
    val semantic = LocalAttendanceColors.current
    val emp = row.employee

    val (badgeBg, badgeIconColor, badgeIcon) = when (row.status) {
        AttendanceStatus.PRESENT -> Triple(semantic.presentContainer, semantic.present, Icons.Filled.Check)
        AttendanceStatus.ABSENT -> Triple(semantic.absentContainer, semantic.absent, Icons.Filled.Close)
        AttendanceStatus.LEAVE -> Triple(semantic.leaveContainer, semantic.leave, Icons.Filled.Hotel)
    }

    GlassCard(contentPadding = PaddingValues(14.dp)) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = badgeBg,
                    border = BorderStroke(1.dp, badgeIconColor.copy(alpha = 0.4f)),
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = badgeIcon,
                            contentDescription = null,
                            tint = badgeIconColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = emp.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = semantic.indigoContainer
                        ) {
                            Text(
                                text = emp.empNo,
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = semantic.onIndigoContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = emp.role.ifBlank { "සේවක" },
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.present,
                            fontWeight = FontWeight.Medium
                        )
                        if (emp.phone.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable {
                                        onDialPhone(EmployeeUtils.extractDialPhone(emp.phone))
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Phone,
                                    contentDescription = stringResource(id = R.string.cd_call_employee),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = emp.phone,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusActionChip(
                    label = "පැමිණ ඇත",
                    icon = Icons.Filled.Check,
                    selected = row.status == AttendanceStatus.PRESENT,
                    activeColor = semantic.present,
                    enabled = !isLocked,
                    onClick = { onStatusSelected(AttendanceStatus.PRESENT) },
                    modifier = Modifier.weight(1f)
                )
                StatusActionChip(
                    label = "නොපැමිණි",
                    icon = Icons.Filled.Close,
                    selected = row.status == AttendanceStatus.ABSENT,
                    activeColor = semantic.absent,
                    enabled = !isLocked,
                    onClick = { onStatusSelected(AttendanceStatus.ABSENT) },
                    modifier = Modifier.weight(1f)
                )
                StatusActionChip(
                    label = "නිවාඩු",
                    icon = Icons.Filled.EventBusy,
                    selected = row.status == AttendanceStatus.LEAVE,
                    activeColor = semantic.leave,
                    enabled = !isLocked,
                    onClick = { onStatusSelected(AttendanceStatus.LEAVE) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusActionChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    activeColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        color = if (selected) activeColor else MaterialTheme.colorScheme.surface,
        border = if (selected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shadowElevation = if (selected) 2.dp else 0.dp,
        modifier = modifier
            .alpha(if (enabled) 1f else 0.55f)
            .minimumInteractiveComponentSize()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
