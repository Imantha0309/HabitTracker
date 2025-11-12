package com.example.habittracker.auth

import android.content.Context
import java.security.MessageDigest

object SessionManager {
    private const val PREFS = "auth_prefs"
    private const val KEY_NAME = "user_name"
    private const val KEY_EMAIL = "user_email"
    private const val KEY_PASSWORD = "user_password" // store hash
    private const val KEY_LOGGED_IN = "is_logged_in"

    private fun prefs(ctx: Context) =
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun isLoggedIn(ctx: Context): Boolean =
        prefs(ctx).getBoolean(KEY_LOGGED_IN, false)

    fun currentUserName(ctx: Context): String? =
        prefs(ctx).getString(KEY_NAME, null)

    fun currentUserEmail(ctx: Context): String? =
        prefs(ctx).getString(KEY_EMAIL, null)

    fun register(ctx: Context, name: String, email: String, password: String): Boolean {
        val hash = sha256(password)
        prefs(ctx).edit()
            .putString(KEY_NAME, name.trim())                // ✅ Save full name
            .putString(KEY_EMAIL, email.trim().lowercase())  // still save email
            .putString(KEY_PASSWORD, hash)
            .apply()
        return true
    }

    fun login(ctx: Context, email: String, password: String): Boolean {
        val savedEmail = prefs(ctx).getString(KEY_EMAIL, null)?.lowercase()
        val savedHash = prefs(ctx).getString(KEY_PASSWORD, null)
        val ok = (savedEmail == email.trim().lowercase() && savedHash == sha256(password))
        if (ok) {
            prefs(ctx).edit().putBoolean(KEY_LOGGED_IN, true).apply()
        }
        return ok
    }

    fun logout(ctx: Context) {
        prefs(ctx).edit().putBoolean(KEY_LOGGED_IN, false).apply()
    }

    private fun sha256(s: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val bytes = md.digest(s.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
