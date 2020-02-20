package com.example.myrecorder.Model
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "records")
data class Records(
    var uri: String,
    var name:String,
    var isPlaying:Boolean=false,
    var duration: Int,
    var timeStop:Int=0,
    val creationDate:String="",
    @PrimaryKey(autoGenerate = true)
    var id:Int=0
    )