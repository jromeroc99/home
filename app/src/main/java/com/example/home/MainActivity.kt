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
    private var currentTopic: String = ""
    private var currentServerUri: String = ""
    private var currentUsername: String = ""
    private var currentPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Recuperar datos guardados de SharedPreferences
        val sharedPreferences = getSharedPreferences("MqttPreferences", MODE_PRIVATE)
        val savedServerUri = sharedPreferences.getString("server_uri", "") ?: ""
        val savedUsername = sharedPreferences.getString("username", "") ?: ""
        val savedPassword = sharedPreferences.getString("password", "") ?: ""
        val savedTopic = sharedPreferences.getString("subscribe_topic", "test/topic") ?: "test/topic"

        // Conexión automática si hay datos guardados
        val shouldAutoConnect = savedServerUri.isNotEmpty()
        if (shouldAutoConnect) {
            currentServerUri = savedServerUri
            currentUsername = savedUsername
            currentPassword = savedPassword
            currentTopic = savedTopic

            mqttHelper = MqttHelper(this, savedServerUri, savedUsername, savedPassword)
            mqttHelper?.connect()
        }

        setContent {
            HomeTheme {
                MqttApp(
                    initialServerUri = savedServerUri.ifEmpty { "tcp://broker.hivemq.com:1883" },
                    initialUsername = savedUsername,
                    initialPassword = savedPassword,
                    initialSubscribeTopic = savedTopic,
                    initiallyConnected = shouldAutoConnect,
                    onConnect = { serverUri, user, pass ->
                        // Guardar los datos de conexión actuales
                        currentServerUri = serverUri
                        currentUsername = user
                        currentPassword = pass

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
                        currentTopic = topic  // Guardar el tópico actual
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

        // PASO 1: Obtener o crear SharedPreferences
        // "MqttPreferences" es el nombre del archivo donde se guardarán los datos
        // MODE_PRIVATE significa que solo esta app puede acceder a estos datos
        val sharedPreferences = getSharedPreferences("MqttPreferences", MODE_PRIVATE)

        // PASO 2: Crear un editor para modificar las preferencias
        val editor = sharedPreferences.edit()

        // PASO 3: Guardar todos los valores con sus claves (key-value)

        // Guardar datos de conexión
        editor.putString("server_uri", currentServerUri)
        editor.putString("username", currentUsername)
        editor.putString("password", currentPassword)

        // Guardar tópico de suscripción
        editor.putString("subscribe_topic", currentTopic)

        // PASO 4: Aplicar los cambios
        // apply() guarda de forma asíncrona (no bloquea el hilo principal)
        // Alternativa: commit() guarda de forma síncrona (espera a que termine)
        editor.apply()

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
    initialServerUri: String = "tcp://broker.hivemq.com:1883",
    initialUsername: String = "",
    initialPassword: String = "",
    initialSubscribeTopic: String = "test/topic",
    initiallyConnected: Boolean = false,
    onConnect: (String, String, String) -> Unit,
    onDisconnect: () -> Unit,
    onPublish: (String, String) -> Unit,
    onSubscribe: (String, (String, String) -> Unit) -> Unit,
    onUnsubscribe: (String) -> Unit,
    checkConnection: () -> Boolean
) {
    // Usar los valores iniciales recuperados de SharedPreferences
    var serverUri by remember { mutableStateOf(initialServerUri) }
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf(initialPassword) }
    var isConnected by remember { mutableStateOf(initiallyConnected) }

    var subscribeTopic by remember { mutableStateOf(initialSubscribeTopic) }
    var publishTopic by remember { mutableStateOf(initialSubscribeTopic) }
    var publishMessage by remember { mutableStateOf("Hello MQTT!") }

    var messages by remember { mutableStateOf(listOf<ReceivedMessage>()) }
    var subscribedTopics by remember { mutableStateOf(setOf<String>()) }

    // Estado para el menú de navegación
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Conexión", "Suscripción", "Publicación")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("MQTT Client") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ConnectionTab(
                    serverUri = serverUri,
                    username = username,
                    password = password,
                    isConnected = isConnected,
                    onServerUriChange = { serverUri = it },
                    onUsernameChange = { username = it },
                    onPasswordChange = { password = it },
                    onConnect = {
                        onConnect(serverUri, username, password)
                        isConnected = true
                    },
                    onDisconnect = {
                        onDisconnect()
                        isConnected = false
                        subscribedTopics = setOf()
                    }
                )
                1 -> SubscriptionTab(
                    subscribeTopic = subscribeTopic,
                    isConnected = isConnected,
                    subscribedTopics = subscribedTopics,
                    messages = messages,
                    onSubscribeTopicChange = { subscribeTopic = it },
                    onSubscribe = {
                        onSubscribe(subscribeTopic) { topic, msg ->
                            messages = messages + ReceivedMessage(topic, msg)
                        }
                        subscribedTopics = subscribedTopics + subscribeTopic
                    },
                    onUnsubscribe = {
                        onUnsubscribe(subscribeTopic)
                        subscribedTopics = subscribedTopics - subscribeTopic
                    },
                    onClearMessages = { messages = listOf() }
                )
                2 -> PublishTab(
                    publishTopic = publishTopic,
                    publishMessage = publishMessage,
                    isConnected = isConnected,
                    onPublishTopicChange = { publishTopic = it },
                    onPublishMessageChange = { publishMessage = it },
                    onPublish = {
                        onPublish(publishTopic, publishMessage)
                    }
                )
            }
        }
    }
}

// Pestaña de Conexión
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

        item {
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
    }
}

// Pestaña de Suscripción
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
                            TextButton(onClick = onClearMessages) {
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

// Pestaña de Publicación
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

        item {
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
    }
}

