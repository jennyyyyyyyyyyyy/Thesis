package thesis.filconnected.both

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import thesis.filconnected.R
import kotlinx.coroutines.*
import thesis.filconnected.users.HomePage

class Start : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // SharedPreferences para matandaan kung unang bukas ng app
        val sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val isFirstLaunch = sharedPreferences.getBoolean("isFirstLaunch", true)

        if (!isFirstLaunch) {
            // Kung hindi first time, diretso na sa HomePage
            goToHomePage()
            return
        }

        // Kung first time lang, ipapakita ang Start UI
        setContentView(R.layout.activity_start)

        // Find the TextView
        val textView = findViewById<TextView>(R.id.start_animation)

        // Load the fade-in animation
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        textView.startAnimation(fadeIn)

        // Delay for 3 seconds bago lumipat sa HomePage
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)

            // Mark this as not first launch
            sharedPreferences.edit().putBoolean("isFirstLaunch", false).apply()

            goToHomePage()
        }
    }

    private fun goToHomePage() {
        val intent = Intent(this, HomePage::class.java)
        startActivity(intent)
        finish() // Para hindi na makabalik sa Start UI
    }
}
