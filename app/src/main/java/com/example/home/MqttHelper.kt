package com.example.home

import android.content.Context
import android.util.Log

import org.eclipse.paho.client.mqttv3.IMqttActionListener
import org.eclipse.paho.client.mqttv3.IMqttMessageListener
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.eclipse.paho.client.mqttv3.MqttAsyncClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.*
import javax.net.ssl.SSLSocketFactory

class MqttHelper(
    private val context: Context,
    private val serverUri: String,
    private val user: String,
    private val pass: String    ) {

    private val clientId = "notif-${UUID.randomUUID()}"
    private val client = MqttAsyncClient(serverUri, clientId, MemoryPersistence())
    @Volatile private var connected = false

    // Reemplaza toda la función connect en MqttHelper.kt con esta:
    // Kotlin
    fun connect() {
        try {
            val opts = MqttConnectOptions().apply {
                isAutomaticReconnect = true
                isCleanSession = false // Mantener sesión para reducir tráfico
                userName= user
                password=pass.toCharArray()

                // Configuración optimizada para ahorro de batería
                connectionTimeout = 30 // Timeout de 30 segundos
                keepAliveInterval = 300 // 5 minutos (reducido de default 60s)
                maxInflight = 10 // Limitar mensajes en vuelo

                // Configura socketFactory solo para URIs seguras
                val lower = serverUri.lowercase(Locale.getDefault())
                if (lower.startsWith("ssl://") || lower.startsWith("tls://") || lower.startsWith("mqtts://")) {
                    socketFactory = defaultSslSocketFactory()
                }
            }

            client.connect(opts, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    connected = true
                    Log.i("MqttHelper", "Conectado a $serverUri")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    connected = false
                    val msg = exception?.message ?: asyncActionToken?.exception?.message ?: "unknown"
                    Log.w("MqttHelper", "Fallo conexión MQTT a $serverUri: $msg", exception)
                }
            })
        } catch (e: MqttException) {
            Log.e("MqttHelper", "Excepción conectando MQTT: ${e.message}", e)
        } catch (e: Exception) {
            Log.e("MqttHelper", "Error inesperado conectando MQTT: ${e.message}", e)
        }
    }

    fun publish(topic: String, payload: String, qos: Int = 1, retained: Boolean = false) {
        try {
            if (!client.isConnected) connect()
            val msg = MqttMessage(payload.toByteArray(Charsets.UTF_8)).apply {
                this.qos = qos
                isRetained = retained
            }
            client.publish(topic, msg, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) { /* opcional */ }
                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w("MqttHelper", "Fallo publicando en $topic", exception)
                }
            })
        } catch (e: Exception) {
            Log.e("MqttHelper", "Error publicando MQTT", e)
        }
    }

    fun subscribe(topic: String, qos: Int = 1, onMessageReceived: (String, String) -> Unit) {
        try {
            if (!client.isConnected) {
                Log.w("MqttHelper", "Cliente no conectado, intentando conectar...")
                connect()
            }

            client.subscribe(topic, qos, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.i("MqttHelper", "Suscrito a $topic")
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.w("MqttHelper", "Fallo suscripción a $topic", exception)
                }
            }, IMqttMessageListener { topic, message ->
                val payload = String(message.payload, Charsets.UTF_8)
                onMessageReceived(topic, payload)
            })
        } catch (e: Exception) {
            Log.e("MqttHelper", "Error suscribiendo MQTT", e)
        }
    }

    fun unsubscribe(topic: String) {
        try {
            if (client.isConnected) {
                client.unsubscribe(topic, null, object : IMqttActionListener {
                    override fun onSuccess(asyncActionToken: IMqttToken?) {
                        Log.i("MqttHelper", "Desuscrito de $topic")
                    }

                    override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                        Log.w("MqttHelper", "Fallo desuscripción de $topic", exception)
                    }
                })
            }
        } catch (e: Exception) {
            Log.e("MqttHelper", "Error desuscribiendo MQTT", e)
        }
    }

    fun disconnect() {
        try {
            if (client.isConnected) client.disconnect()
            connected = false
        } catch (e: Exception) {
            Log.w("MqttHelper", "Error al desconectar", e)
        }
    }

    fun isConnected(): Boolean {
        return try {
            connected || client.isConnected
        } catch (e: Exception) {
            false
        }
    }

    private fun defaultSslSocketFactory(): SSLSocketFactory {
        val sc = javax.net.ssl.SSLContext.getInstance("TLS")
        sc.init(null, null, null)
        return sc.socketFactory
    }
}
