package thesis.filconnected.FastApi
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import thesis.filconnected.R

class MainActivity3 : AppCompatActivity() {

    private lateinit var selectedFileUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnUpload).setOnClickListener {
            openFilePicker()
        }

        findViewById<Button>(R.id.btnListVideos).setOnClickListener {
            listVideos()
        }

        findViewById<Button>(R.id.btnRename).setOnClickListener {
            val oldName = findViewById<EditText>(R.id.etOldName).text.toString()
            val newName = findViewById<EditText>(R.id.etNewName).text.toString()
            renameVideo(oldName, newName)
        }

        findViewById<Button>(R.id.btnDelete).setOnClickListener {
            val filename = findViewById<EditText>(R.id.etDeleteFilename).text.toString()
            deleteVideo(filename)
        }

        findViewById<Button>(R.id.btnShowVideo).setOnClickListener {
            val filename = findViewById<EditText>(R.id.etShowFilename).text.toString()
            if (filename.isNotEmpty()) {
                showVideo(filename)
            } else {
                Toast.makeText(this, "Please enter a filename", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "video/*"
        }
        startActivityForResult(intent, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == RESULT_OK) {
            data?.data?.let { uri ->
                selectedFileUri = uri
                uploadVideo(uri)
            }
        }
    }

    private fun uploadVideo(fileUri: Uri) {
        val file = File(getRealPathFromURI(fileUri))

        // Get the custom filename from the EditText
        val customFilename = findViewById<EditText>(R.id.etCustomFilename).text.toString().trim()

        // Validate the custom filename
        if (customFilename.isEmpty()) {
            findViewById<TextView>(R.id.tvResult).text = "Please enter a custom filename"
            return
        }

        // Append the .mp4 extension to the custom filename
        val fileNameWithExtension = "$customFilename.mp4"
        val fileNameWithoutExtension = customFilename

        // First, check if the file already exists on the server
        RetrofitInstance.api.listVideos().enqueue(object : Callback<ListVideosResponse> {
            override fun onResponse(call: Call<ListVideosResponse>, response: Response<ListVideosResponse>) {
                if (response.isSuccessful) {
                    val videoUrls = response.body()?.videos ?: emptyList()

                    // Extract only the filenames from the URLs and remove the file extensions
                    val videoFilenamesWithoutExtension = videoUrls.map { url ->
                        url.substringAfterLast("/") // Extract the part after the last "/"
                            .substringBeforeLast(".") // Remove the file extension
                    }

                    // Check if the filename already exists (without extension)
                    if (videoFilenamesWithoutExtension.contains(fileNameWithoutExtension)) {
                        findViewById<TextView>(R.id.tvResult).text = "File already exists"
                    } else {
                        // Proceed with the upload if the file does not exist
                        val requestFile = file.asRequestBody("video/*".toMediaTypeOrNull())
                        val body = MultipartBody.Part.createFormData("file", fileNameWithExtension, requestFile)

                        RetrofitInstance.api.uploadVideo(body).enqueue(object : Callback<UploadResponse> {
                            override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                                if (response.isSuccessful) {
                                    val result = response.body()?.message ?: "Upload successful"

                                    // Display a Toast message with the filename (without extension)
                                    Toast.makeText(
                                        this@MainActivity3,
                                        "Upload successful: $fileNameWithoutExtension",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Update the TextView with the result message
                                    findViewById<TextView>(R.id.tvResult).text = "$result: $fileNameWithoutExtension"

                                    // Clear the EditText after successful upload
                                    findViewById<EditText>(R.id.etCustomFilename).setText("")
                                } else {
                                    findViewById<TextView>(R.id.tvResult).text = "Upload failed"
                                }
                            }

                            override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                                findViewById<TextView>(R.id.tvResult).text = "Network error: ${t.message}"
                            }
                        })
                    }
                } else {
                    findViewById<TextView>(R.id.tvResult).text = "Failed to check existing videos"
                }
            }

            override fun onFailure(call: Call<ListVideosResponse>, t: Throwable) {
                findViewById<TextView>(R.id.tvResult).text = "Network error: ${t.message}"
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
                        url.substringAfterLast("/")// Extract the part after the last "/"
                            .substringBeforeLast(".")//remove the mp4 extension
                    }.joinToString("\n") // Join filenames with a newline for display

                    findViewById<TextView>(R.id.tvResult).text = if (videoFilenames.isNotEmpty()) {
                        videoFilenames
                    } else {
                        "No videos found"
                    }
                } else {
                    findViewById<TextView>(R.id.tvResult).text = "Failed to list videos"
                }
            }

            override fun onFailure(call: Call<ListVideosResponse>, t: Throwable) {
                findViewById<TextView>(R.id.tvResult).text = "Network error: ${t.message}"
            }
        })
    }

    private fun renameVideo(oldName: String, newName: String) {
        // First, check if the new filename already exists on the server
        RetrofitInstance.api.listVideos().enqueue(object : Callback<ListVideosResponse> {
            override fun onResponse(call: Call<ListVideosResponse>, response: Response<ListVideosResponse>) {
                if (response.isSuccessful) {
                    val videoUrls = response.body()?.videos ?: emptyList()

                    // Extract only the filenames from the URLs
                    val videoFilenames = videoUrls.map { url ->
                        url.substringAfterLast("/") // Extract the part after the last "/"
                    }

                    // Check if the new filename already exists
                    if (videoFilenames.contains(newName)) {
                        Toast.makeText(
                            this@MainActivity3,
                            "File name already exists",
                            Toast.LENGTH_SHORT
                        ).show()
                        findViewById<TextView>(R.id.tvResult).text = "File name already exists"
                    } else {
                        // Proceed with renaming if the new filename does not exist
                        val renameRequest = RenameRequest(new_name = newName)
                        RetrofitInstance.api.renameVideo(oldName, renameRequest).enqueue(object : Callback<RenameResponse> {
                            override fun onResponse(call: Call<RenameResponse>, response: Response<RenameResponse>) {
                                if (response.isSuccessful) {
                                    // Clear the EditText fields after a successful rename
                                    findViewById<EditText>(R.id.etOldName).setText("")
                                    findViewById<EditText>(R.id.etNewName).setText("")

                                    // Update the TextView with the result message
                                    Toast.makeText(
                                        this@MainActivity3,
                                        response.body()?.message ?: "No message received",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findViewById<TextView>(R.id.tvResult).text = response.body()?.message
                                } else {
                                    Toast.makeText(
                                        this@MainActivity3,
                                        "File name already exist",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    findViewById<TextView>(R.id.tvResult).text = "Rename failed"
                                }
                            }

                            override fun onFailure(call: Call<RenameResponse>, t: Throwable) {
                                findViewById<TextView>(R.id.tvResult).text = "Network error: ${t.message}"
                            }
                        })
                    }
                } else {
                    findViewById<TextView>(R.id.tvResult).text = "Failed to check existing videos"
                }
            }

            override fun onFailure(call: Call<ListVideosResponse>, t: Throwable) {
                findViewById<TextView>(R.id.tvResult).text = "Network error: ${t.message}"
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
                        url.substringAfterLast("/") // Extract the part after the last "/"
                    }

                    // Perform a case-insensitive match to find the exact filename
                    val matchedFilename = videoFilenames.find { it.equals(filename, ignoreCase = true) }

                    if (matchedFilename != null) {
                        // Proceed with deletion using the exact matched filename
                        RetrofitInstance.api.deleteVideo(matchedFilename).enqueue(object : Callback<DeleteResponse> {
                            override fun onResponse(call: Call<DeleteResponse>, response: Response<DeleteResponse>) {
                                if (response.isSuccessful) {
                                    // Show a success Toast
                                    Toast.makeText(
                                        this@MainActivity3,
                                        "File deleted successfully: $matchedFilename",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    // Update the TextView with the result message
                                    findViewById<TextView>(R.id.tvResult).text = response.body()?.message
                                } else {
                                    // Show a failure Toast
                                    Toast.makeText(
                                        this@MainActivity3,
                                        "Failed to delete file: $matchedFilename",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                    findViewById<TextView>(R.id.tvResult).text = "Delete failed"
                                }
                            }

                            override fun onFailure(call: Call<DeleteResponse>, t: Throwable) {
                                // Show a network error Toast
                                Toast.makeText(
                                    this@MainActivity3,
                                    "Network error: ${t.message}",
                                    Toast.LENGTH_SHORT
                                ).show()

                                findViewById<TextView>(R.id.tvResult).text = "Network error: ${t.message}"
                            }
                        })
                    } else {
                        // Show a "file not found" Toast
                        Toast.makeText(
                            this@MainActivity3,
                            "File not found: $filename",
                            Toast.LENGTH_SHORT
                        ).show()

                        findViewById<TextView>(R.id.tvResult).text = "File not found"
                    }
                } else {
                    // Show a failure Toast for listing videos
                    Toast.makeText(
                        this@MainActivity3,
                        "Failed to check existing videos",
                        Toast.LENGTH_SHORT
                    ).show()

                    findViewById<TextView>(R.id.tvResult).text = "Failed to check existing videos"
                }
            }

            override fun onFailure(call: Call<ListVideosResponse>, t: Throwable) {
                // Show a network error Toast
                Toast.makeText(
                    this@MainActivity3,
                    "Network error: ${t.message}",
                    Toast.LENGTH_SHORT
                ).show()

                findViewById<TextView>(R.id.tvResult).text = "Network error: ${t.message}"
            }
        })
    }

    private fun showVideo(filename: String) {
        RetrofitInstance.api.getVideo(filename).enqueue(object : Callback<GetVideoResponse> {
            override fun onResponse(call: Call<GetVideoResponse>, response: Response<GetVideoResponse>) {
                if (response.isSuccessful) {
                    val videoUrl = response.body()?.url
                    videoUrl?.let {
                        playVideo(it)
                    } ?: run {

                        Toast.makeText(this@MainActivity3, "Failed to retrieve video URL", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Log.e("API_ERROR", "Error: ${response.message()}")
                    Toast.makeText(this@MainActivity3, "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<GetVideoResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity3, "Network error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun playVideo(videoUrl: String) {
        val videoView = findViewById<VideoView>(R.id.videoView)
        videoView.visibility = android.view.View.VISIBLE

        val videoUri = Uri.parse("http://157.230.49.49:3000/$videoUrl") // Replace with your server's IP
        videoView.setVideoURI(videoUri)

        videoView.start()

        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
        }

        videoView.setOnErrorListener { _, _, _ ->
            Toast.makeText(this, "Error playing video", Toast.LENGTH_SHORT).show()
            false
        }
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
//    private fun getRealPathFromURI(context: Context, uri: Uri): String {
//        var fileName = "unknown_file"
//        val cursor = context.contentResolver.query(uri, null, null, null, null)
//        cursor?.use {
//            if (it.moveToFirst()) {
//                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
//                if (nameIndex != -1) {
//                    fileName = it.getString(nameIndex)
//                }
//            }
//        }
//        return fileName
//    }

}