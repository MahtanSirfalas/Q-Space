package com.ust.qspace.models

import android.content.Context

const val whiteFont = "WHITE_FONTS"
const val simpleUi = "SIMPLE_UI"
const val playMusic = "PLAY_MUSIC"

class SettingsPrefs(context: Context) {

    val PREF_NAME = "SharedSettingPref"

    val preference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getSetting(setting:String):Boolean{
        var booly = false
        if (setting == playMusic){
            booly = true
        }
        return preference.getBoolean(setting, booly)
    }

    fun setSetting(setting: String, eder: Boolean){
        val editor = preference.edit()
        editor.putBoolean(setting, eder)
        editor.apply()
    }
}