package com.ust.qspace.models

import android.content.Context

const val whiteFont = "WHITE_FONTS"
const val simpleUi = "SIMPLE_UI"

class SettingsPrefs(context: Context) {

    val PREF_NAME = "SharedSettingPref"

    val preference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getSetting(setting:String):Boolean{
        return preference.getBoolean(setting, false)
    }

    fun setSetting(setting: String, eder: Boolean){
        val editor = preference.edit()
        editor.putBoolean(setting, eder)
        editor.apply()
    }
}