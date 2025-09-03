package com.example.wavesoffood

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.wavesoffood.Model.UserModel
import com.example.wavesoffood.databinding.ActivitySignupBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database
import kotlinx.coroutines.launch

class SignupActivity : AppCompatActivity() {
    private lateinit var email : String
    private lateinit var password : String
    private lateinit var userName : String

    private lateinit var auth: FirebaseAuth
    private lateinit var database : DatabaseReference
    private var isSigningIn = false
    private val binding : ActivitySignupBinding by lazy{
        ActivitySignupBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)
        //Initialising firebase & database
        auth = Firebase.auth
        database = Firebase.database.reference

        binding.createAccountButton.setOnClickListener{
            userName = binding.userName.text.toString()
            email = binding.emailAddress.text.toString().trim()
            password = binding.password.text.toString().trim()

            if(userName.isBlank() || email.isBlank() || password.isBlank()){
                Toast.makeText(this, "Please fill all the details" , Toast.LENGTH_SHORT).show()
            }
            else{
                createAccount(email, password)
            }
        }
        binding.alreadyhaveaccount.setOnClickListener{
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
        binding.googleButton.setOnClickListener{
            launchGoogleSignIn()
        }

    }

    private fun createAccount(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{ task ->
            if(task.isSuccessful){
                Toast.makeText(this, "Account Created Successfully" , Toast.LENGTH_SHORT).show()
                saveUserData()
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()
            }
            else{
                Toast.makeText(this, "Account Creation Failed" , Toast.LENGTH_SHORT).show()
                Log.d("Account", "createAccount: Failure" , task.exception)
            }
        }
    }

    private fun saveUserData() {
        userName = binding.userName.text.toString()
        email = binding.emailAddress.text.toString().trim()
        password = binding.password.text.toString().trim()

        val user = UserModel(userName , email , password)
        val userId = FirebaseAuth.getInstance().currentUser!!.uid
        //saving user data into firebase realtime database
        database.child("user").child(userId).setValue(user)
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
                val response = credentialManager.getCredential(context = this@SignupActivity, request = request)
                handleGoogleCredentialResponse(response.credential)
            } catch (e: GetCredentialException) {
                // user cancelled, no credentials, or other recoverable issues
                Log.w("TAG", "Credential Manager getCredential failed", e)
                Toast.makeText(this@SignupActivity, "Google sign-in cancelled or unavailable", Toast.LENGTH_SHORT).show()
            } catch (t: Throwable) {
                Log.e("TAG", "Unexpected error during Google sign-in", t)
                Toast.makeText(this@SignupActivity, "Google sign-in error", Toast.LENGTH_SHORT).show()
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
        isSigningIn = true
        val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(firebaseCredential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Ensure FirebaseAuth user is reloaded before using it
                    auth.currentUser?.reload()?.addOnSuccessListener {
                        val freshUser = auth.currentUser
                        ensureUserRecordForGoogle(freshUser)
                    }
                    isSigningIn = false
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
                val model = UserModel(name = "" , email = "" , password = "")
                userRef.setValue(model)
            }
            else{
                Toast.makeText(this, "Account already exists, please log in" , Toast.LENGTH_SHORT).show()
                auth.signOut()
            }
        }
    }
    private fun updateUI(user: FirebaseUser?) {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}