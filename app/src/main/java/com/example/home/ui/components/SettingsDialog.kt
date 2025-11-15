package com.example.home.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Diálogo de configuración de conexión MQTT
 */
@Composable
fun SettingsDialog(
    showDialog: Boolean,
    serverUri: String,
    username: String,
    password: String,
    isConnected: Boolean,
    onDismiss: () -> Unit,
    onServerUriChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "⚙️ Configuración MQTT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Estado de conexión
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (isConnected)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = if (isConnected) "✓ Conectado" else "✗ Desconectado",
                                color = if (isConnected) Color(0xFF1B5E20) else Color(0xFFB71C1C),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Campos de configuración
                    OutlinedTextField(
                        value = serverUri,
                        onValueChange = onServerUriChange,
                        label = { Text("Broker URI") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConnected,
                        singleLine = true,
                        placeholder = { Text("tcp://broker.hivemq.com:1883") }
                    )

                    OutlinedTextField(
                        value = username,
                        onValueChange = onUsernameChange,
                        label = { Text("Usuario (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConnected,
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("Contraseña (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isConnected,
                        singleLine = true
                    )

                    // Info adicional
                    Text(
                        text = "💡 Los datos se guardan automáticamente",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                if (isConnected) {
                    Button(
                        onClick = {
                            onDisconnect()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Text("Desconectar")
                    }
                } else {
                    Button(
                        onClick = {
                            onConnect()
                            onDismiss()
                        }
                    ) {
                        Text("Conectar")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cerrar")
                }
            }
        )
    }
}

