package com.example.home.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.home.models.ReceivedMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pestaña de Suscripción
 * Permite suscribirse a topics y visualizar mensajes recibidos
 */
@Composable
fun SubscriptionTab(
    subscribeTopic: String,
    isConnected: Boolean,
    subscribedTopics: Set<String>,
    messages: List<ReceivedMessage>,
    onSubscribeTopicChange: (String) -> Unit,
    onSubscribe: () -> Unit,
    onUnsubscribe: () -> Unit,
    onClearMessages: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SubscriptionControlCard(
                subscribeTopic = subscribeTopic,
                isConnected = isConnected,
                subscribedTopics = subscribedTopics,
                onSubscribeTopicChange = onSubscribeTopicChange,
                onSubscribe = onSubscribe,
                onUnsubscribe = onUnsubscribe
            )
        }

        item {
            MessagesHeaderCard(
                messageCount = messages.size,
                onClearMessages = onClearMessages
            )
        }

        items(messages.reversed()) { message ->
            MessageCard(message = message)
        }
    }
}

@Composable
private fun SubscriptionControlCard(
    subscribeTopic: String,
    isConnected: Boolean,
    subscribedTopics: Set<String>,
    onSubscribeTopicChange: (String) -> Unit,
    onSubscribe: () -> Unit,
    onUnsubscribe: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Suscripción a Topics",
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
                value = subscribeTopic,
                onValueChange = onSubscribeTopicChange,
                label = { Text("Topic") },
                modifier = Modifier.fillMaxWidth(),
                enabled = isConnected,
                singleLine = true,
                placeholder = { Text("test/topic") }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSubscribe,
                    enabled = isConnected && subscribeTopic.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Suscribirse")
                }

                Button(
                    onClick = onUnsubscribe,
                    enabled = isConnected && subscribeTopic in subscribedTopics,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("Desuscribirse")
                }
            }

            if (subscribedTopics.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Suscrito a:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                subscribedTopics.forEach { topic ->
                    Text(
                        text = "• $topic",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MessagesHeaderCard(
    messageCount: Int,
    onClearMessages: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mensajes Recibidos ($messageCount)",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                if (messageCount > 0) {
                    TextButton(onClick = onClearMessages) {
                        Text("Limpiar")
                    }
                }
            }

            if (messageCount == 0) {
                Text(
                    text = "No hay mensajes recibidos",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MessageCard(message: ReceivedMessage) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Topic: ${message.topic}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = message.payload,
                fontSize = 14.sp
            )
            Text(
                text = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(Date(message.timestamp)),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

