package com.example.myrecorder.Repository

import androidx.lifecycle.LiveData
import com.example.myrecorder.Database.DAO.RecordsDao
import com.example.myrecorder.Model.Records
import java.io.File

class RecordsRepository(private val recordsDao: RecordsDao) {

    val allRecords=recordsDao.getAllRecords()

    fun insert(record:Records){
        recordsDao.insert(record)
    }

    fun deleteRecord(record: Records){
        val file = File(record.uri)
        file.delete()
        recordsDao.deleteRecord(record.uri)
    }

    fun updateRecord(record: Records){
        recordsDao.updateRecord(record)
    }


}