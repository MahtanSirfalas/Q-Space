package com.ust.qspace.room

import androidx.room.*


@Dao
interface AppRoomDao {

    @Insert
    fun insert (stage: AppRoomEntity)

    @Query("SELECT * FROM stage_table ORDER BY db_stage_id ASC")
    fun getAll():List<AppRoomEntity>

    @Delete
    fun delete(stage: AppRoomEntity)

    @Update
    fun update(stage: AppRoomEntity)

    @Query("SELECT * FROM stage_table WHERE db_stage_name LIKE :stage")
    fun getOne(stage: String): AppRoomEntity

}