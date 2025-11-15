package com.example.home.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pestaña de Conexión
 * Maneja la configuración y conexión al broker MQTT
 */
@Composable
fun ConnectionTab(
    serverUri: String,
    username: String,
    password: String,
    isConnected: Boolean,
    onServerUriChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ConnectionConfigCard(
                serverUri = serverUri,
                username = username,
                password = password,
                isConnected = isConnected,
                onServerUriChange = onServerUriChange,
                onUsernameChange = onUsernameChange,
                onPasswordChange = onPasswordChange,
                onConnect = onConnect,
                onDisconnect = onDisconnect
            )
        }

        item {
            ConnectionInfoCard()
        }
    }
}

@Composable
private fun ConnectionConfigCard(
    serverUri: String,
    username: String,
    password: String,
    isConnected: Boolean,
    onServerUriChange: (String) -> Unit,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Configuración de Conexión",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

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

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onConnect,
                    enabled = !isConnected,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Conectar")
                }

                Button(
                    onClick = onDisconnect,
                    enabled = isConnected,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Desconectar")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isConnected) "✓ Conectado" else "✗ Desconectado",
                color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
private fun ConnectionInfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "ℹ️ Información",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "• Los datos de conexión se guardan automáticamente\n" +
                       "• Al reiniciar la app, se conectará automáticamente\n" +
                       "• Usa tcp:// para conexiones no seguras\n" +
                       "• Usa ssl:// o mqtts:// para conexiones seguras",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

