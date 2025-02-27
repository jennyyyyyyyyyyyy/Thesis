package thesis.filconnected

import android.app.Activity
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import thesis.filconnected.GestureRecognizerHelper.Companion.MP_RECOGNIZER_TASK
import thesis.filconnected.GestureRecognizerHelper.Companion.TAG


class Override : AppCompatActivity() {

    private lateinit var gestureRecognizerHelper: GestureRecognizerHelper

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        if (requestCode == gestureRecognizerHelper.REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
//            data?.data?.let { uri ->
//                val filePath = gestureRecognizerHelper.getPathFromUri(uri)
//                if (filePath != null) {
//                    Log.d(TAG, "Selected model path: $filePath")
//
//                    // Update the model path dynamically
//                    MP_RECOGNIZER_TASK = filePath
//
//                    // Reinitialize the gesture recognizer with the new model
//                    gestureRecognizerHelper.clearGestureRecognizer()
//                    gestureRecognizerHelper.setupGestureRecognizer()
//                } else {
//                    Log.e(TAG, "Failed to resolve file path.")
//                }
//            }
//        }
//    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        gestureRecognizerHelper.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GestureRecognizerHelper.REQUEST_CODE_PICK_FILE && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val filePath = gestureRecognizerHelper.getPathFromUri(uri)
                if (filePath != null) {
                    Log.d(TAG, "Selected model path: $filePath")

                    // Update the model path dynamically
                    MP_RECOGNIZER_TASK = filePath

                    // Reinitialize the gesture recognizer with the new model
                    gestureRecognizerHelper.clearGestureRecognizer()
                    gestureRecognizerHelper.setupGestureRecognizer()
                } else {
                    Log.e(TAG, "Failed to resolve file path.")
                }
            }
        }
    }

}
