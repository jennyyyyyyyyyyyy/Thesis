package thesis.filconnected.users



import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import thesis.filconnected.R
import thesis.filconnected.admin.SignIn
import thesis.filconnected.users.apptutorial.AppOneFslToText

class HomePage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_page)

        // Button 1 Navigation
        val btnFslToText = findViewById<Button>(R.id.btn_fsl_to_text)
        btnFslToText.setOnClickListener {
            val intent = Intent(this, FSLtoText::class.java)
            startActivity(intent)
        }

        // Button 2 Navigation
        val btnTextToFsl = findViewById<Button>(R.id.text_to_fsl)
        btnTextToFsl.setOnClickListener {
            val intent = Intent(this, TextToFsl::class.java)
            startActivity(intent)
        }

        // Button 3 Navigation
        val btnAppTutorial = findViewById<Button>(R.id.app_tutorial)
        btnAppTutorial.setOnClickListener {
            val intent = Intent(this, AppOneFslToText ::class.java)
            startActivity(intent)
        }


        val modal = findViewById<ImageView>(R.id.menu)
        modal.setOnClickListener {
            // Inflate the modal's view (menu_modal.xml)
            val modalView = layoutInflater.inflate(R.layout.dialog_homepage_menu, null)

            // Create a Dialog
            val dialog = Dialog(this)
            dialog.setContentView(modalView)

            // Set the dialog's width and height to wrap content
            dialog.window?.setLayout(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            // Set the position to the top-left corner
            dialog.window?.setGravity(Gravity.TOP or Gravity.START)


            val aboutUsLayout: LinearLayout = modalView.findViewById(R.id.about_us)

            aboutUsLayout.setOnClickListener {
                val intent = Intent(this, About::class.java)
                startActivity(intent)
            }

            val devInfo: LinearLayout = modalView.findViewById(R.id.dev_info)


            devInfo.setOnClickListener {
                val intent = Intent(this, DevInfo::class.java)
                startActivity(intent)
            }

            val maintenance: LinearLayout = modalView.findViewById(R.id.maintenance)
            maintenance.setOnClickListener {
                val intent = Intent(this, SignIn::class.java)
                startActivity(intent)
            }
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            // Show the dialog
            dialog.show()
        }



    }
}
