package thesis.filconnected.admin.model_version

import android.app.Dialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import java.io.File
import android.os.Environment
import android.widget.TextView
import android.os.CountDownTimer
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.FileOutputStream
import android.util.Log
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.camera.view.PreviewView
import java.util.concurrent.Executors
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import thesis.filconnected.R
import java.io.ByteArrayOutputStream

class TrainingCamera : AppCompatActivity() {
    private var label: String? = null
    private var labelPath: String? = null
    private val imgPerLabel = 125 // Capture 125 images per hand
    private var imageCount = 0
    private lateinit var previewView: PreviewView
    private lateinit var countdownOverlay: TextView
    private lateinit var counterOverlay: TextView
    private lateinit var handOverlay: TextView // New overlay for LEFT/RIGHT HAND
    private var imageCaptureExecutor = Executors.newSingleThreadExecutor()
    private var cameraProvider: ProcessCameraProvider? = null
    private var isDataCollectionComplete = false
    private var isLeftHand = true // Track whether capturing for left or right hand


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(thesis.filconnected.R.layout.activity_training_camera)

        // Initialize UI elements
        previewView = findViewById(thesis.filconnected.R.id.view_finder)


        countdownOverlay = findViewById(thesis.filconnected.R.id.countdownOverlay)
        counterOverlay = findViewById(thesis.filconnected.R.id.counterOverlay)


        // Hand overlay (new)
        handOverlay = TextView(this).apply {
            textSize = 10f
            gravity = Gravity.TOP or Gravity.START
            setBackgroundColor(Color.argb(0, 0, 0, 0)) // Semi-transparent black background
            setTextColor(Color.WHITE)
            setPadding(100, 16, 0, 16) // Add padding for better visibility
            visibility = View.VISIBLE
        }
        addContentView(handOverlay, previewView.layoutParams)

        askForLabel()

        val btnDone = findViewById<Button>(thesis.filconnected.R.id.btnDone)
        btnDone.setOnClickListener{
            val intent = Intent(this, Training::class.java)
            startActivity(intent)
        }




    }


    private fun askForLabel() {
        // Create BottomSheetDialog
        val dialog = BottomSheetDialog(this)

        // Inflate the custom layout
        val dialogView = LayoutInflater.from(this)
            .inflate(thesis.filconnected.R.layout.dialog_training_gesture_type, null)

        // Set the custom layout to the BottomSheetDialog
        dialog.setContentView(dialogView)
        dialog.setCancelable(false)

        // Find buttons in the custom layout
        val staticGesture = dialogView.findViewById<TextView>(thesis.filconnected.R.id.staticGesture)
        val dynamicGesture = dialogView.findViewById<TextView>(thesis.filconnected.R.id.dynamicGesture)

        // Set click listeners
        staticGesture.setOnClickListener {
            dialog.dismiss() // Close the dialog
            startStaticGestureCollection() // Call the static gesture function
        }

        dynamicGesture.setOnClickListener {
            dialog.dismiss() // Close the dialog
            startDynamicGestureCollection() // Call the dynamic gesture function
        }

        // Show the BottomSheetDialog
        dialog.show()
    }


    private fun startStaticGestureCollection() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_training_static_gesture_label, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
//            .setCancelable(false)
            .create()

        val editText = dialogView.findViewById<EditText>(thesis.filconnected.R.id.inputLabel)
        val btnNext = dialogView.findViewById<Button>(thesis.filconnected.R.id.nextBtn)

        // Set click listeners
        btnNext.setOnClickListener {
            val userLabel = editText.text.toString().trim()
            if (userLabel.isNotEmpty()) {
                label = sanitizeLabel(userLabel)
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                labelPath = "$downloadsDir/gesture_dataset/$label"
                val labelDir = File(labelPath!!)

                if (!labelDir.exists() && !labelDir.mkdirs()) {
                    showAlert("Failed to create directory for gesture data.")
                    return@setOnClickListener
                }

                imageCount = 0
                isLeftHand = true
                Toast.makeText(this, "Data collection started for: $label", Toast.LENGTH_SHORT).show()
                startCameraWithCountdown()

                alertDialog.dismiss()
            } else {
                editText.error = "Label cannot be empty!."
            }
        }

        // Show the AlertDialog
        alertDialog.show()

        // Make the background transparent AFTER showing the dialog
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

