package com.example.myrecorder.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myrecorder.Database.DAO.RecordsDao
import com.example.myrecorder.Model.Records

@Database(entities = [Records::class],version=1,exportSchema = false)
abstract class RecordsDataBase:RoomDatabase() {

    abstract fun recordDao():RecordsDao

    companion object{
        @Volatile
        private var INSTANCE:RecordsDataBase?=null

        fun getDataBase(context: Context):RecordsDataBase{
            val tempInstance= INSTANCE
            if(tempInstance!=null){
                return tempInstance
            }
            synchronized(this){
                return Room.databaseBuilder(
                    context.applicationContext,
                    RecordsDataBase::class.java,
                    "Record Database")
                    .build()
            }
        }
    }


}