package com.example.wavesoffood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.wavesoffood.Model.UserModel
import com.example.wavesoffood.databinding.ActivityLoginBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import kotlinx.coroutines.launch


class LoginActivity : AppCompatActivity() {
    private lateinit var email : String
    private lateinit var password : String
    private lateinit var auth : FirebaseAuth
    private lateinit var database : DatabaseReference
    private val binding : ActivityLoginBinding by lazy{
        ActivityLoginBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        //Initialising firebase & database
        auth = Firebase.auth
        database = Firebase.database.reference

        binding.donthavebutton.setOnClickListener{
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
            finish()
        }
        //Login with email-password
        binding.loginButton.setOnClickListener{
            //get data from text field
            email = binding.email.text.toString().trim()
            password = binding.password.text.toString().trim()

            if(email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Please enter all the details" , Toast.LENGTH_SHORT).show()
            }
            else{
                login(email, password)
            }
        }
        binding.googleButton.setOnClickListener{
            launchGoogleSignIn()
        }
    }
    private fun launchGoogleSignIn() {
        // Build GetGoogleIdOption — server client id (web client id) required
        val googleIdOption = GetGoogleIdOption.Builder()
            .setServerClientId(getString(R.string.default_web_client_id)) // must be web client id
            .setFilterByAuthorizedAccounts(false) // only accounts previously used (change if you want all)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val credentialManager = CredentialManager.create(this)

        // call in coroutine (getCredential is suspend)
        lifecycleScope.launch {
            try {
                val response = credentialManager.getCredential(context = this@LoginActivity, request = request)
                handleGoogleCredentialResponse(response.credential)
            } catch (e: GetCredentialException) {
                // user cancelled, no credentials, or other recoverable issues
                Log.w("TAG", "Credential Manager getCredential failed", e)
                Toast.makeText(this@LoginActivity, "Google sign-in cancelled or unavailable", Toast.LENGTH_SHORT).show()
            } catch (t: Throwable) {
                Log.e("TAG", "Unexpected error during Google sign-in", t)
                Toast.makeText(this@LoginActivity, "Google sign-in error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleCredentialResponse(credential: androidx.credentials.Credential?) {
        if (credential == null) {
            Toast.makeText(this, "No credential returned", Toast.LENGTH_SHORT).show()
            return
        }

        // We expect a Google ID token credential (GoogleIdTokenCredential)
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            try {
                val googleIdTokenCred = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCred.idToken
                if (idToken.isNotEmpty()) {
                    firebaseAuthWithGoogle(idToken)
                } else {
                    Toast.makeText(this, "No ID token received", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("TAG", "Failed to parse GoogleIdTokenCredential", e)
                Toast.makeText(this, "Google token parsing failed", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w("TAG", "Received unexpected credential type: ${credential::class.java}")
            Toast.makeText(this, "Unexpected credential received", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Ensure FirebaseAuth user is reloaded before using it
                    auth.currentUser?.reload()?.addOnSuccessListener {
                        val freshUser = auth.currentUser
                        ensureUserRecordForGoogle(freshUser)
                    }
                } else {
                    Log.w("TAG", "signInWithCredential:failure", task.exception)
                    Toast.makeText(this, "Firebase auth failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun ensureUserRecordForGoogle(user: FirebaseUser?) {
        if (user == null) return
        val userRef = database.child("user").child(user.uid)
        userRef.get().addOnSuccessListener { snap ->
            if (!snap.exists()) {
                // First time Google sign-in → create user record
                val model = UserModel(
                    name = user.displayName ?: "",
                    email = user.email ?: "",
                    password = "" // Google login doesn’t need password
                )
                userRef.setValue(model).addOnSuccessListener {
                    updateUI(user) // ✅ go to MainActivity
                }
            } else {
                // Existing Google account → just continue to app
                Toast.makeText(this, "Welcome back ${user.displayName ?: ""}!", Toast.LENGTH_SHORT).show()
                updateUI(user) // ✅ go to MainActivity
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Database error: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun login(email : String, password : String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            val user = auth.currentUser
            if(user != null){
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            else {
                // Login failed
                Log.d("Login" , "Login failed : ${task.exception?.message}")
                Toast.makeText(this, "Login failed: Account does not exists", Toast.LENGTH_SHORT).show()
            }
        }
    }
    //check if user is already logged in
    override fun onStart() {
        super.onStart()
        val currUser = auth.currentUser
        if(currUser!= null){
            Log.d("AddItemActivity", "Current user UID: ${auth.currentUser?.uid}, Email: ${auth.currentUser?.email}")
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}