//    private fun startStaticGestureCollection() {
//        val dialogView = layoutInflater.inflate(R.layout.dialog_training_static_gesture_label, null)
//        val alertDialog = AlertDialog.Builder(this).setView(dialogView).create()
//
//        val editText = dialogView.findViewById<EditText>(R.id.inputLabel)
//        val btnNext = dialogView.findViewById<Button>(R.id.nextBtn)
//
//        btnNext.setOnClickListener {
//            val userLabel = editText.text.toString().trim()
//            if (userLabel.isNotEmpty()) {
//                addGestureToList(userLabel) // Add to RecyclerView
//                Toast.makeText(this, "Added: $userLabel", Toast.LENGTH_SHORT).show()
//                alertDialog.dismiss()
//            } else {
//                editText.error = "Label cannot be empty!"
//            }
//        }
//
//        alertDialog.show()
//    }
//
//

    private fun startDynamicGestureCollection() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_training_dynamic_gesture_label, null)

        val alertDialog = AlertDialog.Builder(this) // Use custom style
            .setView(dialogView)
            .setCancelable(true)
            .create()

        val editText = dialogView.findViewById<EditText>(R.id.inputLabel)
        val btnOk = dialogView.findViewById<Button>(R.id.nextBtn)


        btnOk.setOnClickListener {
            val userLabel = editText.text.toString().trim()
            if (userLabel.isNotEmpty()) {
                collectKeyPose(userLabel, 1) // Start with the first keypose
                alertDialog.dismiss()
            } else {
                editText.error = "Label cannot be empty!"
            }
        }


        alertDialog.show()

        // Apply transparency after showing the dialog
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    private fun collectKeyPose(userLabel: String, keyposeCounter: Int) {
        val keyposeLabel = "${sanitizeLabel(userLabel)}$keyposeCounter"
        labelPath =
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/gesture_dataset/$keyposeLabel"
        val labelDir = File(labelPath!!)
        if (!labelDir.exists()) {
            labelDir.mkdirs() // Create the label folder if it doesn't exist
        }

        imageCount = 0 // Reset image count for this keypose
        isLeftHand = true // Start with left hand
        label = keyposeLabel // Set the current label to the keypose label

        Toast.makeText(
            this,
            "Data collection started for dynamic gesture: $keyposeLabel",
            Toast.LENGTH_SHORT
        ).show()

        // Start the camera and countdown process
        startCameraWithCountdown()

        // Use a Handler to wait until data collection is complete
        val handler = android.os.Handler()
        val checkCompletionTask = object : Runnable {
            override fun run() {
                if (isDataCollectionComplete) {
                    // Stop the handler from running again
                    handler.removeCallbacks(this)

                    // Inflate the custom dialog layout
                    val dialogView = LayoutInflater.from(this@TrainingCamera)
                        .inflate(R.layout.dialog_training_collect_key_pose, null)

                    // Create the custom dialog
                    val dialog = Dialog(this@TrainingCamera)
                    dialog.setContentView(dialogView)
                    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent) // Transparent background

                    // Get dialog elements
                    val tvMessage = dialogView.findViewById<TextView>(R.id.tvMessage)
                    val btnYes = dialogView.findViewById<Button>(R.id.yesBtn)
                    val btnCancel = dialogView.findViewById<Button>(R.id.cancelBtn)

                    // Set custom message
                    tvMessage.text = "Do you want to capture the next key pose for '$userLabel'?"

                    // Yes Button Click
                    btnYes.setOnClickListener {
                        isDataCollectionComplete = false
                        collectKeyPose(userLabel, keyposeCounter + 1) // Collect next keypose
                        dialog.dismiss()
                    }

                    // No Button Click
                    btnCancel.setOnClickListener {
                        Toast.makeText(
                            this@TrainingCamera,
                            "Dynamic gesture collection stopped.",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialog.dismiss()
                    }

                    // Show the dialog
                    dialog.show()
                } else {
                    // Check again after 500ms
                    handler.postDelayed(this, 500)
                }
            }
        }

        handler.post(checkCompletionTask)
    }

    private fun startCameraWithCountdown() {
        countdownOverlay.visibility = View.VISIBLE
        counterOverlay.visibility = View.VISIBLE
        handOverlay.visibility = View.VISIBLE
        handOverlay.text = if (isLeftHand) "LEFT HAND" else "RIGHT HAND" // Update hand overlay
        counterOverlay.text = "0/$imgPerLabel" // Reset the counter text

        object : CountDownTimer(4000, 1000) { // 4 seconds total (3, 2, 1, capturing)
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                if (secondsLeft > 0) {
                    countdownOverlay.text = "$secondsLeft"
                }
            }

            override fun onFinish() {
                countdownOverlay.text = "Capturing..."
                startCamera()
            }
        }.start()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            cameraProvider = cameraProviderFuture.get()

            // Preview use case
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            // Image analysis use case
            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(imageCaptureExecutor) { imageProxy ->
                        captureImage(imageProxy)
                    }
                }

            // Select front camera
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
            } catch (e: Exception) {
                Log.e("CameraError", "Failed to bind camera use cases", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun captureImage(imageProxy: ImageProxy) {
        if (imageCount < imgPerLabel) {
            imageCaptureExecutor.execute {
                try {
                    // Update the counter overlay on the main thread
                    runOnUiThread {
                        counterOverlay.text = "${imageCount + 1}/$imgPerLabel"
                    }

                    // Create a sequential filename with leading zeros (e.g., "dislike_01_left.jpg")
                    val handSuffix = if (isLeftHand) "_left" else "_right"
                    val formattedNumber = String.format("%02d", imageCount + 1) // Add leading zero
                    val imageFile = File(labelPath, "${label}_$formattedNumber$handSuffix.jpg")

                    // Convert ImageProxy to Bitmap
                    val bitmap = imageProxy.toBitmap()

                    // Rotate the Bitmap to portrait orientation
                    val rotatedBitmap = rotateBitmap(bitmap, imageProxy.imageInfo.rotationDegrees)

                    // Flip the Bitmap horizontally (mirror effect for front camera)
                    val flippedBitmap = flipBitmapHorizontally(rotatedBitmap)

                    // Compress the flipped and rotated Bitmap into JPEG format with a quality of 85% (adjustable)
                    FileOutputStream(imageFile).use { outputStream ->
                        flippedBitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
                    }

                    imageCount++
                    Log.d("ImageCapture", "Image $imageCount saved: ${imageFile.absolutePath}")
                } catch (e: Exception) {
                    Log.e("ImageCaptureError", "Error saving image", e)
                    runOnUiThread {
                        showAlert("Failed to save image. Please try again.")
                    }
                } finally {
                    imageProxy.close()
                }
            }
        } else {
            // Switch to the next hand or complete data collection
            if (isLeftHand) {
                isLeftHand = false // Switch to right hand
                imageCount = 0 // Reset image count
                runOnUiThread {
                    Toast.makeText(this, "Switching to RIGHT HAND...", Toast.LENGTH_SHORT).show()
                    startCameraWithCountdown() // Restart countdown for right hand
                }
            } else {
                // Data collection complete for both hands
                runOnUiThread {
                    isDataCollectionComplete = true
                    counterOverlay.visibility = View.VISIBLE
                    countdownOverlay.visibility = View.GONE
                    handOverlay.visibility = View.GONE
                    Toast.makeText(this, "Data collection complete for $label!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun ImageProxy.toBitmap(): Bitmap {
        val yBuffer = planes[0].buffer // Y plane
        val uBuffer = planes[1].buffer // U plane
        val vBuffer = planes[2].buffer // V plane

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        // U and V are swapped in ImageProxy compared to NV21 format
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
        val imageBytes = out.toByteArray()
        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun rotateBitmap(bitmap: Bitmap, rotationDegrees: Int): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.postRotate(rotationDegrees.toFloat())

        // Return the rotated bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun flipBitmapHorizontally(bitmap: Bitmap): Bitmap {
        val matrix = android.graphics.Matrix()
        matrix.preScale(-1f, 1f) // Flip horizontally

        // Return the flipped bitmap
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    private fun sanitizeLabel(label: String): String {
        return label.replace(Regex("[^a-zA-Z0-9 ]"), "_")
    }

    private fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .create()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up resources
        cameraProvider?.unbindAll()
        imageCaptureExecutor.shutdown()
    }
}
