package thesis.filconnected.AddVideoAdmin

import UploadVideoInput
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.R

class UploadVideoAdapter(private val itemList: MutableList<UploadVideoInput>) :
    RecyclerView.Adapter<UploadVideoAdapter.InputViewHolder>() {

    class InputViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText: TextView = view.findViewById(R.id.tv_text)
        val ivIcon: ImageView = view.findViewById(R.id.iv_icon)
        val tvCategory: TextView = view.findViewById(R.id.tv_category)
        val deleteButton: ImageView = view.findViewById(R.id.delete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InputViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_upload_video_adapter, parent, false)
        return InputViewHolder(view)
    }

    override fun onBindViewHolder(holder: InputViewHolder, position: Int) {
        val item = itemList[position]
        holder.tvText.text = item.text
        holder.ivIcon.setImageResource(item.iconResId)
        holder.tvCategory.text = item.category

        // Handle Delete Button Click with Custom Dialog
        holder.deleteButton.setOnClickListener {
            // Inflate the custom dialog layout
            val dialogView = LayoutInflater.from(holder.itemView.context).inflate(R.layout.dialog_confirm_delete, null)

            // Create the dialog
            val dialog = Dialog(holder.itemView.context)
            dialog.setContentView(dialogView)

            // Get the buttons from the custom dialog layout
            val btnDelete = dialogView.findViewById<Button>(R.id.deleteBtn)
            val btnCancel = dialogView.findViewById<Button>(R.id.cancelBtn)

            // Set the delete action
            btnDelete.setOnClickListener {
                // Remove the item from the list and notify the adapter
                itemList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, itemList.size)
                dialog.dismiss()  // Dismiss the dialog
            }

            // Set the cancel action
            btnCancel.setOnClickListener {
                dialog.dismiss()  // Just dismiss the dialog if Cancel is clicked
            }

            // Show the custom dialog
            dialog.show()
        }
    }

    override fun getItemCount(): Int = itemList.size

    // Add item to the list
    fun addItem(newItem: UploadVideoInput) {
        itemList.add(newItem)
        notifyItemInserted(itemList.size - 1)
    }
}
