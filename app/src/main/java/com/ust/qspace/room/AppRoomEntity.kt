package com.ust.qspace.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stage_table")
data class AppRoomEntity (
    @PrimaryKey
    var db_stage_id:Int,
    @ColumnInfo
    var db_stage_name:String,
    @ColumnInfo
    var db_stage_points:Int,
    @ColumnInfo
    var db_stage_control:Boolean
){}