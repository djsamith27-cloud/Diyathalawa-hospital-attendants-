package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalAttendanceColors
import kotlinx.coroutines.launch

@Composable
fun PasswordPromptDialog(
    title: String = "මුරපදය ඇතුළත් කරන්න",
    message: String = "වෙනස්කම් සිදු කිරීමට ආරක්ෂක මුරපදය ඇතුළත් කරන්න.",
    confirmLabel: String = "අගුළු හරින්න",
    onDismiss: () -> Unit,
    onVerify: suspend (String) -> Boolean,
    onVerified: () -> Unit
) {
    val semantic = LocalAttendanceColors.current
    val scope = rememberCoroutineScope()
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isChecking by remember { mutableStateOf(false) }

    fun submit() {
        if (isChecking) return
        if (password.isEmpty()) {
            errorMessage = "කරුණාකර මුරපදය ඇතුළත් කරන්න."
            return
        }
        isChecking = true
        errorMessage = null
        scope.launch {
            val valid = onVerify(password)
            isChecking = false
            if (valid) {
                onVerified()
            } else {
                errorMessage = "වැරදි මුරපදයකි. නැවත උත්සාහ කරන්න."
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = {
                        password = it
                        errorMessage = null
                    },
                    label = { Text("මුරපදය (Password / PIN)") },
                    singleLine = true,
                    isError = errorMessage != null,
                    visualTransformation = if (passwordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) {
                                    Icons.Filled.VisibilityOff
                                } else {
                                    Icons.Filled.Visibility
                                },
                                contentDescription = if (passwordVisible) {
                                    "මුරපදය සඟවන්න"
                                } else {
                                    "මුරපදය පෙන්වන්න"
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_prompt_input")
                )

                if (errorMessage != null) {
                    Surface(
                        color = semantic.absentContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.onAbsentContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                enabled = !isChecking,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("password_prompt_confirm")
            ) {
                Icon(
                    imageVector = Icons.Filled.LockOpen,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(confirmLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("password_prompt_cancel")
            ) {
                Text("අවලංගු කරන්න")
            }
        }
    )
}

@Composable
fun SetupOrChangePasswordDialog(
    isChangingExisting: Boolean,
    onDismiss: () -> Unit,
    onSetupPassword: suspend (newPassword: String) -> Unit,
    onChangePassword: suspend (currentPassword: String, newPassword: String) -> Boolean,
    onCompleted: () -> Unit
) {
    val semantic = LocalAttendanceColors.current
    val scope = rememberCoroutineScope()

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showCurrent by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    fun submit() {
        if (isSaving) return
        if (isChangingExisting && currentPassword.isEmpty()) {
            errorMessage = "කරුණාකර දැනට පවතින මුරපදය ඇතුළත් කරන්න."
            return
        }
        if (newPassword.length < 4) {
            errorMessage = "නව මුරපදය අවම වශයෙන් අකුරු හෝ ඉලක්කම් 4ක් විය යුතුය."
            return
        }
        if (newPassword != confirmPassword) {
            errorMessage = "ඇතුළත් කළ මුරපද දෙක නොගැළපේ."
            return
        }

        isSaving = true
        errorMessage = null
        scope.launch {
            if (isChangingExisting) {
                val ok = onChangePassword(currentPassword, newPassword)
                isSaving = false
                if (ok) {
                    onCompleted()
                } else {
                    errorMessage = "දැනට පවතින මුරපදය වැරදියි."
                }
            } else {
                onSetupPassword(newPassword)
                isSaving = false
                onCompleted()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Filled.Key,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(
                text = if (isChangingExisting) "මුරපදය වෙනස් කරන්න" else "නව මුරපදය සකසන්න",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "පැමිණීම් සටහන් කිරීම සහ සැකසුම් වෙනස් කිරීම ආරක්ෂා කිරීමට මුරපදයක් සකසන්න (අවම අක්ෂර 4).",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (isChangingExisting) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = {
                            currentPassword = it
                            errorMessage = null
                        },
                        label = { Text("දැනට පවතින මුරපදය") },
                        singleLine = true,
                        visualTransformation = if (showCurrent) {
                            VisualTransformation.None
                        } else {
                            PasswordVisualTransformation()
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Next
                        ),
                        trailingIcon = {
                            IconButton(onClick = { showCurrent = !showCurrent }) {
                                Icon(
                                    imageVector = if (showCurrent) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                    contentDescription = if (showCurrent) "මුරපදය සඟවන්න" else "මුරපදය පෙන්වන්න"
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("current_password_input")
                    )
                }

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = {
                        newPassword = it
                        errorMessage = null
                    },
                    label = { Text("නව මුරපදය (අවම අක්ෂර 4)") },
                    singleLine = true,
                    visualTransformation = if (showNew) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Next
                    ),
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                imageVector = if (showNew) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = if (showNew) "මුරපදය සඟවන්න" else "මුරපදය පෙන්වන්න"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("new_password_input")
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = {
                        confirmPassword = it
                        errorMessage = null
                    },
                    label = { Text("නව මුරපදය නැවත ඇතුළත් කරන්න") },
                    singleLine = true,
                    visualTransformation = if (showNew) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { submit() }),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("confirm_password_input")
                )

                if (errorMessage != null) {
                    Surface(
                        color = semantic.absentContainer,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = semantic.onAbsentContainer,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { submit() },
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(
                    containerColor = semantic.presentSolid,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_password_button")
            ) {
                Text("සුරකින්න")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_password_setup_button")
            ) {
                Text("අවලංගු කරන්න")
            }
        }
    )
}
