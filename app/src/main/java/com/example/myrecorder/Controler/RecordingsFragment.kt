package com.example.myrecorder.Controler


import android.app.AlertDialog
import android.content.*
import android.icu.text.AlphabeticIndex
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.ViewModelProvider

import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecorder.Model.CustomMediaPlayer
import com.example.myrecorder.Model.Records
import com.example.myrecorder.Model.RecordsViewModel

import com.example.myrecorder.R
import com.example.myrecorder.Utils.ItemClickSupport
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
class RecordingsFragment : Fragment(),PlayButtonListener,SeekBarCallBack,RecordModifierListDialogFragment.Listener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var recordsViewModel: RecordsViewModel
    private lateinit var focusListener: AudioManager.OnAudioFocusChangeListener
    private lateinit var audioManager:AudioManager
    private lateinit var focusRequest: AudioFocusRequest
    private var newName:String?=null
    // Initialize mediaPlayer
    private lateinit var mediaPlayer:MediaPlayer

    private var recordList= listOf<Records>()
    private var position=0
    private var clickedPosition=0

    companion object {
        var handler = Handler()
        lateinit var runnable: Runnable
        var previousRecords = Records(Uri.EMPTY.toString(), "", false, 0)
    }



    /**
     * Requesting AudioFocus
     */

    private fun audioFocus(){

        audioManager=requireActivity().applicationContext.getSystemService(Context.AUDIO_SERVICE)as AudioManager

        focusListener=AudioManager.OnAudioFocusChangeListener{
            when (it){
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK->stopMedia()
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT->stopMedia()
                AudioManager.AUDIOFOCUS_LOSS->stopMedia()
            }
        }
        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
            setAudioAttributes(AudioAttributes.Builder().run {
                setUsage(AudioAttributes.USAGE_MEDIA)
                setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                build()
            })
            setAcceptsDelayedFocusGain(false)
            setOnAudioFocusChangeListener(focusListener)
            build()
        }


        audioManager.requestAudioFocus(focusRequest)


    }

    /**
     * Handle fragment state
     */

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view= inflater.inflate(R.layout.fragment_recordings, container, false)

        // create ViewModel
        recordsViewModel= ViewModelProvider(this).get(RecordsViewModel::class.java)

        recyclerView=view.findViewById(R.id.records_recyclerView)
        getRecordedAudioFiles()
        recyclerView.layoutManager=LinearLayoutManager(requireContext())
        val divider=DividerItemDecoration(recyclerView.context,DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(divider)

        runnable= Runnable {
            previousRecords.timeStop=mediaPlayer.currentPosition
            recyclerView.adapter?.notifyItemChanged(position)
            handler.postDelayed(runnable,0)
        }



        return view
    }

    override fun onStop() {
        super.onStop()
        Log.d("tag","STOOOOOOOOOOOOOOP")
        previousRecords = Records(Uri.EMPTY.toString(), "", false, 0)
        mediaPlayer.reset()
        reInitRecords()
        if (this::audioManager.isInitialized)
        audioManager.abandonAudioFocusRequest(focusRequest)
        if (handler.hasCallbacks(runnable))
            handler.removeCallbacks(runnable)
    }

    override fun onStart() {
        super.onStart()
        if (!this::mediaPlayer.isInitialized){
            initMediaPlayer()
        }
        val listener=ItemClickSupport.OnItemLongClickListener { _, position, _ ->
            if (mediaPlayer.isPlaying) {mediaPlayer.pause()}
            RecordModifierListDialogFragment.newInstance(1,this).show(requireActivity().supportFragmentManager, "dialog")
            clickedPosition=position
            true
        }

        //Item click support
        ItemClickSupport.addTo(recyclerView).setOnItemLongClickListener(listener)

    }

    /**
     * Retrieve all records, and Reinit track positon when fragmen is stopped
     */

    private fun getRecordedAudioFiles(){
        recordsViewModel.allRecords.observe(this,this::updateRecordList)
    }

    private fun updateRecordList(mRecordList:List<Records>){
        recordList=mRecordList
        val adapter=RecordsRecyclerViewAdapter(recordList,this,this)
        recyclerView.adapter=adapter
    }

    private fun reInitRecords(){
        getRecordedAudioFiles()
        for (record in recordList ){
            record.timeStop=0
            record.isPlaying=false
            recordsViewModel.update(record)
        }
    }


    /**
     * Play button callback and start/pause/stop function
     */

    override fun onPlayButtonClicked(records: Records,mPosition: Int) {
        if (this::audioManager.isInitialized)
            audioManager.requestAudioFocus(focusRequest)
        position=mPosition
        when (mediaPlayer.isPlaying){
            false->{
                if (previousRecords.uri!=records.uri){
                    previousRecords.isPlaying=true
                    mediaPlayer.reset()
                    previousRecords.timeStop=0
                    recordsViewModel.update(previousRecords)
                    startPlayingNewRecord(records)
                    handler.postDelayed(runnable,0)
                }
                else {
                    previousRecords.isPlaying=true
                    recordsViewModel.update(records)
                    handler.postDelayed(runnable,0)
                    mediaPlayer.start()


                }
                }
            true->{
                if (previousRecords.uri==records.uri){
                    previousRecords.isPlaying=false
                    records.timeStop=mediaPlayer.currentPosition
                    handler.removeCallbacks(runnable)
                    recordsViewModel.update(records)
                    mediaPlayer.pause()


                }
                else {
                    handler.removeCallbacks(runnable)
                    previousRecords.isPlaying=false
                    previousRecords.timeStop=0
                    recordsViewModel.update(previousRecords)
                    mediaPlayer.stop()
                    mediaPlayer.reset()
                    startPlayingNewRecord(records)
                    handler.postDelayed(runnable,0)
                }
            }
        }

        }

    private fun initMediaPlayer(){
         mediaPlayer= CustomMediaPlayer().apply {
             setOnPreparedListener {
                 seekTo(previousRecords.timeStop)
                 start()
             }
             setOnCompletionListener {
                 previousRecords.isPlaying = false
                 handler.removeCallbacks(runnable)
                 previousRecords.timeStop = 0
                 recordsViewModel.update(previousRecords)
                 reset()
                 previousRecords = Records(Uri.EMPTY.toString(), "", false, 0)
             }
         }
    }

    private fun startPlayingNewRecord(records: Records){
        audioFocus()
        mediaPlayer.apply {
            setDataSource(requireContext(),records.uri.toUri())
            prepare()
        }
        previousRecords=records
        records.isPlaying=true
        recordsViewModel.update(records)
    }

    private fun stopMedia(){
        if (mediaPlayer.isPlaying) {
            mediaPlayer.pause()
            previousRecords.isPlaying=false
        }
    }

    /**
     * SeekBar callBack to use progress with mediaPlayer.seekTo() method
     */

    override fun onSeekBarCallback(progress: Int) {

            mediaPlayer.apply {
                seekTo(progress)
        }
        previousRecords.timeStop=progress
        recyclerView.adapter?.notifyItemChanged(position)

    }

    override fun onRecordModifierClicked(position: Int) {

        when (position){
            1->recordsViewModel.delete(recordList.get(clickedPosition))
            2->renameDialog()
            3->shareRecord(recordList.get(clickedPosition))
        }

    }

    private fun renameDialog() {
        val inflater=requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.rename_editext,null)
        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("ok") { dialog, _ ->
                val editText: EditText = dialogView.findViewById(R.id.rename_EditText)
                newName = editText.text.toString()

                if(newName!=null && newName!!.length>2){
                    val record=recordList.get(clickedPosition)
                    renameRecord(newName,record)

                    dialog.dismiss()
                }
                else {
                    Toast.makeText(requireContext(),"Choose a longer name",Toast.LENGTH_LONG).show()
                    renameDialog()
                }

            }
            .setNegativeButton("Cancel"){ dialog, _ ->
                dialog.dismiss()
            }.show()

        }

    private fun renameRecord(newName:String?, record: Records){
        record.name=newName!!

        var file=File(record.uri)
        var newFile=File(file.parent,"$newName.m4a")
        file.renameTo(newFile)
        record.uri=newFile.path
        recordsViewModel.update(record)
    }

    private fun shareRecord(records: Records){

        val uri=FileProvider.getUriForFile(requireContext(),"com.example.myrecorder.fileprovider", File(records.uri))
        val sharingIntent= Intent(Intent.ACTION_SEND)
        sharingIntent.type="text/*"
        sharingIntent.putExtra(Intent.EXTRA_STREAM,uri)
        startActivity(Intent.createChooser(sharingIntent,"share record"))
    }
}

/**
 * Callback interface
 */

interface PlayButtonListener{
    fun onPlayButtonClicked(records: Records,mPosition:Int)
}
interface SeekBarCallBack{
    fun onSeekBarCallback(progress:Int)
}




