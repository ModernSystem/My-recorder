package com.example.myrecorder.Database.DAO

import android.net.Uri
import android.os.FileObserver.DELETE
import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.myrecorder.Model.Records

@Dao
interface RecordsDao {

    @Query("SELECT*FROM records")
    fun getAllRecords() :LiveData<List<Records>>

    @Update
    fun updateRecord(records: Records)

    @Insert(onConflict= OnConflictStrategy.IGNORE)
    fun insert(records: Records)

    @Query("DELETE FROM records WHERE uri=:mUri")
    fun deleteRecord(mUri:String)

}