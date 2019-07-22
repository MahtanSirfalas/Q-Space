package com.ust.spaceq.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Users(
    val avatar:String,
    val eMail:String,
    val level: String,
    val nickName: String,
    val points: Long,
    val stages: String
) :
    Parcelable {
    constructor(): this("","","","",0, "")
}