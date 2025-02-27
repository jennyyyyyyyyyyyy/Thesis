package thesis.filconnected

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout


class Modal : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_homepage_menu)


        val aboutUsLayout = findViewById<LinearLayout>(R.id.about_us)
        val devInfoLayout = findViewById<LinearLayout>(R.id.dev_info)

        aboutUsLayout.setOnClickListener {
            aboutUsLayout.setBackgroundResource(R.drawable.with_border1_white_radius10)

        }

        devInfoLayout.setOnClickListener {
            devInfoLayout.setBackgroundResource(R.drawable.with_border1_white_radius10)

        }

    }
}