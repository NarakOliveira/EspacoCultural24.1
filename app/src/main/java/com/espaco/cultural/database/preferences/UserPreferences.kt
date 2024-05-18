package com.espaco.cultural.database.preferences

import android.content.Context

class UserPreferences(context: Context){
    companion object {
        private const val USER_INFO = "user_info"

        private const val USER_REGISTRATION = "registration"
        private const val USER_NAME = "name"
        private const val USER_PICTURE = "picture"
        private const val USER_IS_ADMIN = "is_admin"
    }

    private val userPreferences = context.getSharedPreferences(USER_INFO, 0)
    private val userEditor = userPreferences.edit()

    var registration: String
        set(value) { userEditor.putString(USER_REGISTRATION, value); userEditor.apply() }
        get() = userPreferences.getString(USER_REGISTRATION, "") as String

    var name: String
        set(value) { userEditor.putString(USER_NAME, value); userEditor.apply() }
        get() = userPreferences.getString(USER_NAME, "") as String

    var picture: String
        set(value) { userEditor.putString(USER_PICTURE, value); userEditor.apply() }
        get() = userPreferences.getString(USER_PICTURE, "") as String

    var isAdmin: Boolean
        set(value) { userEditor.putBoolean(USER_IS_ADMIN, value); userEditor.apply() }
        get() = userPreferences.getBoolean(USER_IS_ADMIN, false)

    fun isLogged(): Boolean {
        return userPreferences.getString(USER_REGISTRATION, null) != null
    }

    fun clear() {
        userEditor.clear()
        userEditor.apply()
    }
}