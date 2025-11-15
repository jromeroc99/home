package com.example.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.home.ui.theme.HomeTheme

class MainActivity : ComponentActivity() {
    private var mqttHelper: MqttHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HomeTheme {
                MqttApp(
                    onConnect = { serverUri, user, pass ->
                        mqttHelper = MqttHelper(this, serverUri, user, pass)
                        mqttHelper?.connect()
                    },
                    onDisconnect = {
                        mqttHelper?.disconnect()
                    },
                    onPublish = { topic, message ->
                        mqttHelper?.publish(topic, message)
                    },
                    onSubscribe = { topic, callback ->
                        mqttHelper?.subscribe(topic) { t, msg ->
                            callback(t, msg)
                        }
                    },
                    onUnsubscribe = { topic ->
                        mqttHelper?.unsubscribe(topic)
                    },
                    checkConnection = {
                        mqttHelper?.isConnected() ?: false
                    }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttHelper?.disconnect()
    }
}

data class ReceivedMessage(
    val topic: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MqttApp(
    onConnect: (String, String, String) -> Unit,
    onDisconnect: () -> Unit,
    onPublish: (String, String) -> Unit,
    onSubscribe: (String, (String, String) -> Unit) -> Unit,
    onUnsubscribe: (String) -> Unit,
    checkConnection: () -> Boolean
) {
    var serverUri by remember { mutableStateOf("tcp://broker.hivemq.com:1883") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isConnected by remember { mutableStateOf(false) }

    var subscribeTopic by remember { mutableStateOf("test/topic") }
    var publishTopic by remember { mutableStateOf("test/topic") }
    var publishMessage by remember { mutableStateOf("Hello MQTT!") }

    var messages by remember { mutableStateOf(listOf<ReceivedMessage>()) }
    var subscribedTopics by remember { mutableStateOf(setOf<String>()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MQTT Client") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sección de Conexión
            item {
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
                            text = "Conexión",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = serverUri,
                            onValueChange = { serverUri = it },
                            label = { Text("Broker URI") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isConnected,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("Usuario (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isConnected,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña (opcional)") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = !isConnected,
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onConnect(serverUri, username, password)
                                    isConnected = true
                                },
                                enabled = !isConnected,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Conectar")
                            }

                            Button(
                                onClick = {
                                    onDisconnect()
                                    isConnected = false
                                    subscribedTopics = setOf()
                                },
                                enabled = isConnected,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Text("Desconectar")
                            }
                        }

                        Text(
                            text = if (isConnected) "✓ Conectado" else "✗ Desconectado",
                            color = if (isConnected) Color(0xFF4CAF50) else Color(0xFFF44336),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Sección de Suscripción
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Suscripción",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = subscribeTopic,
                            onValueChange = { subscribeTopic = it },
                            label = { Text("Topic") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isConnected,
                            singleLine = true
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSubscribe(subscribeTopic) { topic, msg ->
                                        messages = messages + ReceivedMessage(topic, msg)
                                    }
                                    subscribedTopics = subscribedTopics + subscribeTopic
                                },
                                enabled = isConnected && subscribeTopic.isNotBlank(),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Suscribirse")
                            }

                            Button(
                                onClick = {
                                    onUnsubscribe(subscribeTopic)
                                    subscribedTopics = subscribedTopics - subscribeTopic
                                },
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
                            Text(
                                text = "Suscrito a: ${subscribedTopics.joinToString(", ")}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Sección de Publicación
            item {
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

                        OutlinedTextField(
                            value = publishTopic,
                            onValueChange = { publishTopic = it },
                            label = { Text("Topic") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isConnected,
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = publishMessage,
                            onValueChange = { publishMessage = it },
                            label = { Text("Mensaje") },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = isConnected,
                            minLines = 3,
                            maxLines = 5
                        )

                        Button(
                            onClick = {
                                onPublish(publishTopic, publishMessage)
                            },
                            enabled = isConnected && publishTopic.isNotBlank() && publishMessage.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Publicar")
                        }
                    }
                }
            }

            // Sección de Mensajes Recibidos
            item {
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
                                text = "Mensajes Recibidos (${messages.size})",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )

                            if (messages.isNotEmpty()) {
                                TextButton(onClick = { messages = listOf() }) {
                                    Text("Limpiar")
                                }
                            }
                        }

                        if (messages.isEmpty()) {
                            Text(
                                text = "No hay mensajes recibidos",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                    }
                }
            }

            // Lista de mensajes
            items(messages.reversed()) { message ->
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
                            text = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
                                .format(java.util.Date(message.timestamp)),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}