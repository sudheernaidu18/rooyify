package com.example.rooyify.network

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("RooyifySession", Context.MODE_PRIVATE)

    @JvmOverloads
    fun saveUserSession(
        userId: String, 
        name: String, 
        email: String, 
        role: String, 
        phone: String = "", 
        place: String = "", 
        dob: String = ""
    ) {
        prefs.edit().apply {
            putString("USER_ID", userId)
            putString("USER_NAME", name)
            putString("USER_EMAIL", email)
            putString("USER_ROLE", role)
            putString("USER_PHONE", phone)
            putString("USER_PLACE", place)
            putString("USER_DOB", dob)
            putBoolean("IS_LOGGED_IN", true)
            apply()
        }
    }

    fun updateUserSession(name: String, phone: String, place: String, dob: String) {
        prefs.edit().apply {
            putString("USER_NAME", name)
            putString("USER_PHONE", phone)
            putString("USER_PLACE", place)
            putString("USER_DOB", dob)
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

    fun getUserPhone(): String? {
        return prefs.getString("USER_PHONE", null)
    }

    fun getUserPlace(): String? {
        return prefs.getString("USER_PLACE", null)
    }

    fun getUserDob(): String? {
        return prefs.getString("USER_DOB", null)
    }

    fun checkAndUpdateStreak(): Triple<Int, Int, Int> {
        val todayStr = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
        val lastActive = prefs.getString("LAST_ACTIVE_DATE", null)
        var streak = prefs.getInt("STREAK_COUNT", 0)
        
        if (lastActive == null) {
            streak = 1
            prefs.edit().putString("LAST_ACTIVE_DATE", todayStr).putInt("STREAK_COUNT", streak).apply()
        } else if (lastActive != todayStr) {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
            try {
                val lastDate = sdf.parse(lastActive)
                val todayDate = sdf.parse(todayStr)
                if (lastDate != null && todayDate != null) {
                    val diffMs = todayDate.time - lastDate.time
                    val diffDays = diffMs / (1000 * 60 * 60 * 24)
                    
                    if (diffDays == 1L) {
                        streak += 1
                    } else if (diffDays > 1L) {
                        streak = 1
                    }
                } else {
                    streak = 1
                }
                prefs.edit().putString("LAST_ACTIVE_DATE", todayStr).putInt("STREAK_COUNT", streak).apply()
            } catch (e: Exception) {
                streak = 1
                prefs.edit().putString("LAST_ACTIVE_DATE", todayStr).putInt("STREAK_COUNT", streak).apply()
            }
        }
        
        // Update weekly logged days
        val loggedDays = prefs.getStringSet("LOGGED_DAYS", setOf<String>()) ?: setOf<String>()
        val updatedLoggedDays = loggedDays.toMutableSet()
        updatedLoggedDays.add(todayStr)
        
        // Filter to only keep last 7 days
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
        var todayDate: java.util.Date? = null
        try {
            todayDate = sdf.parse(todayStr)
        } catch (e: Exception) {}
        
        val finalLoggedDays = updatedLoggedDays.filter { dateStr ->
            if (todayDate == null) return@filter false
            try {
                val d = sdf.parse(dateStr)
                if (d != null) {
                    val diff = todayDate.time - d.time
                    val diffDays = diff / (1000 * 60 * 60 * 24)
                    diffDays < 7
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }.toSet()
        
        prefs.edit().putStringSet("LOGGED_DAYS", finalLoggedDays).apply()
        
        val loggedCount = finalLoggedDays.size
        val compliance = ((loggedCount / 7.0) * 100).toInt()
        
        return Triple(streak, loggedCount, compliance)
    }

    fun logout() {
        prefs.edit().clear().apply()
    }
}
