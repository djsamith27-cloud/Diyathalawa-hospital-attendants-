package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ShiftMode
import com.example.ui.AttendanceViewModel
import com.example.ui.components.AppTab
import com.example.ui.components.GlassHeader
import com.example.ui.components.PasswordPromptDialog
import com.example.ui.screens.AttendanceScreen
import com.example.ui.screens.EmployeesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.DiyathalawaAttendanceTheme
import com.example.ui.theme.LocalAttendanceColors
import com.example.util.WelcomeSoundPlayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class MainActivity : ComponentActivity() {
    private val viewModel: AttendanceViewModel by viewModels {
        AttendanceViewModel.Factory((application as AttendanceApplication).repository)
    }
    private lateinit var welcomeSoundPlayer: WelcomeSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        welcomeSoundPlayer = WelcomeSoundPlayer(applicationContext)
        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                if (viewModel.consumeStartupWelcomeSound()) {
                    welcomeSoundPlayer.playWelcomeBoss()
                }
            }
            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            android.graphics.Color.TRANSPARENT,
                            android.graphics.Color.TRANSPARENT
                        )
                    }
                )
            }
            DiyathalawaAttendanceTheme(darkTheme = isDarkTheme) {
                AttendanceAppShell(
                    viewModel = viewModel,
                    isDarkTheme = isDarkTheme,
                    onPlayWelcomeSound = { welcomeSoundPlayer.playWelcomeBoss() }
                )
            }
        }
    }

    override fun onDestroy() {
        if (::welcomeSoundPlayer.isInitialized) {
            welcomeSoundPlayer.release()
        }
        super.onDestroy()
    }
}

