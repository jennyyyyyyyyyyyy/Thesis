package thesis.filconnected.users.apptutorial

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import thesis.filconnected.R
import thesis.filconnected.users.HomePage

class AppThreeTextToFsl : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_three)

        audioDialog(
        )
    }


    private fun audioDialog() {
        val dialogView = layoutInflater.inflate(R.layout.ap_three_audio, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Allow dismissing when clicking outside
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position
        val params = alertDialog.window?.attributes
        params?.gravity = Gravity.BOTTOM
        params?.y = 16 // Offset from top
        alertDialog.window?.attributes = params

        val audioButton = dialogView.findViewById<ImageView>(R.id.audioButton) // Replace with actual ID
        audioButton.setOnClickListener {
            textOutput()
            alertDialog.dismiss()// Close the dialog when audio icon is clicked
        }

        alertDialog.show()
    }

    private fun textOutput() {
        val dialogView = layoutInflater.inflate(R.layout.ap_three_text_output, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Allow dismissing when clicking outside
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position
        val params = alertDialog.window?.attributes
        params?.gravity = Gravity.TOP
        params?.y = 425 // Offset from top
        alertDialog.window?.attributes = params

        val output = dialogView.findViewById<LinearLayout>(R.id.output) // Replace with actual ID
        output.setOnClickListener {
          delete()
            alertDialog.dismiss()// Close the dialog when audio icon is clicked
        }


        alertDialog.show()
    }

    private fun delete() {
        val dialogView = layoutInflater.inflate(R.layout.ap_three_delete, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Allow dismissing when clicking outside
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position
        val params = alertDialog.window?.attributes
        params?.gravity = Gravity.TOP
        params?.y = 487 // Offset from top
        alertDialog.window?.attributes = params

        val output = dialogView.findViewById<ImageView>(R.id.delete) // Replace with actual ID
        output.setOnClickListener {
            fslResult()
            alertDialog.dismiss()// Close the dialog when audio icon is clicked
        }


        alertDialog.show()
    }

    private fun fslResult() {
        val dialogView = layoutInflater.inflate(R.layout.ap_three_fsl_output, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false) // Allow dismissing when clicking outside
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position
        val params = alertDialog.window?.attributes
        params?.gravity = Gravity.BOTTOM
        params?.y = 0 // Offset from top
        alertDialog.window?.attributes = params

        val output = dialogView.findViewById<ImageView>(R.id.camera_image) // Replace with actual ID
        output.setOnClickListener {

        val intent=Intent(this, HomePage::class.java)
            startActivity(intent)


            alertDialog.dismiss()// Close the dialog when audio icon is clicked
        }


        alertDialog.show()
    }




}