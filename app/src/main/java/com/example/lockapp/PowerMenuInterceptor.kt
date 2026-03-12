package com.example.lockapp // <-- CAMBIADO A TU PAQUETE REAL

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.util.Log

class PowerMenuInterceptor : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        // TYPE_WINDOW_STATE_CHANGED detecta cuando aparecen diálogos o menús nuevos
        if (event.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            val packageName = event.packageName?.toString() ?: ""
            val className = event.className?.toString() ?: ""

            // Log para que puedas ver en la consola de Android Studio qué ventana se abre
            Log.d("PowerInterceptor", "Ventana detectada: $className de la app $packageName")

            // Detectar el menú de apagado (varía según la marca del móvil, estas son las más comunes)
            if (className.contains("GlobalActionsDialog") ||
                className.contains("ShutdownActivity") ||
                className.contains("PowerOff") ||
                packageName.contains("android.systemui")) {

                // Si la clase que se abre es del sistema y parece el menú de apagado:
                bloquearConPin()
            }
        }
    }

    private fun bloquearConPin() {
        val intent = Intent(this, PinActivity::class.java)
        // Flags necesarios para lanzar una pantalla desde un servicio y que sea única
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or
                Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
    }

    override fun onInterrupt() {}
}