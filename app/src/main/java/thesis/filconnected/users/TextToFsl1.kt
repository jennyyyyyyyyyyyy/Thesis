package thesis.filconnected.users

import android.os.Bundle
import android.speech.SpeechRecognizer
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import kotlinx.coroutines.*
import thesis.filconnected.R

class TextToFsl1 : AppCompatActivity() {

    private lateinit var accessToken: String
    private val TAG = "TextToFsl"
//
//    private lateinit var speechRecognizer: SpeechRecognizer
//    private lateinit var resultText: TextView
//    private lateinit var startButton: ImageButton
//    private lateinit var deleteButton: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_fsl)

        // Retrieve UI elements
//        val searchInput = findViewById<EditText>(R.id.search_video_title)
        val searchButton = findViewById<ImageView>(R.id.search_button)
        val videoView = findViewById<VideoView>(R.id.video_view)
        val textDisplay = findViewById<TextView>(R.id.user_text_display)

//
//        resultText = findViewById(R.id.user_text_display)
//        startButton = findViewById(R.id.search_button)
//        deleteButton = findViewById(R.id.delete)

        // Retrieve the access token
        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        if (accessToken.isEmpty()) {
            Log.e(TAG, "Error: Missing Access Token")
            Toast.makeText(this, "Error: Missing Access Token", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "Access token successfully retrieved")

        // Set click listener for the search button
        searchButton.setOnClickListener {
            searchButton.setBackgroundResource(R.drawable.send_grey)
            val userInput = textDisplay.text.toString().trim()
            if (userInput.isNotEmpty()) {
//                 Display the input text in the TextView
                textDisplay.text = userInput

                val searchText = userInput  // Store the user input as the search term
                Log.d(TAG, "Search button clicked with input: $searchText")

                // Start searching in Dropbox
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val dbxClient = DbxClientV2(DbxRequestConfig("dropbox-sample"), accessToken)
                        val searchResultResponse =
                            dbxClient.files().searchBuilder("", userInput).start()

                        val fileMetadata = searchResultResponse.matches
                            .mapNotNull { it.metadata as? com.dropbox.core.v2.files.FileMetadata }
                            .firstOrNull()

                        withContext(Dispatchers.Main) {
                            if (fileMetadata != null) {
                                val videoPath = fileMetadata.pathLower
                                Log.d(TAG, "Video file found: $videoPath")

                                // Retrieve temporary link and play the video
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val temporaryLink =
                                            dbxClient.files().getTemporaryLink(videoPath).link
                                        Log.d(TAG, "Temporary link retrieved: $temporaryLink")

                                        withContext(Dispatchers.Main) {
                                            videoView.setVideoPath(temporaryLink)
                                            videoView.setOnPreparedListener {
                                                Log.d(
                                                    TAG,
                                                    "Video is prepared and starting playback"
                                                )
                                                videoView.start()
                                            }
                                            videoView.setOnErrorListener { _, what, extra ->
                                                Log.e(
                                                    TAG,
                                                    "Error playing video: what=$what, extra=$extra"
                                                )
                                                Toast.makeText(
                                                    this@TextToFsl1,
                                                    "Error playing video",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                true
                                            }
                                            videoView.setOnCompletionListener {
                                                Log.d(TAG, "Video playback completed")
                                                Toast.makeText(
                                                    this@TextToFsl1,
                                                    "Video playback completed",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        Log.e(TAG, "Error retrieving temporary link", e)
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                this@TextToFsl1,
                                                "Failed to retrieve video link",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    this@TextToFsl1,
                                    "No video file found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.e(TAG, "Error occurred during search operation", e)
                            Toast.makeText(
                                this@TextToFsl1,
                                "Error Searching video: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }}


