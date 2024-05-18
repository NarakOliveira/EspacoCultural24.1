package com.espaco.cultural.database.preferences

import android.content.Context

class SettingsPreferences(context: Context){
    companion object {
        private const val SETTING_OPTIONS = "setting_options"
        private const val NEED_PASSWORD = "need_password"
        private const val NEW_EXPOSITION = "new_exposition"
        private const val INTERACTIONS = "interactions"
    }

    private val settingsPreferences = context.getSharedPreferences(SETTING_OPTIONS, 0)
    private val settingsEditor = settingsPreferences.edit()

    var needPassword: Boolean
        set(value) { settingsEditor.putBoolean(NEED_PASSWORD, value); settingsEditor.apply() }
        get() = settingsPreferences.getBoolean(NEED_PASSWORD, false)

    var newExposition: Boolean
        set(value) { settingsEditor.putBoolean(NEW_EXPOSITION, value); settingsEditor.apply() }
        get() = settingsPreferences.getBoolean(NEW_EXPOSITION, true)

    var interactions: Boolean
        set(value) { settingsEditor.putBoolean(INTERACTIONS, value); settingsEditor.apply() }
        get() = settingsPreferences.getBoolean(INTERACTIONS, true)

   fun clear() {
       settingsEditor.clear()
       settingsEditor.apply()
   }
}