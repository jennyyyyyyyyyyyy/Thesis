package thesis.filconnected.users

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.RecognitionListener
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import thesis.filconnected.FastApi.GetVideoResponse
import thesis.filconnected.FastApi.RetrofitInstance
import thesis.filconnected.R
import java.util.Locale

class TextToFsl : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var resultText: TextView
    private lateinit var startButton: ImageButton
    private lateinit var deleteButton: ImageView
    private lateinit var playerView: PlayerView
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_text_to_fsl)

        initViews()
        checkPermissions()
        setupSpeechRecognizer()
        setupListeners()
    }

    private fun initViews() {
        resultText = findViewById(R.id.user_text_display)
        startButton = findViewById(R.id.search_button)
        deleteButton = findViewById(R.id.delete)
        playerView = findViewById(R.id.playerView)
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 1)
        }
    }

    private fun setupSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Toast.makeText(this@TextToFsl, "Listening...", Toast.LENGTH_SHORT).show()
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Error: ${getErrorText(error)}")
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    resultText.text = matches[0]
                    startButton.setBackgroundResource(R.drawable.send_black)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun setupListeners() {
        startButton.setOnClickListener {
            startButton.setBackgroundResource(R.drawable.send_grey)
            startListening()
        }

        deleteButton.setOnClickListener {
            resultText.text = ""
            playerView.visibility = View.VISIBLE
        }

        resultText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val filename = s.toString().trim()
                if (filename.isNotEmpty()) showVideo(filename) else stopVideo()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun startListening() {
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Toast.makeText(this, "Speech Recognition not available", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        speechRecognizer.stopListening()
        speechRecognizer.startListening(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        exoPlayer?.release()
    }

    override fun onStop() {
        super.onStop()
        speechRecognizer.stopListening()
        exoPlayer?.pause()
    }

    // API: Fetch video
    private fun showVideo(filename: String) {
        RetrofitInstance.api.getVideo(filename).enqueue(object : Callback<GetVideoResponse> {
            override fun onResponse(call: Call<GetVideoResponse>, response: Response<GetVideoResponse>) {
                val videoUrl = response.body()?.url ?: "http://157.230.49.49:3000/videos/$filename.mp4"
                playVideo(videoUrl)
            }

            override fun onFailure(call: Call<GetVideoResponse>, t: Throwable) {
                Log.e("NETWORK_ERROR", "Network error. Using default video URL.", t)
                playVideo("http://157.230.49.49:3000/videos/$filename.mp4")
            }
        })
    }

    private fun playVideo(videoUrl: String) {

        // Release previous player
        exoPlayer?.release()

        // Initialize new ExoPlayer
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
            prepare()
            playWhenReady = true
        }

        playerView.player = exoPlayer
    }

    private fun stopVideo() {
        exoPlayer?.release()
        exoPlayer = null

    }

    private fun getErrorText(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
            SpeechRecognizer.ERROR_CLIENT -> "Client-side error"
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
            SpeechRecognizer.ERROR_NETWORK -> "Network error"
            SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
            SpeechRecognizer.ERROR_NO_MATCH -> "No recognition result matched"
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "RecognitionService busy. Try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech input"
            else -> "Unknown error"
        }
    }
}
