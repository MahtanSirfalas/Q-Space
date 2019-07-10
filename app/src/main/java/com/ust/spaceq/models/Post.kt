package com.ust.spaceq.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class Post (
    val nickName: String,
    val date: String,
    val time: String,
    val post: String,
    val uid: String,
    val upvoteCount: Int,
    val upvotes: List<String>
    ):Parcelable {
    constructor() : this(
        "",
        "",
        "",
        "",
        "",
        0,
        listOf("")
    )
}