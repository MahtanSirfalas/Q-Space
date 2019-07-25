package com.ust.spaceq.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Suggests (
    val nickName:String,
    val question:String,
    val time: String
    ):Parcelable {
    constructor():this (
        "",
        "",
        ""
    )
}