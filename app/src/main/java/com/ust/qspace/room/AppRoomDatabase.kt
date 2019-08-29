package com.ust.qspace.room

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [AppRoomEntity::class],version = 1, exportSchema = false)
abstract class AppRoomDatabase: RoomDatabase() {

    abstract fun stageDao():AppRoomDao

}