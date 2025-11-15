package com.example.home.models

/**
 * Modelo de datos para representar un mensaje MQTT recibido
 */
data class ReceivedMessage(
    val topic: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)


