package com.unired.util

import android.content.Context
import android.content.SharedPreferences

object SessionManager {
    private const val PREFS_NAME = "unired_session"
    private const val KEY_TOKEN = "token"
    private const val KEY_USER_ID = "user_id"

    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
    }

    fun getToken(): String? = prefs.getString(KEY_TOKEN, null)

    fun saveUserId(userId: Int) {
        prefs.edit().putInt(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): Int = prefs.getInt(KEY_USER_ID, -1)

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(): Boolean = getToken() != null
}
