package com.ust.spaceq.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Post (val nickName: String, val date: String, val time: String,val post: String,
            val avatar: String, val uid: String):Parcelable {
    constructor() : this("", "", "", "", "", "")
}
