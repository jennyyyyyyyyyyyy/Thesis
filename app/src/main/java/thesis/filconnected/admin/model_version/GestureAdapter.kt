

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.R

class GestureAdapter(
    private var folders: MutableList<String>,
    private val onDeleteClick: (String) -> Unit
) : RecyclerView.Adapter<GestureAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val folderName: TextView = view.findViewById(R.id.folderName)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_training_folder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val folderName = folders[position]
        holder.folderName.text = folderName

        holder.btnDelete.setOnClickListener {
            // Inflate the custom dialog layout
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
                onDeleteClick(folderName)  // Call delete function
                dialog.dismiss()           // Dismiss the dialog
            }

            btnCancel.setOnClickListener {
                dialog.dismiss()  // Close the dialog without deleting
            }

            dialog.show()  // Show the dialog
        }
    }


    override fun getItemCount() = folders.size

    fun updateFolders(newFolders: MutableList<String>) {
        folders = newFolders
        notifyDataSetChanged()
    }
}
