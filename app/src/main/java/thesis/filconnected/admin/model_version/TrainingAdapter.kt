package thesis.filconnected.admin.model_version

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.R

class StartTrainingAdapter(private val textList: ArrayList<String>) : RecyclerView.Adapter<StartTrainingAdapter.TextViewHolder>() {


    class TextViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_text)
        val deleteButton: ImageView = itemView.findViewById(R.id.delete)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.activity_training_adapter, parent, false)
        return TextViewHolder(view)
    }

    override fun onBindViewHolder(holder: TextViewHolder, position: Int) {
        holder.textView.text = textList[position]

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
                textList.removeAt(position)
                notifyItemRemoved(position)
                notifyItemRangeChanged(position, textList.size)
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

    override fun getItemCount(): Int {
        return textList.size
    }
}
