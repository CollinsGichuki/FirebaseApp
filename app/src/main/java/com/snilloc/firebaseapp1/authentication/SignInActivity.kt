package com.snilloc.firebaseapp1.authentication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.snilloc.firebaseapp1.R
import com.snilloc.firebaseapp1.databinding.ActivitySignInBinding
import com.snilloc.firebaseapp1.ui.MainActivity

private const val TAG = "Sign_in_activity"
private const val RC_SIGN_IN = 9001// Random Number

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //Google sign-in
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        binding.apply {
            email = emailAddressEt.text.toString()
            password = passwordEd.text.toString()

            btnSignIn.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                singIn()
            }
            signUpTv.setOnClickListener {
                signUpWithEmailAndPassword()
            }
            forgetPasswordTv.setOnClickListener {
                passwordResetActivity()
            }
            googleSignInTv.setOnClickListener {
                signInWithGoogle()
            }
        }
    }

    //check if user has been signed in
    public override fun onStart() {
        super.onStart()
        //Check whether the user is signed in(non-null)
        val currentUser = auth.currentUser
        updateUi(currentUser)
    }

    private fun updateUi(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            val mainActivityIntent = Intent(this, MainActivity::class.java)
            //Verify that the Intent will open up the Activity without any problems
            if (mainActivityIntent.resolveActivity(packageManager) != null) {
                startActivity(mainActivityIntent)
                finish()
            }
        }
    }

    private fun signInWithGoogle() {
        //Sign out the previous google account used to sign in
        googleSignInClient.signOut()
        Log.d(TAG, "Creating Sign-in Intent")
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == RC_SIGN_IN) {
            Log.d(TAG, "Result Ok")
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            Log.d(TAG, "Data from Intent, $data")
            try {
                //If sign-in was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "firebaseAuthWithGoogle:" + account?.id);
                account!!.idToken?.let { firebaseAuthWithGoogle(it) }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e);
            }
        } else {
            Log.d(TAG, "Result Not Okay. Result Code: $resultCode")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credentials = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credentials)
            .addOnCompleteListener {
                if (it.isComplete) {
                    if (it.isSuccessful) {
                        Log.d(TAG, "signInWithCredential:success")
                        goToHomeActivity()
                        finish()
                    } else {
                        Log.d(TAG, "signInWithCredential:failure. ${it.exception}")
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_LONG);
                    }
                }
            }
    }

    private fun signUpWithEmailAndPassword() {
        val intent = Intent(this, SignUpActivity::class.java)
        //Verify that the Intent will open up the Activity without any problems
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun passwordResetActivity() {
        val intent = Intent(this, ResetPasswordActivity::class.java)
        //Verify that the Intent will open up the Activity without any problems
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun singIn() {
        if (!validateForm()) {
            return
        }
        //Show progress bar
        binding.progressBar.isVisible

        val email = binding.emailAddressEt.text.toString()
        val password = binding.passwordEd.text.toString()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "signedInUserWithEmail:success")
                //Check if user is verified
                val user = auth.currentUser
                if (user!!.isEmailVerified) {
                    goToHomeActivity()
                    finish()
                } else {
                    binding.emailVerifyTv.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(baseContext, "Incorrect Email or Password", Toast.LENGTH_SHORT)
                    .show()
            }
            //Hide ProgressBar
            binding.progressBar.visibility = View.INVISIBLE
        }.addOnFailureListener(this) { exception: Exception ->
            Toast.makeText(this, "Signing in failed: $exception", Toast.LENGTH_LONG)
        }
    }

    private fun goToHomeActivity() {
        Log.d(TAG, "Goinghom3")
        val intent = Intent(this, MainActivity::class.java)
        //Verify that the Intent will open up the Activity without any problems
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            finish()
        }
    }

    private fun validateForm(): Boolean {
        //Validate the email and password
        var valid = true

        val email = binding.emailAddressEt.text.toString()
        if (TextUtils.isEmpty(email)) {
            binding.emailAddressEt.error = "Required."
            valid = false
        } else {
            binding.emailAddressEt.error = null
        }
        val password = binding.passwordEd.text.toString()
        if (TextUtils.isEmpty(password)) {
            binding.passwordEd.error = "Required"
            valid = false
        } else {
            binding.passwordEd.error = null
        }
        return valid
    }
}