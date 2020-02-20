package com.example.myrecorder.Controler

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Chronometer
import android.widget.ImageButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.arthenica.mobileffmpeg.FFmpeg
import com.example.myrecorder.Model.Records
import com.example.myrecorder.Model.RecordsViewModel
import com.example.myrecorder.R
import com.example.myrecorder.Utils.ItemClickSupport
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kotlinx.android.synthetic.main.main_fragment.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainFragment : Fragment() {


    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var progressBar: CircularProgressBar
    private lateinit var recordButton: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var chronometer: Chronometer
    private lateinit var file: File
    private lateinit var fileName: String
    private var text = "concat:"
    private lateinit var recorder: MediaRecorder
    private var state = State.ON_STOP
    private var PERMISSION_REQUEST_CODE = 1111
    private var permissionToRecordAccepted = false
    private lateinit var audioFile: File
    private var pausedAudioRecords = ArrayList<String>()
    private var recordingTime = 0L
    private lateinit var recordsViewModel: RecordsViewModel



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.main_fragment, container, false)


        // Bind Views

        progressBar = view.findViewById(R.id.progressBar)
        recordButton = view.findViewById(R.id.start_record_button)
        chronometer = view.findViewById(R.id.chronometer_main)
        pauseButton = view.findViewById(R.id.replay_pause_button)

        pauseButton.visibility = View.GONE

        //Configuration methods
        checkPermissions()
        configureChronometerFormat()
        configureRecordAndStopButton()

        //Create ViewModel
        recordsViewModel=ViewModelProvider(this).get(RecordsViewModel::class.java)


        return view

    }


    override fun onPause() {
        super.onPause()
        if (state== State.RECORDING)
            recorder.stop()

        recorder=MediaRecorder()
        recorder.release()
    }

    override fun onStop() {
        super.onStop()
        if (state== State.RECORDING)
            recorder.stop()

        recorder.release()
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()

    }

    /**
     * Chronometer, progressBar and button image function
     */

    private fun configureChronometerFormat() {

        chronometer.setOnChronometerTickListener { cArg ->
            val time = SystemClock.elapsedRealtime() - cArg.base
            val h = (time / 3600000).toInt()
            val m = (time - h * 3600000).toInt() / 60000
            val s = (time - h * 3600000 - m * 60000).toInt() / 1000
            val hh = if (h < 10) "0$h" else h.toString() + ""
            val mm = if (m < 10) "0$m" else m.toString() + ""
            val ss = if (s < 10) "0$s" else s.toString() + ""
            cArg.text = "$hh:$mm:$ss"
        }
        chronometer.text = getString(R.string.init_chronometer)
        progressBar.progressBarColor = Color.TRANSPARENT
    }

    private fun updateUiWhenRecordingStart() {
        when (state) {
            State.ON_STOP -> {
                chronometer.apply {
                    base = SystemClock.elapsedRealtime()
                    start()
                }
            }
            State.ON_PAUSE -> {
                chronometer.apply {
                    base = SystemClock.elapsedRealtime() + recordingTime
                    start()
                }
            }
            else -> {
            }
        }

        progressBar.progressBarColor = Color.rgb(183,28,28)
        recordButton.setImageResource(R.drawable.ic_stop_24px)
        replay_pause_button.setImageResource(R.drawable.ic_pause_24px)
        pauseButton.visibility = View.VISIBLE

    }

    private fun updateUiWhenRecordingStop() {

        when (state) {
            State.ON_STOP -> {
                chronometer.apply {
                    stop()
                    text = getString(R.string.init_chronometer)
                    pauseButton.visibility = View.GONE
                    recordButton.setImageResource(R.drawable.ic_fiber_manual_record_24px)
                }
            }
            State.ON_PAUSE -> {
                chronometer.stop()
            }
            else -> {
            }
        }

        progressBar.progressBarColor = Color.TRANSPARENT
        replay_pause_button.setImageResource(R.drawable.ic_play_arrow_24px)

    }

    private fun configureRecordAndStopButton() {
        recordButton.setOnClickListener {
            when (state) {
                State.ON_STOP -> {
                    updateUiWhenRecordingStart()
                    state = State.RECORDING
                    startRecording()
                }
                State.RECORDING -> {
                    state = State.ON_STOP
                    updateUiWhenRecordingStop()
                    stopRecording()
                }
                State.ON_PAUSE -> {
                    stopRecording()
                    state = State.ON_STOP
                    updateUiWhenRecordingStop()

                }
            }
        }
        pauseButton.setOnClickListener {
            when (state) {
                State.ON_STOP -> {
                }
                State.RECORDING -> {
                    state = State.ON_PAUSE
                    updateUiWhenRecordingStop()
                    pauseRecording()

                }
                State.ON_PAUSE -> {
                    updateUiWhenRecordingStart()
                    state = State.RECORDING
                    startRecording()
                }
            }
        }
    }

    /**
     * Recording functions
     */


    private fun startRecording() {

        val date = Calendar.getInstance().timeInMillis
        fileName = "$date.m4a"

        audioFile = File("${requireContext().getExternalFilesDir("Records")?.absolutePath}/$fileName")
        recorder= MediaRecorder()
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_2_TS)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(192000)
            setAudioSamplingRate(44100)
            setOutputFile(audioFile)
            prepare()
            start()
        }

    }

    private fun stopRecording() {
        writeFilePathForConcatenation()
        when (state) {
            State.ON_PAUSE -> {
                concatenatePausedRecord()
            }
            State.RECORDING -> {
                recorder.stop()
                concatenatePausedRecord()
            }
            State.ON_STOP -> {
                recorder.stop()
                concatenatePausedRecord()
            }

        }
    }

    private fun pauseRecording() {
        recorder.stop()
        recordingTime = chronometer.base - SystemClock.elapsedRealtime()
        writeFilePathForConcatenation()
    }

    /**
     * Concatenation method to handle pause when recording
     */

    private fun writeFilePathForConcatenation() {
        pausedAudioRecords.add(audioFile.path)
    }

    private fun concatenatePausedRecord() {
        if (pausedAudioRecords.size >= 2) {
            text = "concat:"
            for (s: String in pausedAudioRecords) {
                if (s != audioFile.path)
                    text += "$s|"
            }
            text += pausedAudioRecords[pausedAudioRecords.size - 1]

            val concatenatedRecordName = "${Calendar.getInstance().timeInMillis}[records].m4a"

            FFmpeg.execute("-i $text -c copy ${requireContext().getExternalFilesDir("Records")?.absolutePath}/$concatenatedRecordName  ")

            for (s: String in pausedAudioRecords) {
                file = File(s)
                file.delete()
            }
            pausedAudioRecords = ArrayList()
            val concatenatedAudioPath="${requireContext().getExternalFilesDir("Records")?.absolutePath}/$concatenatedRecordName"
            addToMediaStore(concatenatedAudioPath)
        }
        if (pausedAudioRecords.size == 1) {
            pausedAudioRecords = ArrayList()
            addToMediaStore(audioFile.absolutePath)
        }


    }

    /**
     * Permissions check functions
     */


    private fun checkPermissions() {

        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
            != PackageManager.PERMISSION_GRANTED
        )
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ), PERMISSION_REQUEST_CODE
            )


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == PERMISSION_REQUEST_CODE) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED
                    && grantResults[2] == PackageManager.PERMISSION_GRANTED

        } else false

        if (!permissionToRecordAccepted) {
            activity?.finish()
        }
    }

    /**
     * Add record to MediaStore for app persistance
     */

    private fun addToMediaStore(mFileName:String){


        val resolver = requireContext().applicationContext.contentResolver
        val newSongDetails = ContentValues().apply {
            put(MediaStore.Audio.Media.DATA, mFileName)
            put(MediaStore.Audio.Media.MIME_TYPE,"audio/m4a")
            put(MediaStore.Audio.Media.TITLE,"Records [${Calendar.getInstance().timeInMillis}]" )
        }

        val collection=MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        val myFavoriteSongUri = resolver
            .insert(collection, newSongDetails)

        val tempsMediaPlayer=MediaPlayer().apply {
            setDataSource(requireContext(),myFavoriteSongUri!!)
            prepare()
        }
        val duration=tempsMediaPlayer.duration
        tempsMediaPlayer.release()

        val calendar=Calendar.getInstance(Locale.FRANCE)


        val dateFormat=SimpleDateFormat("dd/MM/yyyy HH:mm")
        val creationDate=dateFormat.format(calendar.timeInMillis)

        val record=Records(mFileName,"Records [${Calendar.getInstance().timeInMillis}]",false,duration,0,creationDate)
        recordsViewModel.insert(record)


        val intent=Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        intent.data=myFavoriteSongUri
        requireContext().applicationContext.sendBroadcast(intent)

    }

}

enum class State(val state: String) {
    RECORDING("Recording"),
    ON_PAUSE("On Pause "),
    ON_STOP("On stop")
}

