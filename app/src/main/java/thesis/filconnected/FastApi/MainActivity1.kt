package thesis.filconnected.FastApi

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import thesis.filconnected.R
import java.io.File
import java.io.FileOutputStream

class MainActivity1 : AppCompatActivity() {

    private lateinit var uploadButton: Button
    private lateinit var listVideosButton: Button
    private lateinit var logsTextView: TextView
    private lateinit var selectFileButton: Button
    private var selectedFileUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main1)

        // Initialize UI components
        uploadButton = findViewById(R.id.uploadButton)
        listVideosButton = findViewById(R.id.listVideosButton)
        logsTextView = findViewById(R.id.logsTextView)
        selectFileButton = findViewById(R.id.selectFileButton)





        // File Picker
        val filePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                Log.d("FilePicker", "Selected file URI: $uri")
                selectedFileUri = uri  // Save selected file

                // Get file name and display it
                val fileName = getFileNameFromUri(uri)
                logsTextView.text = "Selected File: $fileName"  // Show file name in TextView

                Toast.makeText(this@MainActivity1, "File selected: $fileName", Toast.LENGTH_SHORT).show()
            } else {
                Log.e("FilePicker", "No file selected")
                Toast.makeText(this@MainActivity1, "No file selected", Toast.LENGTH_SHORT).show()
            }
        }

        selectFileButton.setOnClickListener {
            Log.d("FilePicker", "Launching file picker...")
            filePickerLauncher.launch("video/*")
        }

        uploadButton.setOnClickListener {
            if (selectedFileUri == null) {
                Toast.makeText(this, "Please select a file first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadVideo(selectedFileUri!!)
        }

        listVideosButton.setOnClickListener {
            listVideos()
        }
    }




    // Upload Video Function
    private fun uploadVideo(fileUri: Uri) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tempFile = createTempFileFromUri(fileUri)
                val requestBody = RequestBody.create("video/*".toMediaTypeOrNull(), tempFile)
                val part = MultipartBody.Part.createFormData("file", tempFile.name, requestBody)

                withContext(Dispatchers.Main) {
                    RetrofitInstance.api.uploadVideo(part).enqueue(object : Callback<Map<String, String>> {
                        override fun onResponse(call: Call<Map<String, String>>, response: Response<Map<String, String>>) {
                            if (response.isSuccessful) {
                                val responseBody = response.body()
                                logsTextView.text = "Upload Success: $responseBody"
                                Log.d("Upload", "Success: $responseBody")
                            } else {
                                logsTextView.text = "Upload Error: ${response.errorBody()?.string()}"
                                Log.e("Upload", "Error: ${response.errorBody()?.string()}")
                            }
                        }

                        override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                            logsTextView.text = "Upload Failure: ${t.message}"
                            Log.e("Upload", "Failure: ${t.message}")
                        }
                    })
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    logsTextView.text = "File processing error: ${e.message}"
                    Log.e("Upload", "File processing error", e)
                }
            }
        }
    }

    // List Videos Function
    private fun listVideos() {
        RetrofitInstance.api.listVideos().enqueue(object : Callback<Map<String, List<String>>> {
            override fun onResponse(call: Call<Map<String, List<String>>>, response: Response<Map<String, List<String>>>) {
                if (response.isSuccessful) {
                    val videoUrls = response.body()?.get("videos")?.joinToString("\n") ?: "No videos found"
                    logsTextView.text = "Videos:\n$videoUrls"
                    Log.d("ListVideos", "Success: $videoUrls")
                } else {
                    logsTextView.text = "List Videos Error: ${response.errorBody()?.string()}"
                    Log.e("ListVideos", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<Map<String, List<String>>>, t: Throwable) {
                logsTextView.text = "List Videos Failure: ${t.message}"
                Log.e("ListVideos", "Failure: ${t.message}")
            }
        })
    }









    //for extracting the original file name of video
    private fun getFileNameFromUri(uri: Uri): String {
        var fileName = "Unknown"
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    fileName = it.getString(nameIndex)
                }
            }
        }
        return fileName
    }

    // Retrofit Interface
    interface ApiService {
        @Multipart
        @POST("upload/")
        fun uploadVideo(@Part file: MultipartBody.Part): Call<Map<String, String>>

        @GET("list-videos/")
        fun listVideos(): Call<Map<String, List<String>>>
    }

    // Retrofit Instance
    object RetrofitInstance {
        private const val BASE_URL = "http://157.230.49.49:3000/"  // Replace with your API URL

        val api: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }

    // Helper Function to Create a Temporary File from Uri
    private fun createTempFileFromUri(uri: Uri): File {
        val contentResolver = contentResolver
        val fileExtension = contentResolver.getType(uri)?.substringAfterLast("/") ?: "mp4"
        val tempFile = File.createTempFile("upload_", ".$fileExtension", cacheDir)

        contentResolver.openInputStream(uri)?.use { inputStream ->
            FileOutputStream(tempFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }

        return tempFile
    }


}
