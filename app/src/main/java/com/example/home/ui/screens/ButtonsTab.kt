package com.example.home.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pestaña de Botones Predefinidos
 * Permite publicar mensajes predeterminados con un solo clic
 */
@Composable
fun ButtonsTab(
    isConnected: Boolean,
    onPublish: (String, String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            QuickActionsCard(
                isConnected = isConnected,
                onPublish = onPublish
            )
        }

        item {
            ButtonsInfoCard()
        }
    }
}

@Composable
private fun QuickActionsCard(
    isConnected: Boolean,
    onPublish: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Acciones Rápidas",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (!isConnected) {
                Text(
                    text = "⚠️ Debes conectarte primero para usar los botones",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            // Botón 1: Radio 1 - Hola
            QuickActionButton(
                title = "Radio 1 - Hola",
                topic = "cmnd/ipe/radio1/Backlog",
                message = "RfRaw AAB02903080168029422EC28180918090918091809091809090909091809180918180918180909180909180955;RfRaw 0",
                enabled = isConnected,
                onPublish = onPublish
            )


        }
    }
}

@Composable
private fun QuickActionButton(
    title: String,
    topic: String,
    message: String,
    enabled: Boolean,
    onPublish: (String, String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled)
                MaterialTheme.colorScheme.secondaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Topic: $topic",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Mensaje: $message",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = { onPublish(topic, message) },
                enabled = enabled,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Enviar")
            }
        }
    }
}

@Composable
private fun ButtonsInfoCard() {
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
                text = "• Los botones ejecutan acciones predefinidas\n" +
                       "• Cada botón publica a un topic específico\n" +
                       "• Los botones se activan solo cuando hay conexión\n" +
                       "• Puedes personalizar los botones en el código",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

