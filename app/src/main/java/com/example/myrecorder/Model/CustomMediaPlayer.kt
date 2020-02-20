package com.example.myrecorder.Model

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri

object Singleton class CustomMediaPlayer:MediaPlayer() {

    var uri:Uri?=null
    var context:Context?=null

    override fun setDataSource(context: Context, uri: Uri) {
        super.setDataSource(context, uri)
        this.uri=uri
        this.context=context
    }

    fun getPlayingUri():Uri{
        return uri!!
    }

}