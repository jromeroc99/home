package com.example.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.home.ui.MqttApp
import com.example.home.ui.theme.HomeTheme

/**
 * Actividad principal de la aplicación
 * Maneja el ciclo de vida, persistencia de datos y la instancia de MqttHelper
 */
class MainActivity : ComponentActivity() {

    private var mqttHelper: MqttHelper? = null

    // Variables para mantener el estado actual de la conexión
    private var currentTopic: String = ""
    private var currentServerUri: String = ""
    private var currentUsername: String = ""
    private var currentPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Cargar datos guardados de SharedPreferences
        val savedData = loadPreferences()

        // Conexión automática si hay datos guardados
        if (savedData.shouldAutoConnect) {
            connectAutomatically(savedData)
        }

        setContent {
            HomeTheme {
                MqttApp(
                    initialServerUri = savedData.serverUri,
                    initialUsername = savedData.username,
                    initialPassword = savedData.password,
                    initialSubscribeTopic = savedData.subscribeTopic,
                    initiallyConnected = savedData.shouldAutoConnect,
                    onConnect = ::handleConnect,
                    onDisconnect = ::handleDisconnect,
                    onPublish = ::handlePublish,
                    onSubscribe = ::handleSubscribe,
                    onUnsubscribe = ::handleUnsubscribe,
                    checkConnection = ::isConnected
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        savePreferences()
        mqttHelper?.disconnect()
    }

    /**
     * Maneja la conexión al broker MQTT
     */
    private fun handleConnect(serverUri: String, user: String, pass: String) {
        currentServerUri = serverUri
        currentUsername = user
        currentPassword = pass

        mqttHelper = MqttHelper(this, serverUri, user, pass)
        mqttHelper?.connect()
    }

    /**
     * Maneja la desconexión del broker MQTT
     */
    private fun handleDisconnect() {
        mqttHelper?.disconnect()
    }

    /**
     * Maneja la publicación de mensajes
     */
    private fun handlePublish(topic: String, message: String) {
        mqttHelper?.publish(topic, message)
    }

    /**
     * Maneja la suscripción a un topic
     */
    private fun handleSubscribe(topic: String, callback: (String, String) -> Unit) {
        currentTopic = topic
        mqttHelper?.subscribe(topic) { t, msg ->
            callback(t, msg)
        }
    }

    /**
     * Maneja la desuscripción de un topic
     */
    private fun handleUnsubscribe(topic: String) {
        mqttHelper?.unsubscribe(topic)
    }

    /**
     * Verifica si está conectado
     */
    private fun isConnected(): Boolean {
        return mqttHelper?.isConnected() ?: false
    }

    /**
     * Carga las preferencias guardadas
     */
    private fun loadPreferences(): SavedPreferences {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        val serverUri = sharedPreferences.getString(KEY_SERVER_URI, "") ?: ""
        val username = sharedPreferences.getString(KEY_USERNAME, "") ?: ""
        val password = sharedPreferences.getString(KEY_PASSWORD, "") ?: ""
        val subscribeTopic = sharedPreferences.getString(KEY_SUBSCRIBE_TOPIC, DEFAULT_TOPIC) ?: DEFAULT_TOPIC

        return SavedPreferences(
            serverUri = serverUri.ifEmpty { DEFAULT_SERVER_URI },
            username = username,
            password = password,
            subscribeTopic = subscribeTopic,
            shouldAutoConnect = serverUri.isNotEmpty()
        )
    }

    /**
     * Guarda las preferencias actuales
     */
    private fun savePreferences() {
        val sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putString(KEY_SERVER_URI, currentServerUri)
            putString(KEY_USERNAME, currentUsername)
            putString(KEY_PASSWORD, currentPassword)
            putString(KEY_SUBSCRIBE_TOPIC, currentTopic)
            apply()
        }
    }

    /**
     * Conecta automáticamente usando los datos guardados
     */
    private fun connectAutomatically(savedData: SavedPreferences) {
        currentServerUri = savedData.serverUri
        currentUsername = savedData.username
        currentPassword = savedData.password
        currentTopic = savedData.subscribeTopic

        mqttHelper = MqttHelper(this, savedData.serverUri, savedData.username, savedData.password)
        mqttHelper?.connect()
    }

    companion object {
        private const val PREFS_NAME = "MqttPreferences"
        private const val KEY_SERVER_URI = "server_uri"
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SUBSCRIBE_TOPIC = "subscribe_topic"

        private const val DEFAULT_SERVER_URI = "tcp://broker.hivemq.com:1883"
        private const val DEFAULT_TOPIC = "test/topic"
    }

    /**
     * Clase de datos para las preferencias guardadas
     */
    private data class SavedPreferences(
        val serverUri: String,
        val username: String,
        val password: String,
        val subscribeTopic: String,
        val shouldAutoConnect: Boolean
    )
}

