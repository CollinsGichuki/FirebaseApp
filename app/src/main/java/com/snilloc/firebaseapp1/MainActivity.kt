package com.snilloc.firebaseapp1

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.snilloc.firebaseapp1.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Initialize the auth
        auth = Firebase.auth

        //Receive the name from the Sign Up Activity
        val intent = getIntent()
        val name =intent.getStringExtra("NAME")

        binding.tvTitle.text = name?: "Hello World"

        Log.d("Home", "Name: $name")
    }

    public override fun onStart() {
        super.onStart()
        //Check whether the user is signed in(non-null)
        val currentUser = auth.currentUser
        updateUi(currentUser)
    }

    private fun updateUi(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            binding.tvTitle.text = "Welcome back"
        } else {
            val signInIntent = Intent(this, SignInActivity::class.java)
            //Verify that the Intent will open up the Activity without any problems
            if (signInIntent.resolveActivity(packageManager) != null) {
                startActivity(signInIntent)
                finish()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
         return if (item.itemId == R.id.sign_out_menu_item){
             signOut()
             true
         } else {super.onOptionsItemSelected(item)}
    }

    private fun signOut() {
        auth.signOut()
        binding.tvTitle.text = "Signed Out"
    }
}