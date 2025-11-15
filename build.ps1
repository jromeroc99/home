# Script para compilar el proyecto MQTT Android
# Uso: .\build.ps1

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Compilando Aplicación MQTT para Android" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Configurar JAVA_HOME
$javaPath = "C:\Program Files\Android\Android Studio\jbr"
if (Test-Path $javaPath) {
    $env:JAVA_HOME = $javaPath
    Write-Host "✓ JAVA_HOME configurado: $javaPath" -ForegroundColor Green
} else {
    Write-Host "✗ No se encontró JBR de Android Studio" -ForegroundColor Red
    Write-Host "  Por favor, instala Android Studio o configura JAVA_HOME manualmente" -ForegroundColor Yellow
    exit 1
}

Write-Host ""
Write-Host "Compilando proyecto..." -ForegroundColor Yellow

# Compilar
.\gradlew assembleDebug

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Green
    Write-Host "  ✓ Compilación exitosa!" -ForegroundColor Green
    Write-Host "================================================" -ForegroundColor Green
    Write-Host ""
    Write-Host "APK generado en:" -ForegroundColor Cyan
    Write-Host "  app\build\outputs\apk\debug\app-debug.apk" -ForegroundColor White
    Write-Host ""
    Write-Host "Para instalar en dispositivo conectado:" -ForegroundColor Cyan
    Write-Host "  .\gradlew installDebug" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "================================================" -ForegroundColor Red
    Write-Host "  ✗ Error en la compilación" -ForegroundColor Red
    Write-Host "================================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Revisa los errores arriba para más detalles" -ForegroundColor Yellow
    exit 1
}

