package thesis.filconnected.users

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dropbox.core.DbxRequestConfig
import com.dropbox.core.v2.DbxClientV2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import thesis.filconnected.GestureRecognizerHelper.Companion.TAG
import thesis.filconnected.R
import java.util.Locale

class TextToFsl : AppCompatActivity() {

    private lateinit var accessToken: String
    private val TAG = "TextToFsl"

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var resultText: TextView
    private lateinit var startButton: ImageButton
    private lateinit var deleteButton: ImageView
    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_fsl)

        resultText = findViewById(R.id.user_text_display)
        startButton = findViewById(R.id.search_button)
        deleteButton = findViewById(R.id.delete)
        videoView = findViewById(R.id.video_view)


        // Request microphone permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }

        // Initialize SpeechRecognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        // Setup recognition listener
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@TextToFsl, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                val errorMessage = getErrorText(error)
                Log.e("SpeechRecognizer", "Error: $errorMessage")
//                runOnUiThread {
//                    Toast.makeText(this@TextToFsl, " $errorMessage", Toast.LENGTH_SHORT).show()
//                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    resultText.text = matches[0]  // Display recognized text
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })


        // Start listening when button is clicked
        startButton.setOnClickListener {
//            startButton.setBackgroundResource(R.drawable.send_grey)
            startListening()

//            videoDisplay()

        }

        deleteButton.setOnClickListener {
            resultText.text = ""
        }


        // Retrieve the access token
        accessToken = intent.getStringExtra("ACCESS_TOKEN") ?: ""
        if (accessToken.isEmpty()) {
            Log.e(TAG, "Error: Missing Access Token")
            Toast.makeText(this, "Error: Missing Access Token", Toast.LENGTH_SHORT).show()
            return
        }
        Log.d(TAG, "Access token successfully retrieved")

        // Listen for text input changes
        resultText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val searchText = s.toString().trim()
                if (searchText.isNotEmpty()) {
                    searchAndPlayVideo(searchText, videoView)
                } else {
                    videoView.stopPlayback()
                }
            }
        })

    }


        private fun searchAndPlayVideo(searchText: String, videoView: VideoView) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val dbxClient = DbxClientV2(DbxRequestConfig("dropbox-sample"), accessToken)
                    val searchResultResponse =
                        dbxClient.files().searchBuilder("", searchText).start()

                    val fileMetadata = searchResultResponse.matches
                        .mapNotNull { it.metadata as? com.dropbox.core.v2.files.FileMetadata }
                        .firstOrNull()

                    withContext(Dispatchers.Main) {
                        if (fileMetadata != null) {
                            val videoPath = fileMetadata.pathLower
                            Log.d(TAG, "Video file found: $videoPath")

                            // Retrieve the temporary link and play the video
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val temporaryLink =
                                        dbxClient.files().getTemporaryLink(videoPath).link
                                    Log.d(TAG, "Temporary link retrieved: $temporaryLink")

                                    withContext(Dispatchers.Main) {
                                        videoView.setVideoPath(temporaryLink)

                                        // Automatically play the video
                                        videoView.setOnPreparedListener {
                                            Log.d(TAG, "Video is prepared and starting playback")
                                            videoView.start()
                                        }

                                        videoView.setOnErrorListener { _, what, extra ->
                                            Log.e(
                                                TAG,
                                                "Error playing video: what=$what, extra=$extra"
                                            )
                                            Toast.makeText(
                                                this@TextToFsl,
                                                "Error playing video",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            true
                                        }

                                        videoView.setOnCompletionListener {
                                            Log.d(TAG, "Video playback completed")
                                            Toast.makeText(
                                                this@TextToFsl,
                                                "Video playback completed",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                } catch (e: Exception) {
                                    Log.e(TAG, "Error retrieving temporary link", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(
                                            this@TextToFsl,
                                            "Failed to retrieve video link",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        } else {
                            Toast.makeText(this@TextToFsl, "No video found", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Log.e(TAG, "Error occurred during search operation", e)
                        Toast.makeText(
                            this@TextToFsl,
                            "Error Searching video: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }



        private fun startListening() {
            if (!SpeechRecognizer.isRecognitionAvailable(this)) {
                Toast.makeText(this, "Speech Recognition not available", Toast.LENGTH_SHORT).show()
                return
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())

            speechRecognizer.stopListening()  // Ensure no previous recognition is running
            speechRecognizer.startListening(intent)
        }

        override fun onDestroy() {
            super.onDestroy()
            speechRecognizer.destroy()
        }

        override fun onStop() {
            super.onStop()
            speechRecognizer.stopListening()

        }
    }
    // Function to translate error codes into readable messages
    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
//            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No recognition result matched"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy. Try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }

//
//
//    private fun showToast(message: String) {
//        runOnUiThread {
//            Toast.makeText(this@TextToFsl, message, Toast.LENGTH_SHORT).show()
//        }


