package thesis.filconnected.users.apptutorial

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import thesis.filconnected.R

class AppOneFslToText : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_ap_one)


        showHandDetectionDialog()

    }


    private fun showHandDetectionDialog() {
        val dialogView = layoutInflater.inflate(R.layout.ap_one_start, null)
        val alertDialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Set transparent background
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // Set dialog position at the bottom
        alertDialog.window?.setGravity(Gravity.BOTTOM)

        // Optionally, you can adjust the width and height
        val params = alertDialog.window?.attributes
        params?.width = WindowManager.LayoutParams.MATCH_PARENT
        params?.height = WindowManager.LayoutParams.WRAP_CONTENT
        alertDialog.window?.attributes = params

        // Convert 16dp to pixels
        val bottomMarginInPixels = (16 * resources.displayMetrics.density).toInt()

        // Modify the layoutParams and set the bottom margin
        val layoutParams = alertDialog.window?.attributes
        layoutParams?.let {
            val lp = it as WindowManager.LayoutParams
            lp.y = bottomMarginInPixels  // Set the margin to the bottom of the screen
            alertDialog.window?.attributes = lp
        }


        val startButton = dialogView.findViewById<Button>(R.id.btn_start) // Replace with actual ID
        startButton.setOnClickListener {
            alertDialog.dismiss()
            val intent =Intent(this, AppTwoFslToText::class.java)
            startActivity(intent)
        }

        alertDialog.show()
    }



}