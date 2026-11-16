package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NoEncryption
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.ui.components.GlassCard
import com.example.ui.components.PasswordPromptDialog
import com.example.ui.components.SetupOrChangePasswordDialog
import com.example.ui.theme.LocalAttendanceColors

@Composable
fun SettingsScreen(
    isPasswordEnabled: Boolean,
    isAdminUnlocked: Boolean,
    isWelcomeSoundEnabled: Boolean,
    onToggleWelcomeSound: (Boolean) -> Unit,
    onTestWelcomeSound: () -> Unit,
    onRequestUnlock: (onUnlocked: () -> Unit) -> Unit,
    onLockAdminSession: () -> Unit,
    onSetupPassword: suspend (newPassword: String) -> Unit,
    onChangePassword: suspend (currentPassword: String, newPassword: String) -> Boolean,
    onRemovePassword: suspend (currentPassword: String) -> Boolean,
    onBackupClick: () -> Unit,
    onRestoreClick: () -> Unit,
    onConfirmResetAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    val semantic = LocalAttendanceColors.current
    val requiresAuth = isPasswordEnabled && !isAdminUnlocked

    var showResetConfirm by remember { mutableStateOf(false) }
    var showSetOrChangePasswordDialog by remember { mutableStateOf(false) }
    var showRemovePasswordDialog by remember { mutableStateOf(false) }

    if (showSetOrChangePasswordDialog) {
        SetupOrChangePasswordDialog(
            isChangingExisting = isPasswordEnabled,
            onDismiss = { showSetOrChangePasswordDialog = false },
            onSetupPassword = onSetupPassword,
            onChangePassword = onChangePassword,
            onCompleted = { showSetOrChangePasswordDialog = false }
        )
    }

    if (showRemovePasswordDialog) {
        PasswordPromptDialog(
            title = "මුරපද ආරක්ෂාව ඉවත් කිරීම",
            message = "මුරපද ආරක්ෂාව ඉවත් කිරීමට දැනට පවතින මුරපදය ඇතුළත් කරන්න.",
            confirmLabel = "ඉවත් කරන්න",
            onDismiss = { showRemovePasswordDialog = false },
            onVerify = onRemovePassword,
            onVerified = { showRemovePasswordDialog = false }
        )
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Filled.Warning,
                    contentDescription = null,
                    tint = semantic.absent
                )
            },
            title = {
                Text(
                    text = "සියලු දත්ත මකා දැමීම",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "සියලුම සේවක සහ පැමිණීමේ දත්ත මකා දැමීමට අවශ්‍ය බව ස්ථිරද? මෙය නැවත ලබාගත නොහැක!",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetConfirm = false
                        onConfirmResetAll()
                    }
                ) {
                    Text("මකා දමන්න", color = semantic.absent)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) {
                    Text("අවලංගු කරන්න")
                }
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Password Lock Security Card
        item {
            GlassCard(
                borderColor = if (isPasswordEnabled) {
                    semantic.present.copy(alpha = 0.45f)
                } else {
                    null
                }
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Security,
                                contentDescription = null,
                                tint = if (isPasswordEnabled) semantic.present else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "මුරපද ආරක්ෂාව (Password Lock)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(100.dp),
                            color = if (isPasswordEnabled) semantic.presentContainer else MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (isPasswordEnabled) "සක්‍රියයි" else "අක්‍රියයි",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isPasswordEnabled) semantic.onPresentContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))

                    Text(
                        text = if (isPasswordEnabled) {
                            "පැමිණීම් සටහන් කිරීමේ කොටසේ සහ සැකසුම් කොටසේ වෙනස්කම් සිදු කිරීමට මුරපදය අවශ්‍ය වේ."
                        } else {
                            "සේවක පැමිණීම් සටහන් කිරීමේ සහ සැකසුම් කොටසේ අනවසර වෙනස්කම් වැළැක්වීමට මුරපදයක් සකසන්න."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (!isPasswordEnabled) {
                        Button(
                            onClick = { showSetOrChangePasswordDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = semantic.presentSolid,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("set_password_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Key,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "මුරපදය සකසන්න (Set Password)",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Button(
                            onClick = {
                                if (requiresAuth) {
                                    onRequestUnlock {}
                                } else {
                                    onLockAdminSession()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (requiresAuth) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    semantic.presentSolid
                                },
                                contentColor = if (requiresAuth) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    Color.White
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .minimumInteractiveComponentSize()
                                .testTag("settings_auth_toggle_button")
                        ) {
                            Icon(
                                imageVector = if (requiresAuth) Icons.Filled.LockOpen else Icons.Filled.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (requiresAuth) {
                                    "වෙනස්කම් සඳහා අගුළු හරින්න (Unlock)"
                                } else {
                                    "නැවත අගුළු දමන්න (Lock Now)"
                                },
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = { showSetOrChangePasswordDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("change_password_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Key,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "වෙනස් කරන්න",
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Surface(
                                onClick = { showRemovePasswordDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                color = semantic.absentContainer,
                                border = BorderStroke(1.dp, semantic.absent.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .minimumInteractiveComponentSize()
                                    .testTag("remove_password_button")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.NoEncryption,
                                        contentDescription = null,
                                        tint = semantic.onAbsentContainer,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ඉවත් කරන්න",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = semantic.onAbsentContainer,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Welcome Sound Card
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.VolumeUp,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "පිළිගැනීමේ හඬ (Welcome Boss)",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Switch(
                            checked = isWelcomeSoundEnabled,
                            onCheckedChange = { newValue ->
                                if (requiresAuth) {
                                    onRequestUnlock { onToggleWelcomeSound(newValue) }
                                } else {
                                    onToggleWelcomeSound(newValue)
                                }
                            },
                            modifier = Modifier.testTag("welcome_sound_switch")
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    Text(
                        text = "ඇප් එක විවෘත කරන සෑම අවස්ථාවකම 'Welcome Boss' පිළිගැනීමේ හඬ වාදනය වේ.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = onTestWelcomeSound,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("test_welcome_sound_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "හඬ පරීක්ෂා කරන්න (Test Sound)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Offline App Info & Backup/Restore Card
        item {
            GlassCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PhoneAndroid,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "දුරකථනයේ Offline භාවිතය සහ දත්ත ආරක්ෂණය",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                    Text(
                        text = "මෙම පද්ධතිය අන්තර්ජාලය නොමැතිව (Offline) වුවද ඔබගේ දුරකථනයෙන් පහසුවෙන් භාවිත කළ හැක. සියලුම දත්ත ආරක්ෂිතව දුරකථනය තුළ සුරැකේ. අවශ්‍ය විටෙක JSON Backup ගොනුවක් ලෙස ලබාගැනීමට හෝ Restore කිරීමට පහත බොත්තම් භාවිත කරන්න.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onBackupClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("settings_backup_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudDownload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup JSON", style = MaterialTheme.typography.labelLarge)
                        }

                        OutlinedButton(
                            onClick = {
                                if (requiresAuth) {
                                    onRequestUnlock { onRestoreClick() }
                                } else {
                                    onRestoreClick()
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .minimumInteractiveComponentSize()
                                .testTag("settings_restore_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Restore JSON", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
        }

        // Danger Zone: Reset All Data
        item {
            GlassCard(borderColor = semantic.absent.copy(alpha = 0.35f)) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Warning,
                            contentDescription = null,
                            tint = semantic.absent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "දත්ත කළමනාකරණය සහ යළි පිහිටුවීම",
                            style = MaterialTheme.typography.titleSmall,
                            color = semantic.absent
                        )
                    }
                    HorizontalDivider(color = semantic.absent.copy(alpha = 0.25f))
                    Text(
                        text = "සියලුම සේවක දත්ත සහ පැමිණීම් ඉවත් කර මුල සිට ආරම්භ කිරීමට අවශ්‍ය නම් පමණක් පහත බොත්තම භාවිත කරන්න.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Surface(
                        onClick = {
                            if (requiresAuth) {
                                onRequestUnlock { showResetConfirm = true }
                            } else {
                                showResetConfirm = true
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = semantic.absentContainer,
                        border = BorderStroke(1.dp, semantic.absent.copy(alpha = 0.45f)),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("reset_all_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = null,
                                tint = semantic.onAbsentContainer,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "සියලු දත්ත මකා දමන්න (Reset All)",
                                style = MaterialTheme.typography.labelLarge,
                                color = semantic.onAbsentContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
