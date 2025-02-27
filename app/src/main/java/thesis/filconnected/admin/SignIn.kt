package thesis.filconnected.admin

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import thesis.filconnected.R
import thesis.filconnected.users.HomePage
import java.io.IOException

class SignIn : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 100  // Request code for Google Sign-In

    // Define the allowed email address
    private val allowedEmail = "filconnected.pdm.2024@gmail.com"  // Replace with the email you want to allow
    private val sharedPrefFile = "thesis.filconnected.admin.PREFERENCE_FILE_KEY"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        // Check if the user is already signed in
//        val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
//        val savedEmail = sharedPreferences.getString("signedInEmail", null)
//
//        // If the user is already signed in, navigate directly to the Admin UI
//        if (savedEmail == allowedEmail) {
//            navigateToAdminUI()
//            return
//        }

        val signInButton = findViewById<Button>(R.id.next)

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.google_client_id))  // Use your web client ID
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        // Sign-In Button Click
        signInButton.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)!!
            Log.d("Google SignIn", "Success: ${account.displayName}, Email: ${account.email}")

            if (account.email == allowedEmail) {
                // Save the sign-in status in SharedPreferences
                val sharedPreferences: SharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("signedInEmail", account.email)  // Save the signed-in email
                editor.apply()

                Toast.makeText(this, "Signed in as Admin", Toast.LENGTH_SHORT).show()
                navigateToAdminUI()
            } else {
                // Unauthorized email attempt
                Toast.makeText(this, "Sign-In Failed. Please try again.", Toast.LENGTH_SHORT).show()

                // Logout user from Google to allow retry
                googleSignInClient.signOut().addOnCompleteListener {
                    Log.d("Google SignIn", "User signed out after unauthorized attempt")
                }
            }

        } catch (e: ApiException) {
            Log.e("Google SignIn", "Failed: ${e.statusCode}")
            Toast.makeText(this, "Sign-In Failed. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToAdminUI() {
        val intent = Intent(this, WelcomeAdmin::class.java)
        startActivity(intent)
//        finish()  // Optionally, close the SignIn activity to prevent going back
    }

}
