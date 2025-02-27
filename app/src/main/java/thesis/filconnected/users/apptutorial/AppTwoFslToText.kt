package thesis.filconnected.users.apptutorial

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import thesis.filconnected.R

class AppTwoFslToText : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ap_two)

//        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
//        val tutorialShown = sharedPreferences.getBoolean("TutorialShown", false)
//
//        if (!tutorialShown) {
//            showHandDetectionDialog()
//
//            // Save that the tutorial has been shown
//            with(sharedPreferences.edit()) {
//                putBoolean("TutorialShown", true)
//                apply()
//            }
//        }
        textOutputDialog()
    }


    private fun textOutputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.ap_two_text_output, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Allow dismissing when clicking outside
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position
        val params = alertDialog.window?.attributes
        params?.gravity = Gravity.BOTTOM
        params?.y = 265 // Offset from top
        alertDialog.window?.attributes = params

        val output = dialogView.findViewById<LinearLayout>(R.id.output) // Replace with actual ID
        output.setOnClickListener {
           restartDialog()
            alertDialog.dismiss()// Close the dialog when audio icon is clicked
        }

        alertDialog.show()
    }
//    private fun audioDialog() {
//        val dialogView = layoutInflater.inflate(R.layout.ap_two_audio, null)
//        val alertDialog = AlertDialog.Builder(this)
//            .setView(dialogView)
//            .setCancelable(true) // Allow dismissing when clicking outside
//            .create()
//
//        // Set transparent background
//        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//
//        // Set dialog position
//        val params = alertDialog.window?.attributes
//        params?.gravity = Gravity.BOTTOM
//        params?.x = 10 // Offset from left
//        params?.y = 385 // Offset from top
//        alertDialog.window?.attributes = params
//
//        // **Find the Audio Icon and set click listener**
//        val audioIcon = dialogView.findViewById<ImageView>(R.id.audioIcon) // Replace with actual ID
//        audioIcon.setOnClickListener {
//            restartDialog()
//            alertDialog.dismiss() // Close the dialog when audio icon is clicked
////            showHandDetectionDialog1()
//        }
//
//        alertDialog.show()
//    }


    private fun restartDialog() {
        val dialogView = layoutInflater.inflate(R.layout.ap_two_restart, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Allow dismissing when clicking outside
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position
        val params = alertDialog.window?.attributes
        params?.gravity = Gravity.BOTTOM
//        params?.x = 10 // Offset from left
        params?.y = 50 // Offset from top
        alertDialog.window?.attributes = params

        // **Find the Audio Icon and set click listener**
        val audioIcon = dialogView.findViewById<Button>(R.id.btn_restart) // Replace with actual ID
        audioIcon.setOnClickListener {
            textToFslDialog()
            alertDialog.dismiss() // Close the dialog when audio icon is clicked
//
        }

        alertDialog.show()
    }


    private fun textToFslDialog() {
        val dialogView = layoutInflater.inflate(R.layout.ap_two_text_to_fsl, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Allow dismissing when clicking outside
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position
        val params = alertDialog.window?.attributes
        params?.gravity = Gravity.BOTTOM
//        params?.x = 10 // Offset from left
        params?.y = 50 // Offset from top
        alertDialog.window?.attributes = params

        // **Find the Audio Icon and set click listener**
        val audioIcon = dialogView.findViewById<Button>(R.id.btn_restart) // Replace with actual ID
        audioIcon.setOnClickListener {
            alertDialog.dismiss() // Close the dialog when audio icon is clicked
            val intent = Intent(this, AppThreeTextToFsl::class.java)
            startActivity(intent)
        }

        alertDialog.show()
    }







}
