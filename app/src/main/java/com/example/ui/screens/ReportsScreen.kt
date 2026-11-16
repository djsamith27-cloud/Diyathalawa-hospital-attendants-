package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ShiftMode
import com.example.ui.MonthlyReportUiState
import com.example.ui.ReportCellState
import com.example.ui.ReportEmployeeRow
import com.example.ui.components.GlassCard
import com.example.ui.components.MonthYearPickerDialog
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAttendanceColors

@Composable
fun ReportsScreen(
    uiState: MonthlyReportUiState,
    onSelectMonth: (String) -> Unit,
    onShiftMonth: (Long) -> Unit,
    onExportCsv: (ShiftMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalAttendanceColors.current
    var showMonthPicker by remember { mutableStateOf(false) }

    if (showMonthPicker) {
        MonthYearPickerDialog(
            initialMonthStr = uiState.monthStr,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { picked ->
                showMonthPicker = false
                onSelectMonth(picked)
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Month Selector & CSV Export Controls
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "මාසය තෝරන්න:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                onClick = { onShiftMonth(-1) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                        contentDescription = "පෙර මාසය",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { showMonthPicker = true },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.onSurface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("report_month_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = uiState.monthStr,
                                        fontFamily = JetBrainsMonoFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Icon(
                                        imageVector = Icons.Filled.CalendarMonth,
                                        contentDescription = null,
                                        tint = semantic.nightShift,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Surface(
                                onClick = { onShiftMonth(1) },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.minimumInteractiveComponentSize()
                            ) {
                                Box(
                                    modifier = Modifier.padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                        contentDescription = "ඊළඟ මාසය",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onExportCsv(ShiftMode.DAY) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = semantic.dayShiftSolid,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("export_day_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "දිවා වාර්තාව CSV",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = { onExportCsv(ShiftMode.NIGHT) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = semantic.nightShiftSolid,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("export_night_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Download,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "රාත්‍රි වාර්තාව CSV",
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        // Legend Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "සංකේත:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "✔ පැමිණි (P)",
                    style = MaterialTheme.typography.labelMedium,
                    color = semantic.present,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "✘ නොපැමිණි (A)",
                    style = MaterialTheme.typography.labelMedium,
                    color = semantic.absent,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "L නිවාඩු",
                    style = MaterialTheme.typography.labelMedium,
                    color = semantic.leave,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Day Shift Monthly Report Table
        item {
            MonthlyShiftMatrixCard(
                title = "දිවා සේවා මාසික වාර්තාව (Day Shift)",
                icon = Icons.Filled.WbSunny,
                accentColor = semantic.dayShift,
                headerBgColor = semantic.dayShiftContainer,
                headerTextColor = semantic.onDayShiftContainer,
                daysInMonth = uiState.daysInMonth,
                rows = uiState.dayShiftRows
            )
        }

        // Night Shift Monthly Report Table
        item {
            MonthlyShiftMatrixCard(
                title = "රාත්‍රි සේවා මාසික වාර්තාව (Night Shift)",
                icon = Icons.Filled.NightsStay,
                accentColor = semantic.nightShift,
                headerBgColor = semantic.nightShiftContainer,
                headerTextColor = semantic.onNightShiftContainer,
                daysInMonth = uiState.daysInMonth,
                rows = uiState.nightShiftRows
            )
        }
    }
}

@Composable
private fun MonthlyShiftMatrixCard(
    title: String,
    icon: ImageVector,
    accentColor: Color,
    headerBgColor: Color,
    headerTextColor: Color,
    daysInMonth: Int,
    rows: List<ReportEmployeeRow>
) {
    val semantic = LocalAttendanceColors.current
    val horizontalScrollState = rememberScrollState()

    GlassCard(contentPadding = PaddingValues(0.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(accentColor)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerBgColor.copy(alpha = 0.75f))
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = headerTextColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = headerTextColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (rows.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "සේවක දත්ත නොමැත.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Pinned left columns (EmpNo + Name) + scrollable day columns
                Row(modifier = Modifier.fillMaxWidth()) {
                    // Left Pinned Column
                    Column(
                        modifier = Modifier
                            .width(172.dp)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                                .background(headerBgColor.copy(alpha = 0.45f))
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "අංකය",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor,
                                modifier = Modifier.width(58.dp)
                            )
                            Text(
                                text = "සේවකයා",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = headerTextColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        rows.forEach { row ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(36.dp)
                                    .padding(horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = row.employee.empNo,
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.width(58.dp)
                                )
                                Text(
                                    text = row.employee.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        }
                    }

                    // Scrollable Days Columns
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .horizontalScroll(horizontalScrollState)
                    ) {
                        Row(
                            modifier = Modifier
                                .height(36.dp)
                                .background(headerBgColor.copy(alpha = 0.45f)),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            for (d in 1..daysInMonth) {
                                Text(
                                    text = d.toString(),
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = headerTextColor,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.width(30.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                        rows.forEach { row ->
                            Row(
                                modifier = Modifier.height(36.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                row.dayCells.forEach { cell ->
                                    val (symbol, cellTextColor, cellBgColor) = when (cell) {
                                        ReportCellState.PRESENT -> Triple(
                                            "✔",
                                            semantic.present,
                                            semantic.presentContainer.copy(alpha = 0.65f)
                                        )
                                        ReportCellState.ABSENT -> Triple(
                                            "✘",
                                            semantic.absent,
                                            semantic.absentContainer.copy(alpha = 0.65f)
                                        )
                                        ReportCellState.LEAVE -> Triple(
                                            "L",
                                            semantic.leave,
                                            semantic.leaveContainer.copy(alpha = 0.65f)
                                        )
                                        ReportCellState.UNRECORDED -> Triple(
                                            "-",
                                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.70f),
                                            Color.Transparent
                                        )
                                        ReportCellState.BEFORE_JOIN -> Triple(
                                            "",
                                            Color.Transparent,
                                            Color.Transparent
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .width(30.dp)
                                            .height(30.dp)
                                            .padding(2.dp)
                                            .background(cellBgColor, RoundedCornerShape(6.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = symbol,
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = cellTextColor,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))
                        }
                    }
                }
            }
        }
    }
}
