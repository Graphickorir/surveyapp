package com.pula.surveyapp.room

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface QueDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(que: Que)

    @Update
    fun update(que: Que)

    @Delete
    fun delete(que: Que)

    @Query("delete from Que")
    fun deleteAllQuestions()

    @Query("select * from Que order by id asc")
    fun getAllQuestions(): List<Que>
}