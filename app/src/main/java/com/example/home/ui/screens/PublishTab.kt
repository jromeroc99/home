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
 * Pestaña de Publicación
 * Permite publicar mensajes a topics MQTT
 */
@Composable
fun PublishTab(
    publishTopic: String,
    publishMessage: String,
    isConnected: Boolean,
    onPublishTopicChange: (String) -> Unit,
    onPublishMessageChange: (String) -> Unit,
    onPublish: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PublishControlCard(
                publishTopic = publishTopic,
                publishMessage = publishMessage,
                isConnected = isConnected,
                onPublishTopicChange = onPublishTopicChange,
                onPublishMessageChange = onPublishMessageChange,
                onPublish = onPublish
            )
        }

        item {
            PublishInfoCard()
        }
    }
}

@Composable
private fun PublishControlCard(
    publishTopic: String,
    publishMessage: String,
    isConnected: Boolean,
    onPublishTopicChange: (String) -> Unit,
    onPublishMessageChange: (String) -> Unit,
    onPublish: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Publicar Mensaje",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            if (!isConnected) {
                Text(
                    text = "⚠️ Debes conectarte primero",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp
                )
            }

            OutlinedTextField(
                value = publishTopic,
                onValueChange = onPublishTopicChange,
                label = { Text("Topic") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                singleLine = true,
                placeholder = { Text("test/topic") }
            )

            OutlinedTextField(
                value = publishMessage,
                onValueChange = onPublishMessageChange,
                label = { Text("Mensaje") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                minLines = 4,
                maxLines = 8,
                placeholder = { Text("Escribe tu mensaje aquí...") }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onPublish,
                enabled = isConnected && publishTopic.isNotBlank() && publishMessage.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publicar", fontSize = 16.sp)
            }
        }
    }
}

@Composable
private fun PublishInfoCard() {
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
                text = "ℹ️ Consejos",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "• Puedes publicar en cualquier topic\n" +
                       "• No necesitas estar suscrito para publicar\n" +
                       "• El mensaje se enviará a todos los suscriptores del topic\n" +
                       "• Usa topics jerárquicos: casa/sala/luz",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

