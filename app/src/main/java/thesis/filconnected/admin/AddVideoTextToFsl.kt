package thesis.filconnected.admin

import kotlinx.coroutines.*
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.dropbox.core.DbxException
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import com.dropbox.core.v2.files.WriteMode
import thesis.filconnected.R
import java.io.IOException

class AddVideoTextToFsl : AppCompatActivity() {

    private lateinit var accessToken: String
    private lateinit var videoTitle: String
    private lateinit var videoCategory: String

    // Declare the ActivityResultLauncher
    private lateinit var videoPickerLauncher: ActivityResultLauncher<Intent>

    // Declare a TextView to display the selected file path
    private lateinit var selectedFilePathTextView: TextView
//    val scrollContainer: LinearLayout = findViewById(R.id.scrollContainer)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_video_text_to_fsl)

        //delete icon
//        val delete: ImageView = findViewById(R.id.delete)

//        delete.setOnClickListener { deleteDialog() }

        val videoTitleEditText: EditText = findViewById(R.id.video_name_input)
        // for dropdown
        val categorySpinner: Spinner = findViewById(R.id.video_category_input)

        // Sample data for the dropdown
        val categories = arrayOf("Greetings", "Numbers", "Letters", "Direction")

        // Adapter to populate Spinner
        val adapter = ArrayAdapter(this, R.layout.text_view_for_dropdown, categories)
        adapter.setDropDownViewResource(R.layout.dropdown_themes)

        // Set adapter to Spinner
        categorySpinner.adapter = adapter

        // Get the access token from the intent
        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: "Token not available."

        // Validate the access token
        if (accessToken == "Token not available.") {
            Toast.makeText(this, "Invalid Access Token", Toast.LENGTH_SHORT).show()
            finish() // Exit the activity if the token is invalid
            return
        }

//        // SuccessActivity.kt
//        findViewById<Button>(R.id.search_video_button).setOnClickListener {
//            val intent = Intent(this, TextToFsl::class.java)
//            intent.putExtra("ACCESS_TOKEN", accessToken) // Pass the access token
//            startActivity(intent)
//        }


//        // Display the access token
//        val tokenTextView: TextView = findViewById(R.id.access_token_text)
//        tokenTextView.text = accessToken

        // Set up input fields for video title and category



        // Initialize the TextView for displaying the selected file path
        selectedFilePathTextView = findViewById(R.id.selected_file_path)

        // Register the ActivityResultLauncher
        videoPickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    // Get the file name from the URI
                    val fileName = getFileName(it)
                    // Display the selected file name
                    selectedFilePathTextView.text = fileName ?: it.toString()
                    uploadVideoToDropbox(it)
                }
            }
        }

        // Set up button listener for adding video
        findViewById<LinearLayout>(R.id.add_video_button).setOnClickListener {
            videoTitle = videoTitleEditText.text.toString()
            videoCategory = categorySpinner.selectedItem.toString()

            if (videoTitle.isNotEmpty() && videoCategory.isNotEmpty()) {
                pickVideo()
            } else {
                Toast.makeText(this, "Please enter video title and category", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var fileName: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                fileName = it.getString(nameIndex)
            }
        }
        return fileName
    }

    private fun pickVideo() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        // Use the ActivityResultLauncher to start the video picker
        videoPickerLauncher.launch(intent)
    }

    private fun uploadVideoToDropbox(videoUri: Uri) {


        // Run the upload on a background thread (using a coroutine)
        CoroutineScope(Dispatchers.IO).launch {
            val config = DbxRequestConfig.newBuilder("dropbox/java-tutorial").build()
            val client = DbxClientV2(config, accessToken)

            try {
                contentResolver.openFileDescriptor(videoUri, "r")?.use { fileDescriptor ->
                    val fileSize = fileDescriptor.statSize
                    if (fileSize > 150 * 1024 * 1024) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddVideoTextToFsl, "File too large to upload. Max size is 150MB.", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    val inputStream = contentResolver.openInputStream(videoUri)
                    // Check if videoTitle and videoCategory are empty
                    if (videoCategory.isEmpty() || videoTitle.isEmpty()) {
                        Log.e("DropboxUpload", "Video title or category is empty.")
                        withContext(Dispatchers.Main) {

                            Toast.makeText(this@AddVideoTextToFsl, "Please provide both title and category.", Toast.LENGTH_SHORT).show()
                        }
                        return@launch
                    }

                    // Construct the file name for Dropbox upload
                    val fileName = "/$videoCategory/${videoTitle.replace(" ", "_")}_${System.currentTimeMillis()}.mp4"
                    Log.d("DropboxUpload", "File path: $fileName")

//                    val fileName = "$videoCategory ${videoTitle.replace(" ", "_")}_${System.currentTimeMillis()}.mp4"
//                    Log.d("DropboxUpload", "File path: $fileName")

                    inputStream?.use {
                        // Upload to Dropbox
                        client.files().uploadBuilder(fileName)
                            .withMode(WriteMode.OVERWRITE)
                            .uploadAndFinish(it)

                        withContext(Dispatchers.Main) {
                            selectedFilePathTextView.text=fileName
//                            scrollContainer.addView(scrollContainer)
                            Toast.makeText(this@AddVideoTextToFsl, "Video uploaded successfully!", Toast.LENGTH_SHORT).show()
                        }
                    } ?: run {
                        Log.e("DropboxUpload", "Failed to open video input stream.")
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@AddVideoTextToFsl, "Failed to open video input stream.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AddVideoTextToFsl, "Failed to open video file.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: IOException) {
                Log.e("DropboxUpload", "I/O Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddVideoTextToFsl, "I/O Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: DbxException) {
                Log.e("DropboxUpload", "Dropbox Exception: ${e.message}")
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddVideoTextToFsl, "Dropbox API Error: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("DropboxUpload", "Unexpected Exception: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AddVideoTextToFsl, "Upload failed: ${e.localizedMessage ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun deleteDialog() {
        val dialog = Dialog(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_delete, null)
        dialog.setContentView(view)

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Make background transparent

        val deleteButton = view.findViewById<Button>(R.id.deleteBtn)
        val cancelButton = view.findViewById<Button>(R.id.cancelBtn)

        deleteButton.setOnClickListener {
            // TODO: Add your delete logic here
            Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        cancelButton.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()

}}
