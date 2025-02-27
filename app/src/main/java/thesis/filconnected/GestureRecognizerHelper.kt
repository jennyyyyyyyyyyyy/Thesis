
package thesis.filconnected

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import android.os.SystemClock
import android.util.Log
import androidx.annotation.VisibleForTesting
import androidx.camera.core.ImageProxy
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.framework.image.MPImage
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.core.Delegate
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizer
import com.google.mediapipe.tasks.vision.gesturerecognizer.GestureRecognizerResult
import android.content.Intent
import android.app.Activity
import android.speech.tts.TextToSpeech
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult
import java.util.Locale


class GestureRecognizerHelper(
    var minHandDetectionConfidence: Float = DEFAULT_HAND_DETECTION_CONFIDENCE,
    var minHandTrackingConfidence: Float = DEFAULT_HAND_TRACKING_CONFIDENCE,
    var minHandPresenceConfidence: Float = DEFAULT_HAND_PRESENCE_CONFIDENCE,
    var currentDelegate: Int = DELEGATE_CPU,
    var runningMode: RunningMode = RunningMode.IMAGE,
    val context: Context,
    val gestureRecognizerListener: GestureRecognizerListener? = null,
    ) {

    private var gestureRecognizer: GestureRecognizer? = null
    private var modelPath: String = MP_RECOGNIZER_TASK // Default model path
    private var poseLandmarker: PoseLandmarker? = null
    private var latestPoseLandmarkerResult: PoseLandmarkerResult? = null
    private lateinit var textToSpeech: TextToSpeech

    // Add state variables to track alignment and last spoken message
    private var isCurrentlyAligned: Boolean = false
    private var lastSpokenMessage: String? = null


    init {
        setupGestureRecognizer()
        setupPoseLandmarker()
        initializeTTS()
        // Load the saved model path from SharedPreferences
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val savedModelPath = sharedPreferences.getString("MODEL_PATH", null)
        if (!savedModelPath.isNullOrEmpty()) {
            modelPath = savedModelPath
        }
        setupGestureRecognizer()
    }

    fun initializeTTS() {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.language = Locale.US // Set language to English
            } else {
                Log.e(TAG, "TextToSpeech initialization failed")
            }
        }
    }

    private fun speak(text: String) {
        // Only speak if the message is different from the last spoken message
        if (lastSpokenMessage != text) {
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
            lastSpokenMessage = text // Update the last spoken message
        }
    }

    fun clearGestureRecognizer() {
        gestureRecognizer?.close()
        gestureRecognizer = null
        poseLandmarker?.close()
        poseLandmarker = null
    }



    fun setupGestureRecognizer() {
        if (modelPath.isNullOrEmpty()) {
            Log.e(TAG, "Model path is empty. Cannot initialize Gesture Recognizer.")
            gestureRecognizerListener?.onError("Model path is empty. Please select a valid model.")
            return
        }

        val baseOptionBuilder = BaseOptions.builder()

        when (currentDelegate) {
            DELEGATE_CPU -> {
                baseOptionBuilder.setDelegate(Delegate.CPU)
            }
            DELEGATE_GPU -> {
                baseOptionBuilder.setDelegate(Delegate.GPU)
            }
        }

        baseOptionBuilder.setModelAssetPath(modelPath)

        try {
            val baseOptions = baseOptionBuilder.build()
            val optionsBuilder = GestureRecognizer.GestureRecognizerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinHandDetectionConfidence(minHandDetectionConfidence)
                .setMinTrackingConfidence(minHandTrackingConfidence)
                .setMinHandPresenceConfidence(minHandPresenceConfidence)
                .setRunningMode(runningMode)
                .setNumHands(2)

            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener(this::returnLivestreamResult)
                    .setErrorListener(this::returnLivestreamError)
            }

            val options = optionsBuilder.build()
            gestureRecognizer = GestureRecognizer.createFromOptions(context, options)
            Log.d(TAG, "Gesture recognizer successfully initialized with model: $modelPath")
        } catch (e: IllegalStateException) {
            gestureRecognizerListener?.onError("Gesture recognizer failed to initialize. See error logs for details")
            Log.e(TAG, "MP Task Vision failed to load the task with error: " + e.message)
        } catch (e: RuntimeException) {
            gestureRecognizerListener?.onError("Gesture recognizer failed to initialize. See error logs for details", GPU_ERROR)
            Log.e(TAG, "MP Task Vision failed to load the task with error: " + e.message)
        }
    }

    private fun setupPoseLandmarker() {
        val baseOptionBuilder = BaseOptions.builder()
        when (currentDelegate) {
            DELEGATE_CPU -> baseOptionBuilder.setDelegate(Delegate.CPU)
            DELEGATE_GPU -> baseOptionBuilder.setDelegate(Delegate.GPU)
        }
        baseOptionBuilder.setModelAssetPath("pose_landmarker_full.task")
        try {
            val baseOptions = baseOptionBuilder.build()
            val optionsBuilder = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setMinPoseDetectionConfidence(0.7f) // Increase confidence threshold
                .setMinTrackingConfidence(0.7f)      // Increase confidence threshold
                .setMinPosePresenceConfidence(0.7f) // Increase confidence threshold
                .setRunningMode(runningMode)
            if (runningMode == RunningMode.LIVE_STREAM) {
                optionsBuilder
                    .setResultListener { result, _ ->
                        latestPoseLandmarkerResult = result
                        handlePoseResults(result, null)
                    }
                    .setErrorListener { error ->
                        gestureRecognizerListener?.onError(error.message ?: "Unknown error", OTHER_ERROR)
                    }
            }
            val options = optionsBuilder.build()
            poseLandmarker = PoseLandmarker.createFromOptions(context, options)
        } catch (e: Exception) {
            gestureRecognizerListener?.onError("Pose landmarker failed to initialize.", OTHER_ERROR)
            Log.e(TAG, "Failed to load pose landmarker model: ${e.message}")
        }
    }

    private fun handlePoseResults(result: PoseLandmarkerResult, input: MPImage?) {
        val landmarks = result.landmarks().firstOrNull()
        if (landmarks != null) {
            // Check alignment only if landmarks are present
            val isAligned = checkAlignment(landmarks)
            if (isAligned) {
                if (!isCurrentlyAligned) { // Only trigger if alignment changed
                    gestureRecognizerListener?.onPoseAligned()
//                    speak("Start the hand gesture") // Announce alignment
                    isCurrentlyAligned = true // Update state
                }
            } else {
                if (isCurrentlyAligned) { // Only trigger if alignment changed
                    gestureRecognizerListener?.onPoseMisaligned()
//                    speak("Please position the camera") // Announce misalignment
                    isCurrentlyAligned = false // Update state
                }
            }
        } else {
            // Reset alignment state and clear latest pose landmarks if no person is detected
            if (isCurrentlyAligned) { // Only trigger if alignment changed
                gestureRecognizerListener?.onPoseMisaligned()
                speak("Please position the camera") // Announce misalignment
                isCurrentlyAligned = false // Update state
            }
            latestPoseLandmarkerResult = null
        }
    }

    private fun checkAlignment(landmarks: List<NormalizedLandmark>): Boolean {
        // Define the guide area dimensions and center
        val guideWidth = 0.5f // Example width of the guide area
        val guideHeight = 0.8f // Example height of the guide area
        val guideCenterX = 0.5f // Center of the guide area
        val guideCenterY = 0.5f // Center of the guide area
        val tolerance = 0.1f // Allow a small margin of error

        // Check if the required landmarks are present
        val leftShoulder = landmarks.getOrNull(11) ?: return false
        val rightShoulder = landmarks.getOrNull(12) ?: return false
        val leftHip = landmarks.getOrNull(23) ?: return false
        val rightHip = landmarks.getOrNull(24) ?: return false

        // Ensure landmarks are visible and within valid range
        if (!isLandmarkVisible(leftShoulder) || !isLandmarkVisible(rightShoulder) ||
            !isLandmarkVisible(leftHip) || !isLandmarkVisible(rightHip)) {
            return false
        }

        // Calculate bounding box for the upper body (shoulders and hips)
        val minX = minOf(leftShoulder.x(), rightShoulder.x())
        val maxX = maxOf(leftShoulder.x(), rightShoulder.x())
        val minY = minOf(leftShoulder.y(), rightShoulder.y())
        val maxY = maxOf(leftShoulder.y(), rightShoulder.y())

        // Check if the bounding box fits within the guide area with tolerance
        return minX >= guideCenterX - guideWidth / 2 - tolerance &&
                maxX <= guideCenterX + guideWidth / 2 + tolerance &&
                minY >= guideCenterY - guideHeight / 2 - tolerance &&
                maxY <= guideCenterY + guideHeight / 2 + tolerance
    }

    private fun isLandmarkVisible(landmark: NormalizedLandmark): Boolean {
        // Check if the landmark is within the valid normalized range (0 to 1)
        return landmark.x() in 0.0..1.0 && landmark.y() in 0.0..1.0
    }


    // Add this function to handle the result of file picker
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                // Handle the URI returned from the file picker
                val filePath = getPathFromUri(uri)
                Log.d(TAG, "File selected: $filePath")

            }
        }
    }

    fun getPathFromUri(uri: Uri): String? {
        var path: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndex("_data")
            if (columnIndex != -1 && it.moveToFirst()) {
                path = it.getString(columnIndex)
            }
        }
        if (path == null && uri.scheme == "content") {
            // Handle content scheme URIs (for Android 10 and above)
            if (uri.path != null) {
                path = uri.path
            }
        }
        return path
    }

    // Convert the ImageProxy to MP Image and feed it to GestureRecognizer.
    fun recognizeLiveStream(imageProxy: ImageProxy) {
        val frameTime = SystemClock.uptimeMillis()
        val bitmapBuffer = Bitmap.createBitmap(imageProxy.width, imageProxy.height, Bitmap.Config.ARGB_8888)
        imageProxy.use { bitmapBuffer.copyPixelsFromBuffer(imageProxy.planes[0].buffer) }
        imageProxy.close()
        val matrix = Matrix().apply {
            postRotate(imageProxy.imageInfo.rotationDegrees.toFloat())
            postScale(1f, 1f, imageProxy.width.toFloat(), imageProxy.height.toFloat())
        }
        val rotatedBitmap = Bitmap.createBitmap(bitmapBuffer, 0, 0, bitmapBuffer.width, bitmapBuffer.height, matrix, true)
        val mpImage = BitmapImageBuilder(rotatedBitmap).build()

        // Perform pose detection first
        poseLandmarker?.detectAsync(mpImage, frameTime)


        // Only proceed with gesture recognition if aligned
        val isAligned = checkAlignmentFromPoseLandmarker()
        if (isAligned) {
            recognizeAsync(mpImage, frameTime)
        } else {
            // Clear the latest pose landmarks if not aligned
            latestPoseLandmarkerResult = null
        }
    }

    private fun checkAlignmentFromPoseLandmarker(): Boolean {
        // Use the stored latest result
        val result = latestPoseLandmarkerResult ?: return false
        val landmarks = result.landmarks().firstOrNull() ?: return false
        // Check alignment only if landmarks are present
        return checkAlignment(landmarks)
    }

    // Run hand gesture recognition using MediaPipe Gesture Recognition API
    @VisibleForTesting
    fun recognizeAsync(mpImage: MPImage, frameTime: Long) {
        // As we're using running mode LIVE_STREAM, the recognition result will
        // be returned in returnLivestreamResult function
        gestureRecognizer?.recognizeAsync(mpImage, frameTime)
    }

    // Return running status of the recognizer helper
    fun isClosed(): Boolean {
        return gestureRecognizer == null && poseLandmarker == null
    }

    // Return the recognition result to the GestureRecognizerHelper's caller
    private fun returnLivestreamResult(result: GestureRecognizerResult, input: MPImage) {
        val finishTimeMs = SystemClock.uptimeMillis()
        val inferenceTime = finishTimeMs - result.timestampMs()
        // Get the latest pose landmarks
        val poseLandmarks = latestPoseLandmarkerResult?.landmarks()?.firstOrNull()
        // Notify the listener with the updated ResultBundle
        gestureRecognizerListener?.onResults(
            ResultBundle(
                results = listOf(result),
                inferenceTime = inferenceTime,
                inputImageHeight = input.height,
                inputImageWidth = input.width,
                poseLandmarks = poseLandmarks // Include pose landmarks
            )
        )
    }
    // Return errors thrown during recognition to this GestureRecognizerHelper's
    // caller
    private fun returnLivestreamError(error: RuntimeException) {
        gestureRecognizerListener?.onError(error.message ?: "An unknown error has occurred")
    }

    companion object {
        val TAG = "GestureRecognizerHelper ${this.hashCode()}"
        var MP_RECOGNIZER_TASK = "gesture_recognizer.task"

        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DEFAULT_HAND_DETECTION_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_TRACKING_CONFIDENCE = 0.5F
        const val DEFAULT_HAND_PRESENCE_CONFIDENCE = 0.5F
        const val OTHER_ERROR = 0
        const val GPU_ERROR = 1
        public const val REQUEST_CODE_PICK_FILE = 1
    }

    data class ResultBundle(
        val results: List<GestureRecognizerResult>,
        val inferenceTime: Long,
        val inputImageHeight: Int,
        val inputImageWidth: Int,
        val poseLandmarks: List<NormalizedLandmark>? = null // New property for pose landmarks
    )

    interface GestureRecognizerListener {
        fun onError(error: String, errorCode: Int = OTHER_ERROR)
        fun onResults(resultBundle: ResultBundle)
        fun onPoseAligned()
        fun onPoseMisaligned()
    }
}