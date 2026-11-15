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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.EmployeeEntity
import com.example.data.model.EmployeeUtils
import com.example.ui.components.GlassCard
import com.example.ui.components.IsoDatePickerDialog
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalAttendanceColors
import java.time.LocalDate

@Composable
fun EmployeesScreen(
    employees: List<EmployeeEntity>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onSuggestNextEmpNo: () -> String,
    onSaveEmployee: (EmployeeEntity) -> Unit,
    onDeleteEmployee: (String) -> Unit,
    onExportEmployeesCsv: () -> Unit,
    onDialPhone: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalAttendanceColors.current
    var showFormDialog by remember { mutableStateOf(false) }
    var editingEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var profileEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }
    var deleteConfirmEmployee by remember { mutableStateOf<EmployeeEntity?>(null) }

    if (showFormDialog) {
        EmployeeFormDialog(
            existing = editingEmployee,
            suggestedEmpNo = remember(editingEmployee, employees) {
                editingEmployee?.empNo ?: onSuggestNextEmpNo()
            },
            onDismiss = {
                showFormDialog = false
                editingEmployee = null
            },
            onSave = { saved ->
                showFormDialog = false
                editingEmployee = null
                onSaveEmployee(saved)
            }
        )
    }

    profileEmployee?.let { emp ->
        EmployeeProfileDialog(
            employee = emp,
            onDismiss = { profileEmployee = null },
            onDialPhone = onDialPhone
        )
    }

    deleteConfirmEmployee?.let { emp ->
        AlertDialog(
            onDismissRequest = { deleteConfirmEmployee = null },
            title = {
                Text("සේවකයා ඉවත් කිරීම", style = MaterialTheme.typography.titleMedium)
            },
            text = {
                Text(
                    text = "${emp.name} (${emp.empNo}) ඉවත් කිරීමට අවශ්‍ය බව ස්ථිරද?",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteConfirmEmployee = null
                        onDeleteEmployee(emp.id)
                    }
                ) {
                    Text("ඉවත් කරන්න", color = semantic.absent)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmEmployee = null }) {
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
        // Search & Action Controls
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = {
                            Text(
                                text = "නම, අංකය හෝ NIC මඟින් සොයන්න...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = "Clear search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("employee_search_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                editingEmployee = null
                                showFormDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = semantic.present,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("add_employee_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "නව සේවකයෙක් එකතු කරන්න",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            onClick = onExportEmployeesCsv,
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("export_employees_csv_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Download,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "CSV",
                                    style = MaterialTheme.typography.labelLarge
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section Header
        item {
            GlassCard(contentPadding = PaddingValues(0.dp)) {
                Column {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(semantic.presentContainer.copy(alpha = 0.7f))
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "සේවක ලැයිස්තුව සහ පුද්ගලික තොරතුරු",
                            style = MaterialTheme.typography.titleSmall,
                            color = semantic.onPresentContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = semantic.presentContainer
                        ) {
                            Text(
                                text = "${employees.size}",
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = semantic.onPresentContainer,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (employees.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonSearch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                            Text(
                                text = "සේවකයන් හමු නොවීය.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Employee Cards
        items(employees, key = { it.id }) { emp ->
            EmployeeListItemCard(
                employee = emp,
                onViewProfile = { profileEmployee = emp },
                onDialPhone = onDialPhone,
                onEdit = {
                    editingEmployee = emp
                    showFormDialog = true
                },
                onDelete = { deleteConfirmEmployee = emp }
            )
        }
    }
}

@Composable
private fun EmployeeListItemCard(
    employee: EmployeeEntity,
    onViewProfile: () -> Unit,
    onDialPhone: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val semantic = LocalAttendanceColors.current
    val shiftTypeLabel = when (employee.shiftType) {
        "day" -> "දිවා පමණයි"
        "night" -> "රාත්‍රි පමණයි"
        else -> "දිවා/රාත්‍රි දෙකම"
    }

    GlassCard(contentPadding = PaddingValues(14.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onViewProfile),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = semantic.presentContainer,
                modifier = Modifier.size(42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = employee.name.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = semantic.onPresentContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = employee.name,
                        style = MaterialTheme.typography.titleSmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = semantic.presentContainer
                    ) {
                        Text(
                            text = employee.role.ifBlank { "සේවක" },
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.onPresentContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "අංකය: ${employee.empNo}",
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "| $shiftTypeLabel",
                        style = MaterialTheme.typography.bodySmall,
                        color = semantic.nightShift,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (employee.phone.isNotBlank()) {
                Surface(
                    onClick = { onDialPhone(EmployeeUtils.extractDialPhone(employee.phone)) },
                    shape = RoundedCornerShape(10.dp),
                    color = semantic.presentContainer,
                    border = BorderStroke(1.dp, semantic.present.copy(alpha = 0.35f)),
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Phone,
                            contentDescription = stringResource(id = R.string.cd_call_employee),
                            tint = semantic.onPresentContainer,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = employee.phone,
                            style = MaterialTheme.typography.labelSmall,
                            color = semantic.onPresentContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Surface(
                onClick = onEdit,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Box(
                    modifier = Modifier.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Edit,
                        contentDescription = stringResource(id = R.string.cd_edit_employee),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Surface(
                onClick = onDelete,
                shape = RoundedCornerShape(10.dp),
                color = semantic.absentContainer,
                border = BorderStroke(1.dp, semantic.absent.copy(alpha = 0.35f)),
                modifier = Modifier.minimumInteractiveComponentSize()
            ) {
                Box(
                    modifier = Modifier.padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = stringResource(id = R.string.cd_delete_employee),
                        tint = semantic.onAbsentContainer,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EmployeeFormDialog(
    existing: EmployeeEntity?,
    suggestedEmpNo: String,
    onDismiss: () -> Unit,
    onSave: (EmployeeEntity) -> Unit
) {
    val semantic = LocalAttendanceColors.current
    val defaultJoinDate = remember {
        LocalDate.now().withDayOfMonth(1).toString()
    }

    var name by remember { mutableStateOf(existing?.name ?: "") }
    var empNo by remember { mutableStateOf(existing?.empNo ?: suggestedEmpNo) }
    var role by remember { mutableStateOf(existing?.role ?: "") }
    var nic by remember { mutableStateOf(existing?.nic ?: "") }
    var shiftType by remember { mutableStateOf(existing?.shiftType?.ifBlank { "both" } ?: "both") }
    var joinDate by remember { mutableStateOf(existing?.joinDate?.ifBlank { defaultJoinDate } ?: defaultJoinDate) }
    var phone by remember { mutableStateOf(existing?.phone ?: "") }
    var emergency by remember { mutableStateOf(existing?.emergency ?: "") }
    var blood by remember { mutableStateOf(existing?.blood ?: "") }
    var dob by remember { mutableStateOf(existing?.dob ?: "") }
    var address by remember { mutableStateOf(existing?.address ?: "") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var shiftDropdownExpanded by remember { mutableStateOf(false) }
    var bloodDropdownExpanded by remember { mutableStateOf(false) }
    var pickJoinDate by remember { mutableStateOf(false) }
    var pickDobDate by remember { mutableStateOf(false) }

    if (pickJoinDate) {
        IsoDatePickerDialog(
            initialIsoDate = joinDate.ifBlank { defaultJoinDate },
            onDismiss = { pickJoinDate = false },
            onDateSelected = {
                joinDate = it
                pickJoinDate = false
            }
        )
    }

    if (pickDobDate) {
        IsoDatePickerDialog(
            initialIsoDate = dob.ifBlank { "1990-01-01" },
            onDismiss = { pickDobDate = false },
            onDateSelected = {
                dob = it
                pickDobDate = false
            }
        )
    }

    val shiftOptions = listOf(
        "both" to "දිවා සහ රාත්‍රි (Both)",
        "day" to "දිවා සේවය පමණයි",
        "night" to "රාත්‍රි සේවය පමණයි"
    )
    val bloodOptions = listOf("", "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(semantic.presentContainer)
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (existing != null) "සේවක තොරතුරු සංස්කරණය" else "නව සේවකයෙක් එකතු කරන්න",
                        style = MaterialTheme.typography.titleMedium,
                        color = semantic.onPresentContainer
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(id = R.string.cd_close),
                            tint = semantic.onPresentContainer
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (validationError != null) {
                        Surface(
                            color = semantic.absentContainer,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = validationError!!,
                                style = MaterialTheme.typography.bodySmall,
                                color = semantic.onAbsentContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("සම්පූර්ණ නම *") },
                        placeholder = { Text("උදා: එච්. එම්. පෙරේරා") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = empNo,
                        onValueChange = { empNo = it },
                        label = { Text("සේවක අංකය *") },
                        placeholder = { Text("උදා: EMP001") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = role,
                        onValueChange = { role = it },
                        label = { Text("තනතුර") },
                        placeholder = { Text("උදා: වැඩ පරීක්ෂක") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = nic,
                        onValueChange = { nic = it },
                        label = { Text("ජාතික හැඳුනුම්පත් අංකය (NIC)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Shift Type Dropdown
                    ExposedDropdownMenuBox(
                        expanded = shiftDropdownExpanded,
                        onExpandedChange = { shiftDropdownExpanded = !shiftDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = shiftOptions.firstOrNull { it.first == shiftType }?.second ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("සේවය කරන වැඩ මුරය *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = shiftDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = shiftDropdownExpanded,
                            onDismissRequest = { shiftDropdownExpanded = false }
                        ) {
                            shiftOptions.forEach { (key, label) ->
                                DropdownMenuItem(
                                    text = { Text(label) },
                                    onClick = {
                                        shiftType = key
                                        shiftDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Join Date Picker field
                    OutlinedTextField(
                        value = joinDate,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("සේවයට බැඳුණු දිනය *") },
                        trailingIcon = {
                            IconButton(onClick = { pickJoinDate = true }) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(id = R.string.cd_select_date))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickJoinDate = true }
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("දුරකථන අංකය") },
                        placeholder = { Text("07XXXXXXXX") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = emergency,
                        onValueChange = { emergency = it },
                        label = { Text("හදිසි ඇමතුම් අංකය") },
                        placeholder = { Text("උදා: බිරිඳ - 0712345678") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Blood Group Dropdown
                    ExposedDropdownMenuBox(
                        expanded = bloodDropdownExpanded,
                        onExpandedChange = { bloodDropdownExpanded = !bloodDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = blood.ifBlank { "තෝරන්න" },
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("රුධිර වර්ගය") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodDropdownExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        )
                        ExposedDropdownMenu(
                            expanded = bloodDropdownExpanded,
                            onDismissRequest = { bloodDropdownExpanded = false }
                        ) {
                            bloodOptions.forEach { bg ->
                                DropdownMenuItem(
                                    text = { Text(bg.ifBlank { "තෝරන්න" }) },
                                    onClick = {
                                        blood = bg
                                        bloodDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // DOB Picker field
                    OutlinedTextField(
                        value = dob,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("උපන් දිනය") },
                        trailingIcon = {
                            IconButton(onClick = { pickDobDate = true }) {
                                Icon(Icons.Filled.CalendarToday, contentDescription = stringResource(id = R.string.cd_select_date))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { pickDobDate = true }
                    )

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("පදිංචි ලිපිනය") },
                        minLines = 2,
                        maxLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                HorizontalDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("අවලංගු කරන්න")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val trimmedName = name.trim()
                            val trimmedNo = empNo.trim()
                            val trimmedJoin = joinDate.trim()
                            if (trimmedName.isEmpty() || trimmedNo.isEmpty() || trimmedJoin.isEmpty()) {
                                validationError = "කරුණාකර නම, අංකය සහ බැඳුණු දිනය ඇතුළත් කරන්න."
                            } else {
                                onSave(
                                    EmployeeEntity(
                                        id = existing?.id ?: System.currentTimeMillis().toString(),
                                        name = trimmedName,
                                        empNo = trimmedNo,
                                        role = role.trim(),
                                        shiftType = shiftType,
                                        joinDate = trimmedJoin,
                                        nic = nic.trim(),
                                        phone = phone.trim(),
                                        emergency = emergency.trim(),
                                        blood = blood,
                                        dob = dob.trim(),
                                        address = address.trim()
                                    )
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = semantic.present,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("සුරකින්න")
                    }
                }
            }
        }
    }
}

@Composable
private fun EmployeeProfileDialog(
    employee: EmployeeEntity,
    onDismiss: () -> Unit,
    onDialPhone: (String) -> Unit
) {
    val semantic = LocalAttendanceColors.current
    val shiftTypeText = when (employee.shiftType) {
        "day" -> "දිවා පමණයි"
        "night" -> "රාත්‍රි පමණයි"
        else -> "දිවා/රාත්‍රි දෙකම"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Badge,
                contentDescription = null,
                tint = semantic.present
            )
        },
        title = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "සේවක පැතිකඩ",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${employee.role.ifBlank { "සේවක" }} (${employee.empNo})",
                    style = MaterialTheme.typography.bodySmall,
                    color = semantic.present
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ProfileDetailRow("වැඩ මුරය:", shiftTypeText)
                ProfileDetailRow("බැඳුණු දිනය:", employee.joinDate.ifBlank { "-" })
                ProfileDetailRow("NIC:", employee.nic.ifBlank { "-" })
                ProfileDetailRow("දුරකථන:", employee.phone.ifBlank { "-" })
                ProfileDetailRow("හදිසි ඇමතුම්:", employee.emergency.ifBlank { "-" })
                ProfileDetailRow("රුධිර වර්ගය:", employee.blood.ifBlank { "-" })
                ProfileDetailRow("උපන් දිනය:", employee.dob.ifBlank { "-" })
                ProfileDetailRow("ලිපිනය:", employee.address.ifBlank { "-" })
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (employee.phone.isNotBlank()) {
                    TextButton(
                        onClick = { onDialPhone(EmployeeUtils.extractDialPhone(employee.phone)) }
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("අමතන්න")
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("වසන්න")
                }
            }
        }
    )
}

@Composable
private fun ProfileDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
