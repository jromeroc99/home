# Estructura del Proyecto MQTT Client

Este documento describe la organización del código de la aplicación MQTT Client.

## 📁 Estructura de Archivos

```
app/src/main/java/com/example/home/
│
├── MainActivity.kt                      # Actividad principal
├── MqttHelper.kt                        # Cliente MQTT (ya existente)
│
├── models/
│   └── ReceivedMessage.kt              # Modelo de datos para mensajes
│
└── ui/
    ├── MqttApp.kt                      # Pantalla principal con navegación
    │
    ├── screens/
    │   ├── ConnectionTab.kt            # Pestaña de conexión
    │   ├── SubscriptionTab.kt          # Pestaña de suscripción
    │   └── PublishTab.kt               # Pestaña de publicación
    │
    └── theme/
        └── ...                         # Temas de la app (ya existente)
```

## 📋 Descripción de Componentes

### 🎯 MainActivity.kt
**Responsabilidad**: Gestión del ciclo de vida y coordinación

- Maneja el ciclo de vida de la aplicación (`onCreate`, `onDestroy`)
- Gestiona la instancia de `MqttHelper`
- Persiste y carga datos con `SharedPreferences`
- Implementa conexión automática al iniciar
- Delega operaciones MQTT a métodos específicos (`handleConnect`, `handlePublish`, etc.)

**Métodos principales**:
- `handleConnect()` - Conecta al broker MQTT
- `handleDisconnect()` - Desconecta del broker
- `handlePublish()` - Publica mensajes
- `handleSubscribe()` - Se suscribe a topics
- `handleUnsubscribe()` - Se desuscribe de topics
- `loadPreferences()` - Carga configuración guardada
- `savePreferences()` - Guarda configuración actual
- `connectAutomatically()` - Reconexión automática

---

### 🎨 ui/MqttApp.kt
**Responsabilidad**: Pantalla principal y navegación

- Contiene el `Scaffold` principal con `TopAppBar`
- Implementa sistema de pestañas (`TabRow`)
- Maneja el estado global de la UI (conexión, suscripciones, mensajes)
- Coordina la navegación entre pestañas
- Distribuye callbacks a las pantallas hijas

**Estado gestionado**:
- Datos de conexión (serverUri, username, password)
- Estado de conexión (isConnected)
- Topics de suscripción y publicación
- Lista de mensajes recibidos
- Pestaña seleccionada

---

### 🔌 ui/screens/ConnectionTab.kt
**Responsabilidad**: Configuración de conexión MQTT

**Componentes**:
- `ConnectionTab()` - Composable principal
- `ConnectionConfigCard()` - Card con formulario de conexión
- `ConnectionInfoCard()` - Card con información de ayuda

**Funcionalidades**:
- Campos de entrada para URI, usuario y contraseña
- Botones de conectar/desconectar
- Indicador visual de estado de conexión
- Tips sobre tipos de conexión (tcp://, ssl://, mqtts://)
- Información sobre persistencia automática

---

### 📡 ui/screens/SubscriptionTab.kt
**Responsabilidad**: Suscripción y visualización de mensajes

**Componentes**:
- `SubscriptionTab()` - Composable principal
- `SubscriptionControlCard()` - Control de suscripciones
- `MessagesHeaderCard()` - Header de mensajes con contador
- `MessageCard()` - Card individual de mensaje

**Funcionalidades**:
- Campo de entrada para topic de suscripción
- Botones de suscribirse/desuscribirse
- Lista de topics activos suscritos
- Visualización de mensajes recibidos con:
  - Topic del mensaje
  - Payload (contenido)
  - Timestamp formateado
- Botón para limpiar historial de mensajes
- Mensaje cuando no hay mensajes

---

### 📤 ui/screens/PublishTab.kt
**Responsabilidad**: Publicación de mensajes MQTT

**Componentes**:
- `PublishTab()` - Composable principal
- `PublishControlCard()` - Formulario de publicación
- `PublishInfoCard()` - Tips sobre publicación

**Funcionalidades**:
- Campo de entrada para topic de publicación
- Campo de texto multilínea para el mensaje
- Botón de publicar (habilitado solo si hay conexión y datos)
- Consejos sobre:
  - Publicación sin suscripción
  - Topics jerárquicos
  - Entrega a suscriptores

---

### 📦 models/ReceivedMessage.kt
**Responsabilidad**: Modelo de datos

```kotlin
data class ReceivedMessage(
    val topic: String,
    val payload: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

Representa un mensaje MQTT recibido con su metadata.

---

## 🔄 Flujo de Datos

### Conexión:
```
Usuario → ConnectionTab → MqttApp → MainActivity.handleConnect() → MqttHelper.connect()
```

### Suscripción:
```
Usuario → SubscriptionTab → MqttApp → MainActivity.handleSubscribe() → MqttHelper.subscribe()
```

### Recepción de Mensajes:
```
MqttHelper → callback → handleSubscribe → MqttApp (actualiza lista) → SubscriptionTab (muestra)
```

### Publicación:
```
Usuario → PublishTab → MqttApp → MainActivity.handlePublish() → MqttHelper.publish()
```

### Persistencia:
```
onDestroy → savePreferences() → SharedPreferences
onCreate → loadPreferences() → SharedPreferences → connectAutomatically()
```

---

## 🎯 Ventajas de esta Arquitectura

### ✅ Separación de Responsabilidades
- Cada archivo tiene una única responsabilidad clara
- Fácil de entender y mantener

### ✅ Modularidad
- Los componentes son independientes
- Fácil de testear individualmente
- Reutilización de componentes

### ✅ Escalabilidad
- Agregar nuevas pantallas es simple (nueva pestaña)
- Agregar funcionalidades no afecta otros componentes
- Estructura preparada para crecimiento

### ✅ Mantenibilidad
- Código organizado y documentado
- Búsqueda rápida de funcionalidades
- Modificaciones aisladas

### ✅ Legibilidad
- Nombres descriptivos
- Jerarquía clara
- Documentación inline

---

## 🛠️ Convenciones de Código

### Nombres de Archivos
- `PascalCase` para clases y archivos
- Sufijo `Tab` para pantallas de pestañas
- Sufijo `Card` para componentes de UI

### Organización de Composables
- Composable público principal primero
- Composables privados después con prefijo `private`
- Ordenados por jerarquía visual

### Comentarios
- KDoc (`/** */`) para funciones públicas
- Comentarios inline para lógica compleja
- Secciones separadas con comentarios de línea

### Parámetros
- Estados como `remember { mutableStateOf() }`
- Callbacks con prefijo `on` (onConnect, onPublish, etc.)
- Nombres descriptivos y claros

---

## 🚀 Próximas Mejoras Sugeridas

1. **ViewModel**: Separar la lógica de negocio de la UI
2. **Repository**: Abstraer el acceso a datos y preferencias
3. **Dependency Injection**: Usar Hilt o Koin
4. **Testing**: Tests unitarios y de UI
5. **Encriptación**: Usar EncryptedSharedPreferences para credenciales
6. **Estados**: Implementar sealed classes para estados de UI
7. **Navigation**: Usar Jetpack Navigation si se agregan más pantallas
8. **Logging**: Sistema de logs estructurado

---

## 📚 Recursos

- [Jetpack Compose](https://developer.android.com/jetpack/compose)
- [Material 3 Design](https://m3.material.io/)
- [MQTT Protocol](https://mqtt.org/)
- [Eclipse Paho](https://www.eclipse.org/paho/)

---

*Última actualización: 2025-11-15*

