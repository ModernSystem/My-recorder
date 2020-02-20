package com.example.myrecorder.Controler

import android.content.Context
import android.os.Bundle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.myrecorder.R
import kotlinx.android.synthetic.main.fragment_recordmodifier_list_dialog.*
import kotlinx.android.synthetic.main.fragment_recordmodifier_list_dialog_item.view.*

// TODO: Customize parameter argument names
const val ARG_ITEM_COUNT = "item_count"

/**
 *
 * A fragment that shows a list of items as a modal bottom sheet.
 *
 * You can show this modal bottom sheet from your activity like this:
 * <pre>
 *    RecordModifierListDialogFragment.newInstance(30).show(supportFragmentManager, "dialog")
 * </pre>
 *
 * You activity (or fragment) needs to implement [RecordModifierListDialogFragment.Listener].
 */
class RecordModifierListDialogFragment(var mListener: Listener) : BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_recordmodifier_list_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        list.layoutManager = LinearLayoutManager(context)
        list.adapter = RecordModifierAdapter(arguments!!.getInt(ARG_ITEM_COUNT))
    }


    interface Listener {
        fun onRecordModifierClicked(position: Int)
    }

    private inner class ViewHolder internal constructor(
        inflater: LayoutInflater,
        parent: ViewGroup
    ) : RecyclerView.ViewHolder(
        inflater.inflate(
            R.layout.fragment_recordmodifier_list_dialog_item,
            parent,
            false

        )
    ) {

        internal val delete: TextView = itemView.delete
        internal val rename:TextView=itemView.rename
        internal val share:TextView=itemView.share


        init {
            delete.setOnClickListener {
                mListener.let {
                    it.onRecordModifierClicked(1)
                    dismiss()
                }
            }

            rename.setOnClickListener {
                mListener.let {
                    it.onRecordModifierClicked(2)
                    dismiss()
                }
            }

            share.setOnClickListener {
                mListener.let {
                    it.onRecordModifierClicked(3)
                    dismiss()
                }
            }

        }
    }

    private inner class RecordModifierAdapter internal constructor(private val mItemCount: Int) :
        RecyclerView.Adapter<ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(parent.context), parent)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        }

        override fun getItemCount(): Int {
            return mItemCount
        }
    }

    companion object {

        fun newInstance(itemCount: Int,listener: Listener): RecordModifierListDialogFragment =
            RecordModifierListDialogFragment(listener).apply {
                arguments = Bundle().apply {
                    putInt(ARG_ITEM_COUNT, itemCount)
                }
            }

    }
}
