package thesis.filconnected.FastApi

import VideoItem
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.R

class VideoAdapter(
    private val videoList: MutableList<VideoItem>,
    private val onDeleteClickListener: (String) -> Unit // Callback for delete button
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    inner class VideoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvVideoName: TextView = itemView.findViewById(R.id.tvVideoName)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_video, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val video = videoList[position]
        holder.tvVideoName.text = video.name

        // Set up delete button click listener
        holder.btnDelete.setOnClickListener {
            onDeleteClickListener(video.name) // Pass the video name to the callback
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }



}