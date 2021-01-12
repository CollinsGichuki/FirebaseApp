package com.snilloc.firebaseapp1

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.snilloc.firebaseapp1.databinding.ActivitySignUpBinding

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var auth: FirebaseAuth
    private val TAG = "SignUpActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        // Initialize Firebase Auth
        auth = Firebase.auth

        binding.apply {
            btnSignUp.setOnClickListener {
                signUp()
            }
            signInTv.setOnClickListener {
                goToSignInActivity()
            }
        }
    }

    private fun goToSignInActivity() {
        val intent = Intent(this, SignInActivity::class.java)
        //Verify that the Intent will open up the Activity without any problems
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            finish()
        }
    }

    private fun signUp() {
        if (!validateForm()) {
            return
        }
        //Show progress bar
        binding.progressBar.isVisible

        //Get the values entered
        val email = binding.emailAddressEt.text.toString()
        val password = binding.passwordEd.text.toString()
        val name = binding.nameEt.text.toString()

        //Log the email address used
        Log.d(TAG, "create account:$email")

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                //SignIn successful
                Log.d(TAG, "createUserWithEmail:success")
                val user = auth.currentUser

                //Send verification email
                user!!.sendEmailVerification().addOnCompleteListener(this) {
                    if (task.isSuccessful){
                        //Verify email to log in
                        Toast.makeText(this, "Verify email to log in", Toast.LENGTH_LONG)
                    } else {
                        Toast.makeText(this, "There was an error sending your email verification", Toast.LENGTH_LONG)
                    }
                }.addOnFailureListener(this) { exception: Exception ->
                    Toast.makeText(this, "Sending the verification email failed: $exception", Toast.LENGTH_LONG)
                }
                finish()
            } else {
                //Sing in fails
                Toast.makeText(baseContext, "Sign Up Failed", Toast.LENGTH_SHORT).show()
            }
            //Hide Progress bar
            binding.progressBar.visibility = View.INVISIBLE
        }.addOnFailureListener(this) { exception: Exception ->
            Toast.makeText(this, "Creating account failed: $exception", Toast.LENGTH_LONG)
        }
    }

    private fun validateForm(): Boolean {
        //Validate the email and password
        var valid = true

        val name = binding.nameEt.text.toString()
        if (TextUtils.isEmpty(name)) {
            binding.nameEt.error = "Required"
            valid = false
        } else {
            binding.nameEt.error = null
        }

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
        } else if(password.length<6){
            binding.passwordEd.error = "Password should be more than 6 characters"
            valid = false
        } else {
            binding.passwordEd.error = null
        }

        return valid
    }
}