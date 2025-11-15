package com.example.home.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.home.models.ReceivedMessage
import com.example.home.ui.components.SettingsDialog
import com.example.home.ui.screens.ButtonsTab
import com.example.home.ui.screens.PublishTab
import com.example.home.ui.screens.SubscriptionTab

/**
 * Pantalla principal de la aplicación MQTT
 * Contiene la navegación por pestañas y maneja el estado global de la app
 */
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
    // Estado de conexión
    var serverUri by remember { mutableStateOf(initialServerUri) }
    var username by remember { mutableStateOf(initialUsername) }
    var password by remember { mutableStateOf(initialPassword) }
    var isConnected by remember { mutableStateOf(initiallyConnected) }

    // Estado de suscripción
    var subscribeTopic by remember { mutableStateOf(initialSubscribeTopic) }
    var subscribedTopics by remember { mutableStateOf(setOf<String>()) }
    var messages by remember { mutableStateOf(listOf<ReceivedMessage>()) }

    // Estado de publicación
    var publishTopic by remember { mutableStateOf(initialSubscribeTopic) }
    var publishMessage by remember { mutableStateOf("Hello MQTT!") }

    // Estado de navegación - Sin pestaña de Conexión
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Botones", "Suscripción", "Publicación")

    // Estado del diálogo de configuración
    var showSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Mi hogar") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    navigationIcon = {
                        IconButton(onClick = { showSettingsDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Configuración"
                            )
                        }
                    }
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
        // Diálogo de configuración
        SettingsDialog(
            showDialog = showSettingsDialog,
            serverUri = serverUri,
            username = username,
            password = password,
            isConnected = isConnected,
            onDismiss = { showSettingsDialog = false },
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> ButtonsTab(
                    isConnected = isConnected,
                    onPublish = onPublish
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

