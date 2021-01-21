package com.snilloc.firebaseapp1

import android.text.TextUtils
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/*
0 = InvalidFormat
1 = successful and verified
2 = successful but Unverified
3 = unsuccessful
4 = failed
*/

private const val TAG = "SignInViewModel"

class SignInViewModel : ViewModel() {
    private var auth = Firebase.auth
    private var status: Int = 0

    fun signIn(email: String, password: String): Int {
        if (!validateForm(email, password)) {
            status = 0
            return status
        }

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            status = if (task.isSuccessful) {
                Log.d(TAG, "signedInUserWithEmail:success")
                //Check if user is verified
                val user = auth.currentUser
                if (user!!.isEmailVerified) {
                    Log.d(TAG, "signedInUserWithEmail:successful and email verified")
                    1
                } else {
                    Log.d(TAG, "signedInUserWithEmail:successful but unverified ")
                    2
                }
            } else {
                Log.d(TAG, "signedInUserWithEmail:unSuccessful")
                3
            }
        }.addOnFailureListener {
            Log.d(TAG, "signedInUserWithEmail:failed")
            status = 4
        }
        return status
    }

    private fun validateForm(email: String, password: String): Boolean {
        var valid = true
        if (TextUtils.isEmpty(email)) {
            status = 0
            valid = false
        }
        if (TextUtils.isEmpty(password)) {
            status = 1
            valid = false
        }
        return valid
    }
}