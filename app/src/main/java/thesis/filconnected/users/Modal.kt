package thesis.filconnected.users

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import thesis.filconnected.R

class Modal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_homepage_menu)


        val aboutUsLayout = findViewById<LinearLayout>(R.id.about_us)
        val devInfoLayout = findViewById<LinearLayout>(R.id.dev_info)
        val maintenanceLayout = findViewById<LinearLayout>(R.id.maintenance)

// Add a border to the selected layout
        aboutUsLayout.setOnClickListener {
            aboutUsLayout.setBackgroundResource(R.drawable.with_border1_white_radius10)

        }

        devInfoLayout.setOnClickListener {
            devInfoLayout.setBackgroundResource(R.drawable.with_border1_white_radius10)

        }
        maintenanceLayout.setOnClickListener {
            devInfoLayout.setBackgroundResource(R.drawable.with_border1_white_radius10)

        }

    }
}