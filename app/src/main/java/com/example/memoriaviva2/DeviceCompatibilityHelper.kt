package com.example.memoriaviva2

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log

object DeviceCompatibilityHelper {
    private const val TAG = "DeviceCompatibility"
    
    /**
     * Safe SharedPreferences write - always use commit() for reliability
     */
    fun safeSharedPreferencesWrite(
        sharedPreferences: SharedPreferences,
        operation: (SharedPreferences.Editor) -> Unit
    ): Boolean {
        return try {
            val editor = sharedPreferences.edit()
            operation(editor)
            editor.commit() // Always use commit() for immediate write
        } catch (e: Exception) {
            Log.e(TAG, "Error writing to SharedPreferences", e)
            false
        }
    }
    
    /**
     * Safe SharedPreferences read with fallback
     */
    fun safeSharedPreferencesRead(
        context: Context,
        prefsName: String,
        key: String,
        defaultValue: Any
    ): Any {
        return try {
            val prefs = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            when (defaultValue) {
                is Boolean -> prefs.getBoolean(key, defaultValue)
                is String -> prefs.getString(key, defaultValue) ?: defaultValue
                is Int -> prefs.getInt(key, defaultValue)
                is Float -> prefs.getFloat(key, defaultValue)
                is Long -> prefs.getLong(key, defaultValue)
                else -> defaultValue
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading from SharedPreferences", e)
            defaultValue
        }
    }
    
    /**
     * Log device information for debugging
     */
    fun logDeviceInfo() {
        Log.i(TAG, "Device Info:")
        Log.i(TAG, "  Model: ${Build.MODEL}")
        Log.i(TAG, "  Manufacturer: ${Build.MANUFACTURER}")
        Log.i(TAG, "  Android Version: ${Build.VERSION.RELEASE}")
        Log.i(TAG, "  SDK Level: ${Build.VERSION.SDK_INT}")
    }
}