package com.snilloc.firebaseapp1

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.snilloc.firebaseapp1.databinding.ActivityResetPasswordBinding

class ResetPasswordActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResetPasswordBinding
    private lateinit var auth: FirebaseAuth
    private var TAG = "RESET_PASSWORD_ACTIVITY"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResetPasswordBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        binding.btnSendEmail.setOnClickListener {
            sendPasswordResetEmail()
        }
    }

    private fun sendPasswordResetEmail() {
        if (!validateForm()){
           return
        }
        binding.progressBar.visibility = View.VISIBLE
        val email = binding.emailAddressEt.text.toString()
        auth.setLanguageCode("en")
        auth.sendPasswordResetEmail(email).addOnCompleteListener(this) { task ->
            if (task.isSuccessful){
                //Update the UI
                    Log.d(TAG, "email sent successfully")
                updateUi("Check email to reset password")
            } else {
                Toast.makeText(this, "There was a problem in sending the email", Toast.LENGTH_LONG)
            }
        }.addOnFailureListener(this) { exception: Exception ->
            Log.d(TAG, "failure, $email  $exception")
            updateUi("Failed $exception")
        }
    }

    private fun updateUi(text: String) {
        //Change the text and make the other views invisible
        binding.apply {
            progressBar.visibility = View.INVISIBLE
            emailTv.text = text
            emailAddressEt.visibility = View.INVISIBLE
            btnSendEmail.visibility = View.INVISIBLE
        }
    }

    private fun validateForm() : Boolean {
        var valid = true

        val email = binding.btnSendEmail.text.toString()
        if (TextUtils.isEmpty(email)){
            binding.btnSendEmail.error = "Required"
            valid = false
        }

        return valid
    }
}