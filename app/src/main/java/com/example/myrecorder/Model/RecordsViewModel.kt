package com.example.myrecorder.Model

import android.app.Application
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel

import androidx.lifecycle.viewModelScope
import com.example.myrecorder.Database.RecordsDataBase
import com.example.myrecorder.Repository.RecordsRepository
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import java.io.File

class RecordsViewModel(application: Application): AndroidViewModel(application) {

    private val recordsRepository:RecordsRepository
    val allRecords:LiveData<List<Records>>

    init {
     val recordDao=RecordsDataBase.getDataBase(application).recordDao()
        recordsRepository= RecordsRepository(recordDao)
        allRecords=recordsRepository.allRecords

    }


    fun insert(record:Records)=viewModelScope.launch(newSingleThreadContext("name")) {
        recordsRepository.insert(record)
    }

    fun update(record: Records)=viewModelScope.launch(newSingleThreadContext("name")) {
        recordsRepository.updateRecord(record)
    }

    fun delete(record: Records)=viewModelScope.launch (newSingleThreadContext("name")){
        File(record.uri).absoluteFile.delete()
        recordsRepository.deleteRecord(record)
    }
}