package thesis.filconnected.admin

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import thesis.filconnected.FastApi.Video
import thesis.filconnected.R
import thesis.filconnected.admin.model_version.Training


class WelcomeAdmin : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome_admin)

        val register = findViewById<Button>(R.id.register_btn)
        register.setOnClickListener {
            val intent = Intent(this, Training::class.java)
            startActivity(intent)
        }

        val btnManageVideos = findViewById<Button>(R.id.btnManageVideos)

        btnManageVideos.setOnClickListener {
            val intent =Intent(this, Video::class.java)
            startActivity(intent)

        }


        val logout = findViewById<ImageButton>(thesis.filconnected.R.id.logout)
        logout.setOnClickListener {
            // Inflate the modal's view
            val modalView = layoutInflater.inflate(R.layout.dialog_admin_logout, null)

            // Create a Dialog
            val dialog = Dialog(this)
            dialog.window?.setGravity(Gravity.CENTER) // Ensure positioning before setting content
            dialog.setContentView(modalView)

            // Set width & height
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // Transparent bg

            // Find buttons inside the modal
            val yes: TextView = modalView.findViewById(thesis.filconnected.R.id.yesBtn)
            val cancel: TextView = modalView.findViewById(thesis.filconnected.R.id.cancelBtn)

            // Yes button logs out
            yes.setOnClickListener {
                val intent = Intent(this, SignIn::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                dialog.dismiss() // Dismiss dialog
            }

            // Cancel button closes dialog instead of navigating away
            cancel.setOnClickListener {
                dialog.dismiss()
            }

            // Show the dialog
            dialog.show()
        }



    }


}