package thesis.filconnected.AddVideoAdmin

import UploadVideoInput
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import thesis.filconnected.R

class UploadVideo : AppCompatActivity() {
    private lateinit var etInput: EditText
    private lateinit var btnSubmit: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var categorySpinner: Spinner
    private lateinit var adapter: UploadVideoAdapter
    private val inputList = mutableListOf<UploadVideoInput>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_video)

        etInput = findViewById(R.id.et_input)
        btnSubmit = findViewById(R.id.btn_submit)
        recyclerView = findViewById(R.id.recyclerView)
        categorySpinner = findViewById(R.id.video_category_input)

        adapter = UploadVideoAdapter(inputList)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Set up Spinner
        setupSpinner()

        btnSubmit.setOnClickListener {
            val userInput = etInput.text.toString().trim()
            val selectedCategory = categorySpinner.selectedItem.toString()

            // Check if input is empty or category is unselected
            if (userInput.isEmpty()) {
                Toast.makeText(this, "Please enter a filename.", Toast.LENGTH_SHORT).show()
            } else if (selectedCategory == "Select a category") {
                Toast.makeText(this, "Please select a category.", Toast.LENGTH_SHORT).show()
            } else {
                val iconResId = getIconForInput(userInput)
                val newItem = UploadVideoInput(userInput, iconResId, selectedCategory)

                adapter.addItem(newItem)
                etInput.text.clear()
                categorySpinner.setSelection(0) // Reset the spinner to "Select a category"
            }
        }

    }

    private fun getIconForInput(input: String): Int {
        return when {
            input.endsWith(".jpg", true) || input.endsWith(".png", true) -> R.drawable.image
            input.endsWith(".mp4", true) || input.endsWith(".avi", true) -> R.drawable.video
            else -> R.drawable.image // Use a default icon
        }
    }

    private fun setupSpinner() {
        val categories = arrayOf("Select a category","Greetings", "Numbers", "Letters")
        val adapter = ArrayAdapter(this, R.layout.text_view_for_dropdown, categories)
        adapter.setDropDownViewResource(R.layout.dropdown_themes)
        categorySpinner.adapter = adapter
    }


}
