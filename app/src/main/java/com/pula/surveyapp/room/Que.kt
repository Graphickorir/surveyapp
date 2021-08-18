package com.pula.surveyapp.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Que (
    val name: String,
    val question_id: String,
    val question_text: String,
    val options: String,
    var answer: String,
    @PrimaryKey(autoGenerate = true) val id: Int? = null
)