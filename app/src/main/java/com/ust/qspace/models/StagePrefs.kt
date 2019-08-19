package com.ust.qspace.models

import android.content.Context

class StagePrefs(context: Context) {

    val PREF_NAME = "SharedOfflinePref"

    val preference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val editor = preference.edit()

    fun getPointsPref(stage:String):Long{
        return preference.getLong(stage, 0)
    }

    fun setPointsPref(stage: String, sPoint:Long){
        editor.putLong(stage, sPoint)
        editor.apply()
    }

    fun getStagePref(stage: String):Boolean{
        return preference.getBoolean(stage, true)
    }

    fun setStagePref(stage: String, eder: Boolean){
        editor.putBoolean(stage, eder)
        editor.apply()
    }
}