package com.example.rooyify.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("RooyifySession", Context.MODE_PRIVATE)

    fun saveUserSession(userId: String, name: String, email: String, role: String) {
        prefs.edit().apply {
            putString("USER_ID", userId)
            putString("USER_NAME", name)
            putString("USER_EMAIL", email)
            putString("USER_ROLE", role)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean("IS_LOGGED_IN", false)
    }

    fun getUserId(): String? {
        return prefs.getString("USER_ID", null)
    }
    
    fun getUserName(): String? {
        return prefs.getString("USER_NAME", null)
    }

    fun getUserEmail(): String? {
        return prefs.getString("USER_EMAIL", null)
    }

    fun getUserRole(): String? {
        return prefs.getString("USER_ROLE", null)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
