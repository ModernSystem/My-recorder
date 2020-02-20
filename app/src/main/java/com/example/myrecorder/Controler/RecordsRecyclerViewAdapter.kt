package com.example.myrecorder.Controler

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myrecorder.Model.Records
import com.example.myrecorder.R

import java.text.SimpleDateFormat
import java.util.concurrent.TimeUnit


class RecordsRecyclerViewAdapter(recordList:List<Records>,
                                 private val mListener: PlayButtonListener,
                                 private val seekBarCallBack: SeekBarCallBack) :
    RecyclerView.Adapter<RecordsRecyclerViewAdapter.RecordsViewHolder>() {

    private val mRecordList=recordList


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordsViewHolder {
        val inflater=LayoutInflater.from(parent.context)
        val view=inflater.inflate(R.layout.record_item,parent,false)
        return RecordsViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mRecordList.size
    }

    override fun onBindViewHolder(holder: RecordsViewHolder, position: Int) {
        holder.updateUiWihInfo(mRecordList.get(position))
        holder.recordSeekBar.setOnSeekBarChangeListener(object:SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser){
                    seekBarCallBack.onSeekBarCallback(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        holder.playButton.setOnClickListener {
            mListener.onPlayButtonClicked(mRecordList.get(position),position)
        }
    }


    class RecordsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val recordName:TextView
        val recordSeekBar:SeekBar
        var playButton:ImageButton
        private val recordDate:TextView
        private val recordDuration:TextView
        private lateinit var record:Records


        init {
            with(itemView){
                recordName=findViewById(R.id.recordName)
                recordSeekBar=findViewById(R.id.recordProgressBar)
                playButton=findViewById(R.id.play_record_button)
                recordDate=findViewById(R.id.recordDate)
                recordDuration=findViewById(R.id.recordDuration)
            }
        }

        fun updateUiWihInfo(mRecords: Records){
            record=mRecords
            recordName.text=mRecords.name
            recordDate.text=mRecords.creationDate
            recordDuration.text=timeFormat((mRecords.duration).toLong())

            if (RecordingsFragment.previousRecords.uri==mRecords.uri){
                recordDuration.text= "${timeFormat(RecordingsFragment.previousRecords.timeStop.toLong())}/${timeFormat((mRecords.duration).toLong())}"
            }

            if (RecordingsFragment.previousRecords.isPlaying&&RecordingsFragment.previousRecords.uri==mRecords.uri) {
                if (RecordingsFragment.previousRecords.timeStop!=0)
                    recordSeekBar.visibility=View.VISIBLE
                playButton.setImageResource(R.drawable.ic_pause_circle_outline_24px)
                recordSeekBar.max=record.duration
                recordSeekBar.progress=RecordingsFragment.previousRecords.timeStop
            }
            else {
                if (RecordingsFragment.previousRecords.timeStop!=0&&RecordingsFragment.previousRecords.uri==mRecords.uri)
                    recordSeekBar.visibility=View.VISIBLE
                recordSeekBar.max=record.duration
                recordSeekBar.progress=RecordingsFragment.previousRecords.timeStop
                playButton.setImageResource(R.drawable.ic_play_circle_outline_24px)
            }
        }

        fun timeFormat(millis:Long):String{
            return String.format(
                "%02d:%02d:%02d", TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) % TimeUnit.HOURS.toMinutes(1),
                TimeUnit.MILLISECONDS.toSeconds(millis) % TimeUnit.MINUTES.toSeconds(1)
            )
        }
    }
}