package com.pula.surveyapp.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database (entities = [Que::class], version = 1)
abstract class AppDb : RoomDatabase() {
    abstract fun QueDao(): QueDao

    companion object{
        @Volatile private var instance: AppDb? = null
        private val LOCK = Any()

        operator fun invoke(ctx: Context) = instance?: synchronized(LOCK){
            instance?: buildDb(ctx).also{
                instance = it
            }
        }

        private fun buildDb(ctx: Context) = Room.databaseBuilder(
            ctx.applicationContext,
            AppDb::class.java,
            "questionsDb"
        ).build()

    }
}