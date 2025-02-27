package thesis.filconnected.users

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import thesis.filconnected.R
import thesis.filconnected.fragment.CameraFragment
import thesis.filconnected.GestureRecognizerHelper
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import java.io.File
import android.speech.tts.TextToSpeech
import java.util.Locale

class FSLtoText : AppCompatActivity() {

    // Declare TextToSpeech instance
    private lateinit var textToSpeech: TextToSpeech


    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fsl_to_text)

        val startButton: Button = findViewById(R.id.btn_start)
        val replyButton: Button = findViewById(R.id.btn_reply)
        val restartButton: Button = findViewById(R.id.btn_restart)
        val fragmentContainer = findViewById<View>(R.id.fragment_container)
        val image = findViewById<ImageView>(R.id.camera_image)
        val buttonUpdateModel = findViewById<Button>(R.id.button_update_model)


        gestureRecognizerHelper = GestureRecognizerHelper(context = this)

        // File picker button to select a new model
        buttonUpdateModel.setOnClickListener {
            openFilePicker()  // Call file picker method
        }

        replyButton.visibility = View.GONE
        restartButton.visibility = View.GONE

        // Initialize TextToSpeech
        textToSpeech = TextToSpeech(this) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US // Set language to English
            } else {
                println("TextToSpeech initialization failed")
            }
        }

        restartButton.setOnClickListener {
            val fragmentManager = supportFragmentManager
            val fragmentTransaction = fragmentManager.beginTransaction()
            fragmentTransaction.replace(
                R.id.fragment_container,
                CameraFragment::class.java.newInstance()
            )
            fragmentTransaction.commit()
        }

        replyButton.setOnClickListener {
            val intent = Intent(this, TextToFsl::class.java)
            startActivity(intent)
        }

        startButton.setOnClickListener {
            fragmentContainer.visibility = View.VISIBLE
            replyButton.visibility = View.VISIBLE
            restartButton.visibility = View.VISIBLE
            startButton.visibility = View.GONE
            image.visibility = View.GONE
            // Speak the message "Please position the camera"
            speak("Please position the camera")
        }
    }

    // Function to handle speaking messages
    private fun speak(text: String) {
        textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    // Shutdown TextToSpeech when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized) {
            textToSpeech.shutdown()
        }
    }



    private fun openFilePicker() {
        // Intent to open file picker for model selection
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/octet-stream"  // You may use specific MIME types depending on your file
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, GestureRecognizerHelper.REQUEST_CODE_PICK_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GestureRecognizerHelper.REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val newModelPath = saveModelToInternalStorage(uri)
                if (newModelPath != null) {
                    GestureRecognizerHelper.MP_RECOGNIZER_TASK = newModelPath
                    gestureRecognizerHelper.clearGestureRecognizer()
                    gestureRecognizerHelper.setupGestureRecognizer()
                    Toast.makeText(this, "Model updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to load model", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun saveModelToInternalStorage(uri: Uri): String? {
        return try {
            val inputStream = contentResolver.openInputStream(uri)
            val modelFile = File(filesDir, "gesture_recognizer.task")
            inputStream.use { input ->
                modelFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }
            // Save the model path to SharedPreferences
            val sharedPreferences = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
            sharedPreferences.edit().putString("MODEL_PATH", modelFile.absolutePath).apply()


            modelFile.absolutePath
        } catch (e: Exception) {
            Log.e("FSLtoText", "Failed to save model: ${e.message}")
            null
        }
    }

}