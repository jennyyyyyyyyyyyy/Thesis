package thesis.filconnected.FastApi

import VideoItem
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.opengl.Visibility
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import thesis.filconnected.R
import thesis.filconnected.users.TextToFsl

class Video : AppCompatActivity() {

    private lateinit var selectedFileUri: Uri
    private lateinit var recyclerView: RecyclerView
    private lateinit var videoAdapter: VideoAdapter
    private val videoItems = mutableListOf<VideoItem>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        // Initialize RecyclerView
        recyclerView = findViewById(R.id.rvVideos)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = LinearLayoutManager(this)


        videoAdapter = VideoAdapter(videoItems) { filename ->
            deleteVideo(filename)
        }
        recyclerView.adapter = videoAdapter
        recyclerView.adapter = videoAdapter


        findViewById<ImageButton>(R.id.btnAdd).setOnClickListener {
            val customFilename = findViewById<EditText>(R.id.etCustomFilename).text.toString().trim().lowercase()

            if (customFilename.isBlank()) {
                alertDialog()

            } else {
                openFilePicker()
            }
        }

        listVideos()

    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
        }
        startActivityForResult(intent, 100)
    }


    //after selecting a video file
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                val fileName = getFileName(uri)

                Toast.makeText(this, "Selected File: $fileName", Toast.LENGTH_SHORT).show()
                val uploadButton = findViewById<Button>(R.id.btnUpload)
                uploadButton.isEnabled = true
                uploadButton.setVisibility(View.VISIBLE);

                uploadButton.setOnClickListener {
                    uploadVideo(uri)
                    uploadButton.setVisibility(View.GONE);
                }
            }
        }
    }


    private fun uploadVideo(fileUri: Uri) {
        val file = File(getRealPathFromURI(fileUri))
        val customFilename = findViewById<EditText>(R.id.etCustomFilename).text.toString().trim().lowercase() // Convert to lowercase
        val uploadButton = findViewById<Button>(R.id.btnUpload)

        val originalExtension = file.extension
        val fileNameWithExtension = "$customFilename.$originalExtension" // Filename is now case insensitive

        val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", fileNameWithExtension, requestFile)

        RetrofitInstance.api.uploadVideo(body).enqueue(object : Callback<UploadResponse> {
            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(this@Video, "Upload Successful!", Toast.LENGTH_LONG).show()

                    // Reset selected file
                    selectedFileUri = Uri.EMPTY

                    // Clear input fields
                    findViewById<EditText>(R.id.etCustomFilename).setText("")

                    // Disable upload button until a new file is chosen
                    uploadButton.isEnabled = false

                    // Refresh video list
                    listVideos()
                } else {
                    val err = findViewById<EditText>(R.id.etCustomFilename)
                    err.error = "File already exists."
                }
            }

            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                showAlertDialog("Network Error", "An error occurred: ${t.message}")
            }
        })
    }




    private fun deleteVideo(filename: String) {
        // First, fetch the list of existing videos to perform a case-insensitive match
        RetrofitInstance.api.listVideos().enqueue(object : Callback<ListVideosResponse> {
            override fun onResponse(call: Call<ListVideosResponse>, response: Response<ListVideosResponse>) {
                if (response.isSuccessful) {
                    val videoUrls = response.body()?.videos ?: emptyList()

                    // Extract only the filenames from the URLs
                    val videoFilenames = videoUrls.map { url ->
                        url.substringAfterLast("/")
                    }

                    // Perform a case-insensitive match to find the exact filename
                    val matchedFilename = videoFilenames.find { it.substringBeforeLast(".").equals(filename, ignoreCase = true) }


                    if (matchedFilename != null) {
                        // Proceed with deletion using the exact matched filename
                        RetrofitInstance.api.deleteVideo(matchedFilename).enqueue(object : Callback<DeleteResponse> {
                            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                                if (response.isSuccessful) {
                                    // Show a success Toast
                                    Toast.makeText(
                                        this@Video,
                                        "File deleted successfully: $matchedFilename",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Refresh the list of videos
                                    listVideos()
                                } else {
                                    // Show a failure Toast
                                    Toast.makeText(
                                        this@Video,
                                        "Failed to delete file: $matchedFilename",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                                // Show a network error Toast
                                Toast.makeText(
                                    this@Video,
                                    "Network error: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        })
                    } else {
                        // Show a "file not found" Toast
                        Toast.makeText(
                            this@Video,
                            "File not found: $filename",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Show a failure Toast for listing videos
                    Toast.makeText(
                        this@Video,
                        "Failed to check existing videos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ListVideosResponse>, t: Throwable) {
                // Show a network error Toast
                Toast.makeText(
                    this@Video,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun listVideos() {
        RetrofitInstance.api.listVideos().enqueue(object : Callback<ListVideosResponse> {
            override fun onResponse(call: Call<ListVideosResponse>, response: Response<ListVideosResponse>) {
                if (response.isSuccessful) {
                    val videoUrls = response.body()?.videos ?: emptyList()

                    // Extract only the filenames from the URLs
                    val videoFilenames = videoUrls.map { url ->
                        url.substringAfterLast("/")
                            .substringBeforeLast(".")
                    }

                    // Clear the existing list and add new items
                    videoItems.clear()
                    videoItems.addAll(videoFilenames.map { VideoItem(it) })
                    videoAdapter.notifyDataSetChanged()

                    // Show a message if no videos are found
                    if (videoFilenames.isEmpty()) {
                        showAlertDialog("No Videos", "No videos found on the server.")
                    }
                } else {
                    showAlertDialog("Error", "Failed to list videos")
                }
            }

            override fun onFailure(call: Call<ListVideosResponse>, t: Throwable) {
                showAlertDialog("Network Error", "An error occurred: ${t.message}")
            }
        })
    }



    private fun getRealPathFromURI(uri: Uri): String {
        val filePathColumn = arrayOf(android.provider.MediaStore.Video.Media.DATA)
        val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
        cursor?.moveToFirst()
        val columnIndex = cursor?.getColumnIndex(filePathColumn[0])
        val filePath = columnIndex?.let { cursor.getString(it) }
        cursor?.close()
        return filePath ?: ""
    }

    private fun showAlertDialog(title: String, message: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss() // Close the dialog when "OK" is clicked
            }
            .create()
            .show()
    }

    private fun getFileName(uri: Uri): String {
        var fileName = "Unknown File" // Default name if extraction fails
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME) // Get the column index of file name
            if (nameIndex != -1 && cursor.moveToFirst()) {
                fileName = cursor.getString(nameIndex) // Extract the file name
            }
        }
        return fileName
    }



    private fun alertDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_filename_empty, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        // Set transparent background to make it look custom
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Center the dialog
        alertDialog.window?.setGravity(Gravity.CENTER)

        // Find the OK button and set a click listener to dismiss the dialog
        val okButton = dialogView.findViewById<Button>(R.id.okBtn)
        okButton.setOnClickListener {
            alertDialog.dismiss()
        }

        alertDialog.show()
    }
}