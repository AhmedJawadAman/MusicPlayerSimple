package com.example.musicplayer

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class Mp3Adapter(
    private var audioList: List<AudioFile>,
    private val onItemClick: (AudioFile) -> Unit
) : RecyclerView.Adapter<Mp3Adapter.Mp3ViewHolder>() {

    fun updateData(newAudioList: List<AudioFile>) {
        audioList = newAudioList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Mp3ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mp3, parent, false)
        return Mp3ViewHolder(view)
    }

    override fun onBindViewHolder(holder: Mp3ViewHolder, position: Int) {
        val audioFile = audioList[position]
        holder.fileName.text = audioFile.name
        holder.filePath.text = audioFile.path

        // Set the click listener for the item
        holder.itemView.setOnClickListener {
            onItemClick(audioFile)
        }
    }

    override fun getItemCount(): Int {
        return audioList.size
    }

    class Mp3ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val fileName: TextView = itemView.findViewById(R.id.tvFileName)
        val filePath: TextView = itemView.findViewById(R.id.tvFilePath)
    }
}
