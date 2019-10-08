package com.ust.qspace.models

import android.content.Context

const val whiteFont = "WHITE_FONTS"
const val simpleUi = "SIMPLE_UI"
const val playMusic = "PLAY_MUSIC"
const val metUfo = "MET_UFO"

class SettingsPrefs(context: Context) {

    private val PREF_NAME = "SharedSettingPref"

    private val preference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    fun getSetting(setting:String):Boolean{
        return if (setting == playMusic){
            preference.getBoolean(setting, true)
        }else{
            preference.getBoolean(setting, false)
        }
    }

    fun setSetting(setting: String, eder: Boolean){
        val editor = preference.edit()
        editor.putBoolean(setting, eder)
        editor.apply()
    }

    fun removeSetting(setting: String){
        val editor = preference.edit()
        editor.remove(setting)
        editor.apply()
    }
}