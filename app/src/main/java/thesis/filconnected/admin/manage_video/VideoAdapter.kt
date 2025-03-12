package thesis.filconnected.FastApi

import VideoItem
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

        holder.btnDelete.setOnClickListener {
            val dialogView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.dialog_confirm_delete, null)

            // Create the dialog
            val dialog = Dialog(holder.itemView.context)
            dialog.setContentView(dialogView)
            dialog.setCancelable(false) // Prevent dismiss on outside touch

            // Get the buttons from the custom dialog layout
            val btnDelete = dialogView.findViewById<Button>(R.id.deleteBtn)
            val btnCancel = dialogView.findViewById<Button>(R.id.cancelBtn)

            btnDelete.setOnClickListener {
                onDeleteClickListener(video.name)
                dialog.dismiss()
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    override fun getItemCount(): Int {
        return videoList.size
    }

}