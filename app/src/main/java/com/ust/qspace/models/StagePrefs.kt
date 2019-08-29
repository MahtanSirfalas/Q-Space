package com.ust.qspace.models

import android.content.Context
import com.ust.qspace.uid

class StagePrefs(context: Context) {

    val PREF_NAME = uid

    val preference = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    val editor = preference.edit()

    fun getPointsPref(stage: String):Long{
        return preference.getLong("P$stage", 0L)
    }

    fun setPointsPref(stage: String, sPoint:Long){
        editor.putLong("P$stage", sPoint)
        editor.apply()
    }

    fun getStagePref(stage: String):Boolean{
        return preference.getBoolean("B$stage", true)
    }

    fun setStagePref(stage: String, eder: Boolean){
        editor.putBoolean("B$stage", eder)
        editor.apply()
    }
}