@Composable
private fun AttendanceAppShell(
    viewModel: AttendanceViewModel,
    isDarkTheme: Boolean,
    onPlayWelcomeSound: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeTab by rememberSaveable { mutableStateOf(AppTab.ATTENDANCE) }
    var pendingExportContent by remember { mutableStateOf<String?>(null) }
    var showPasswordPrompt by remember { mutableStateOf(false) }
    var pendingPasswordAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    val liveDateTime by viewModel.liveDateTimeText.collectAsStateWithLifecycle()
    val attendanceUiState by viewModel.attendanceUiState.collectAsStateWithLifecycle()
    val filteredEmployees by viewModel.filteredEmployees.collectAsStateWithLifecycle()
    val searchQuery by viewModel.employeeSearchQuery.collectAsStateWithLifecycle()
    val reportUiState by viewModel.monthlyReportState.collectAsStateWithLifecycle()
    val isPasswordEnabled by viewModel.isPasswordEnabled.collectAsStateWithLifecycle()
    val isAdminUnlocked by viewModel.isAdminUnlocked.collectAsStateWithLifecycle()
    val isWelcomeSoundEnabled by viewModel.isWelcomeSoundEnabled.collectAsStateWithLifecycle()

    val requestUnlockAndRun: (() -> Unit) -> Unit = { action ->
        if (isPasswordEnabled && !isAdminUnlocked) {
            pendingPasswordAction = action
            showPasswordPrompt = true
        } else {
            action()
        }
    }

    if (showPasswordPrompt) {
        PasswordPromptDialog(
            onDismiss = {
                showPasswordPrompt = false
                pendingPasswordAction = null
            },
            onVerify = { pwd -> viewModel.unlockWithPassword(pwd) },
            onVerified = {
                val action = pendingPasswordAction
                showPasswordPrompt = false
                pendingPasswordAction = null
                action?.invoke()
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.snackbarMessages.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    // Back press on secondary tabs returns to Attendance tab
    BackHandler(enabled = activeTab != AppTab.ATTENDANCE) {
        activeTab = AppTab.ATTENDANCE
    }

    val createJsonBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        val contentToWrite = pendingExportContent
        pendingExportContent = null
        if (uri != null && contentToWrite != null) {
            scope.launch {
                val success = writeTextToUri(context, uri, contentToWrite)
                viewModel.notifyMessage(
                    if (success) "Backup ගොනුව සාර්ථකව සුරකින ලදී!" else "Backup සුරැකීමේ දෝෂයකි."
                )
            }
        }
    }

    val createCsvLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val contentToWrite = pendingExportContent
        pendingExportContent = null
        if (uri != null && contentToWrite != null) {
            scope.launch {
                val success = writeTextToUri(context, uri, contentToWrite)
                viewModel.notifyMessage(
                    if (success) "CSV වාර්තාව සාර්ථකව සුරකින ලදී!" else "CSV සුරැකීමේ දෝෂයකි."
                )
            }
        }
    }

    val openJsonRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val raw = readTextFromUri(context, uri)
                if (raw != null) {
                    viewModel.restoreFromJson(raw)
                } else {
                    viewModel.notifyMessage("ගොනුව කියවීමේ දෝෂයක් ඇත.")
                }
            }
        }
    }

    val triggerBackup: () -> Unit = {
        scope.launch {
            pendingExportContent = viewModel.getBackupJson()
            createJsonBackupLauncher.launch("Attendance_Backup_${LocalDate.now()}.json")
        }
    }

    val triggerRestore: () -> Unit = {
        openJsonRestoreLauncher.launch(arrayOf("application/json", "text/plain", "*/*"))
    }

    val dialPhone: (String) -> Unit = { number ->
        if (number.isNotBlank()) {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
            runCatching { context.startActivity(intent) }
        }
    }

    val semantic = LocalAttendanceColors.current
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(semantic.bgTop, semantic.bgBottom)
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            containerColor = androidx.compose.ui.graphics.Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                GlassHeader(
                    activeTab = activeTab,
                    liveDateTime = liveDateTime,
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() },
                    onBackupClick = triggerBackup,
                    onRestoreClick = { requestUnlockAndRun(triggerRestore) }
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        windowInsets = WindowInsets.navigationBars,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        tonalElevation = 8.dp
                    ) {
                        AppTab.entries.forEach { tab ->
                            NavigationBarItem(
                                selected = activeTab == tab,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = stringResource(id = tab.navLabelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = tab.navLabelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        AppTab.entries.forEach { tab ->
                            NavigationRailItem(
                                selected = activeTab == tab,
                                onClick = { activeTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = stringResource(id = tab.navLabelRes)
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(id = tab.navLabelRes),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                modifier = Modifier.testTag(tab.testTag)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 840.dp)
                            .fillMaxHeight()
                    ) {
                        Crossfade(
                            targetState = activeTab,
                            animationSpec = tween(durationMillis = 220),
                            label = "tabCrossfade"
                        ) { tab ->
                            when (tab) {
                                AppTab.ATTENDANCE -> AttendanceScreen(
                                    uiState = attendanceUiState,
                                    isPasswordEnabled = isPasswordEnabled,
                                    isAdminUnlocked = isAdminUnlocked,
                                    onRequestUnlock = requestUnlockAndRun,
                                    onLockAdminSession = { viewModel.lockAdminSession() },
                                    onSelectDate = { viewModel.selectDate(it) },
                                    onShiftDateByDays = { viewModel.shiftDateByDays(it) },
                                    onSetShiftMode = { viewModel.setShiftMode(it) },
                                    onSetEmployeeStatus = { empId, status ->
                                        viewModel.setEmployeeStatus(empId, status)
                                    },
                                    onMarkAll = { status -> viewModel.markAllCurrentShift(status) },
                                    onConfirmToggleLock = { locked -> viewModel.setDateLocked(locked) },
                                    onDialPhone = dialPhone
                                )

                                AppTab.EMPLOYEES -> EmployeesScreen(
                                    employees = filteredEmployees,
                                    searchQuery = searchQuery,
                                    onSearchQueryChange = { viewModel.updateSearchQuery(it) },
                                    onSuggestNextEmpNo = { viewModel.computeSuggestedEmpNo() },
                                    onSaveEmployee = { viewModel.saveEmployee(it) },
                                    onDeleteEmployee = { viewModel.deleteEmployee(it) },
                                    onExportEmployeesCsv = {
                                        scope.launch {
                                            pendingExportContent = viewModel.getEmployeesCsv()
                                            createCsvLauncher.launch("Employees_List.csv")
                                        }
                                    },
                                    onDialPhone = dialPhone
                                )

                                AppTab.REPORTS -> ReportsScreen(
                                    uiState = reportUiState,
                                    onSelectMonth = { viewModel.selectReportMonth(it) },
                                    onShiftMonth = { viewModel.shiftReportMonth(it) },
                                    onExportCsv = { shift ->
                                        scope.launch {
                                            pendingExportContent = viewModel.getMonthlyCsv(shift)
                                            val shiftNameSi = if (shift == ShiftMode.DAY) {
                                                "දිවා_සේවය"
                                            } else {
                                                "රාත්‍රි_සේවය"
                                            }
                                            createCsvLauncher.launch(
                                                "Monthly_Attendance_${shiftNameSi}_${reportUiState.monthStr}.csv"
                                            )
                                        }
                                    }
                                )

                                AppTab.SETTINGS -> SettingsScreen(
                                    isPasswordEnabled = isPasswordEnabled,
                                    isAdminUnlocked = isAdminUnlocked,
                                    isWelcomeSoundEnabled = isWelcomeSoundEnabled,
                                    onToggleWelcomeSound = { viewModel.setWelcomeSoundEnabled(it) },
                                    onTestWelcomeSound = onPlayWelcomeSound,
                                    onRequestUnlock = requestUnlockAndRun,
                                    onLockAdminSession = { viewModel.lockAdminSession() },
                                    onSetupPassword = { pwd -> viewModel.setupPassword(pwd) },
                                    onChangePassword = { cur, next -> viewModel.changePassword(cur, next) },
                                    onRemovePassword = { cur -> viewModel.removePassword(cur) },
                                    onBackupClick = triggerBackup,
                                    onRestoreClick = triggerRestore,
                                    onConfirmResetAll = { viewModel.resetAllData() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private suspend fun writeTextToUri(
    context: android.content.Context,
    uri: Uri,
    content: String
): Boolean = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openOutputStream(uri)?.use { stream ->
            stream.write(content.toByteArray(Charsets.UTF_8))
        } ?: error("Cannot open output stream")
    }.isSuccess
}

private suspend fun readTextFromUri(
    context: android.content.Context,
    uri: Uri
): String? = withContext(Dispatchers.IO) {
    runCatching {
        context.contentResolver.openInputStream(uri)?.use { stream ->
            stream.bufferedReader(Charsets.UTF_8).readText()
        }
    }.getOrNull()
}
