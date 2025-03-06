package thesis.filconnected.admin.model_version

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.FastApi.Video
import thesis.filconnected.R

class StartTraining : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnAddLabel: LinearLayout
    private lateinit var btnStartTraining: Button
    private lateinit var adapter: StartTrainingAdapter
    private val textList = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_training)

        recyclerView = findViewById(R.id.recyclerView)
        btnAddLabel = findViewById(R.id.btnAddLabel)
        btnStartTraining = findViewById(R.id.btnStartTraining)


        // Initialize RecyclerView once
        adapter = StartTrainingAdapter(textList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Show AlertDialog on button click
        btnAddLabel.setOnClickListener{
            val dialogView = layoutInflater.inflate(R.layout.dialog_training_static_gesture_label, null)
            val alertDialog = AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create()

            alertDialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT) // Full width
            }

            // IMPORTANT: Use dialogView to find the views in AlertDialog
            val inputLabel = dialogView.findViewById<EditText>(R.id.inputLabel)
            val nextButton = dialogView.findViewById<Button>(R.id.nextBtn)

            // Add input text to RecyclerView when button is clicked
            nextButton.setOnClickListener {
                val text = inputLabel.text.toString().trim()
                if (text.isNotEmpty()) {
                    textList.add(text)
                    adapter.notifyItemInserted(textList.size - 1) // Notify adapter
                    inputLabel.text.clear() // Clear EditText
                    recyclerView.scrollToPosition(textList.size - 1) // Scroll to last item
                    alertDialog.dismiss() // Close dialog
                } else {
                    inputLabel.error = "Please enter a frame name."
                }
            }

            alertDialog.show()
        }


        btnStartTraining.setOnClickListener{
            val intent = Intent(this, Video::class.java )
            startActivity(intent)
        }


    }
}
