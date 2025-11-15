package com.example.home

import android.util.Log

/**
 * Utilidad de logging que respeta BuildConfig.ENABLE_VERBOSE_LOGGING
 * En modo release, solo se registran errores (Log.e) para ahorrar batería
 */
object AppLog {

    private const val ENABLED = true

    fun i(tag: String, message: String) {
        if (ENABLED) {
            Log.i(tag, message)
        }
    }

    fun d(tag: String, message: String) {
        if (ENABLED) {
            Log.d(tag, message)
        }
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        // Warnings siempre se registran (pueden indicar problemas)
        if (throwable != null) {
            Log.w(tag, message, throwable)
        } else {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        // Errores siempre se registran
        if (throwable != null) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
        }
    }
}
