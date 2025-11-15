# Aplicación Cliente MQTT para Android

Esta es una aplicación Android completa que permite conectarse a un broker MQTT, suscribirse a topics y publicar mensajes.

## Características

- ✅ Conexión a brokers MQTT (TCP, SSL/TLS)
- ✅ Autenticación con usuario y contraseña (opcional)
- ✅ Suscripción a múltiples topics
- ✅ Publicación de mensajes
- ✅ Visualización de mensajes recibidos en tiempo real
- ✅ Interfaz moderna con Jetpack Compose y Material Design 3
- ✅ Desuscripción de topics
- ✅ Gestión de conexiones

## Uso

### 1. Conectarse a un Broker

1. Ingresa la URI del broker en el campo "Broker URI" (por defecto: `tcp://broker.hivemq.com:1883`)
   - Formatos soportados: `tcp://`, `ssl://`, `tls://`, `mqtts://`
2. Opcionalmente, ingresa usuario y contraseña si tu broker lo requiere
3. Presiona el botón **"Conectar"**
4. El estado se mostrará en verde cuando esté conectado

### 2. Suscribirse a un Topic

1. Asegúrate de estar conectado al broker
2. Ingresa el topic en el campo "Topic" (ejemplo: `test/topic`)
3. Presiona el botón **"Suscribirse"**
4. Los topics suscritos se mostrarán debajo de los botones

### 3. Publicar Mensajes

1. Asegúrate de estar conectado al broker
2. Ingresa el topic de destino en el campo "Topic"
3. Escribe tu mensaje en el campo "Mensaje"
4. Presiona el botón **"Publicar"**

### 4. Ver Mensajes Recibidos

- Todos los mensajes recibidos se mostrarán en la sección "Mensajes Recibidos"
- Cada mensaje muestra:
  - Topic de origen
  - Contenido del mensaje
  - Hora de recepción
- Puedes limpiar el historial con el botón **"Limpiar"**

### 5. Desconectarse

- Presiona el botón rojo **"Desconectar"** para cerrar la conexión
- Esto también eliminará todas las suscripciones activas

## Brokers Públicos para Pruebas

Puedes usar estos brokers MQTT públicos para pruebas:

- **HiveMQ**: `tcp://broker.hivemq.com:1883`
- **Eclipse Mosquitto**: `tcp://test.mosquitto.org:1883`
- **EMQX**: `tcp://broker.emqx.io:1883`

## Configuración del Proyecto

### Dependencias

El proyecto utiliza las siguientes librerías principales:

- **Kotlin 2.1.0**
- **Jetpack Compose** - UI moderna
- **Material 3** - Design system
- **Paho MQTT Android 4.4.2** - Cliente MQTT

### Permisos

La aplicación requiere los siguientes permisos (ya configurados en `AndroidManifest.xml`):

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Arquitectura

### Componentes Principales

1. **MainActivity.kt** - Actividad principal con UI Compose
2. **MqttHelper.kt** - Clase helper para operaciones MQTT (SIN MODIFICAR)
   - `connect()` - Establece conexión con el broker
   - `disconnect()` - Cierra la conexión
   - `publish(topic, message)` - Publica un mensaje
   - `subscribe(topic, callback)` - Se suscribe a un topic
   - `unsubscribe(topic)` - Cancela suscripción
   - `isConnected()` - Verifica estado de conexión

### MqttHelper - Características

El `MqttHelper` incluye:

- Reconexión automática
- Configuración optimizada para ahorro de batería
- Soporte para SSL/TLS
- Keep-alive de 5 minutos
- QoS configurable (por defecto: 1)
- Mensajes retenidos opcionales

## Compilación

### Requisitos

- Android Studio Hedgehog o superior
- JDK 11 o superior (se puede usar el JBR de Android Studio)
- Android SDK API 30 o superior

### Compilar desde Terminal

```powershell
# Configurar JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"

# Compilar
.\gradlew assembleDebug

# Instalar en dispositivo conectado
.\gradlew installDebug
```

### Compilar desde Android Studio

1. Abre el proyecto en Android Studio
2. Espera a que Gradle sincronice
3. Presiona el botón "Run" o usa `Shift + F10`

## APK Compilado

El APK de debug se genera en:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Notas Técnicas

- La aplicación usa `MqttAsyncClient` para operaciones no bloqueantes
- Los mensajes se procesan en callbacks asíncronos
- La UI se actualiza mediante `mutableStateOf` de Compose
- La conexión se cierra automáticamente al destruir la actividad

## Ejemplos de Uso

### Prueba Local

1. Conecta a `tcp://broker.hivemq.com:1883`
2. Suscríbete a `test/android/#`
3. Publica en `test/android/mensaje` con contenido "Hola desde Android"
4. Verás tu mensaje aparecer en la lista de mensajes recibidos

### Comunicación entre Dispositivos

1. En el dispositivo A y B, conecta al mismo broker
2. En ambos, suscríbete a `chat/general`
3. Publica mensajes desde cualquier dispositivo
4. Ambos recibirán los mensajes en tiempo real

## Solución de Problemas

### No se puede conectar
- Verifica que el broker esté activo
- Comprueba la conexión a internet
- Revisa si el broker requiere autenticación
- Consulta los logs con `adb logcat | grep MqttHelper`

### Mensajes no se reciben
- Verifica que estés suscrito al topic correcto
- Los topics son case-sensitive
- Usa comodines: `#` (multi-nivel) o `+` (un nivel)

### Error de compilación
- Asegúrate de tener JAVA_HOME configurado
- Limpia el proyecto: `.\gradlew clean`
- Invalida cachés en Android Studio

## Licencia

Este proyecto es de código abierto y está disponible para uso educativo y comercial.

