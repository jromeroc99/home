# 📱 MQTT Client Android

Cliente MQTT minimalista y funcional para Android con interfaz moderna en Jetpack Compose.

## ✨ Características

🔌 Conexión a brokers MQTT (TCP/SSL/TLS)  
📤 Publicar mensajes  
📥 Suscribirse a múltiples topics  
💬 Visualización en tiempo real  
🎨 Material Design 3

## 🚀 Inicio Rápido

1. **Conectar**: Ingresa el broker (ej: `tcp://broker.hivemq.com:1883`) → Conectar
2. **Suscribir**: Escribe el topic → Suscribirse
3. **Publicar**: Ingresa topic + mensaje → Publicar

## 🛠 Stack Técnico

- **Kotlin 2.1.0**
- **Jetpack Compose** + Material 3
- **Paho MQTT 4.4.2**

## 📦 Compilar

```powershell
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
.\gradlew assembleDebug
```

APK generado en: `app/build/outputs/apk/debug/app-debug.apk`

## 🧪 Brokers de Prueba

- `tcp://broker.hivemq.com:1883`
- `tcp://test.mosquitto.org:1883`
- `tcp://broker.emqx.io:1883`

---

💡 **Tip**: Usa `#` para suscribirte a todos los subtopics (ej: `test/#`)

