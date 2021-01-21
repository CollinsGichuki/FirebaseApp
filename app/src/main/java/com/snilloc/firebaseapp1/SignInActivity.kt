package com.snilloc.firebaseapp1

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.snilloc.firebaseapp1.databinding.ActivitySignInBinding

private const val TAG = "MainActivity"

class SignInActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var binding: ActivitySignInBinding
    private lateinit var signInViewModel: SignInViewModel
    private lateinit var email: String
    private lateinit var password: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth

        //ViewModel
        signInViewModel = ViewModelProvider(this).get(SignInViewModel::class.java)

        email = binding.emailAddressEt.text.toString()
        password = binding.passwordEd.text.toString()

        binding.apply {
            btnSignIn.setOnClickListener {
                progressBar.visibility = View.VISIBLE
                viewModelSignIn()

                singIn()
            }
            signUpTv.setOnClickListener {
                signUp()
            }
            forgetPasswordTv.setOnClickListener {
                passwordResetActivity()
            }
        }
    }

    private fun viewModelSignIn() {
        val status = signInViewModel.signIn(email, password)

        if (status == 0) {

        }
    }

    private fun signUp() {
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