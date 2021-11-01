package com.snilloc.firebaseapp1.ui

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.snilloc.firebaseapp1.R
import com.snilloc.firebaseapp1.authentication.SignInActivity
import com.snilloc.firebaseapp1.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

private const val TAG = "MAIN_ACTIVITY"

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private lateinit var allThePhotoNames: MutableList<String>
    private var someValue: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Initialize the auth
        auth = Firebase.auth

        //Initialize FirebaseStorage
        storage = Firebase.storage

        //Initialize allPhotoNames to an empty mutableList
        allThePhotoNames = mutableListOf()

        //Create a Storage Reference
        //Points to the root
        storageReference = storage.reference

        //Get the photos stored
        listFiles()
    }

    override fun onResume() {
        super.onResume()
        //Check if imageUrls is empty
        if (someValue == 1) {
            listFiles()
        }
    }

    private fun listFiles() = CoroutineScope(Dispatchers.IO).launch {
        //Show progress bar in the main ui thread
        withContext(Dispatchers.Main) {
            binding.progressBar.visibility = View.VISIBLE
        }

        try {
            val images = storageReference.child("memes-photos/").listAll().await()
            val imageUrls = mutableListOf<String>()

            for (image in images.items) {
                val url = image.downloadUrl.await()
                imageUrls.add(url.toString())
            }
            withContext(Dispatchers.Main) {
                binding.progressBar.visibility = View.INVISIBLE

                val imageAdapter = ImagesAdapter(imageUrls)
                binding.imageRecyclerview.apply {
                    adapter = imageAdapter
                    layoutManager = LinearLayoutManager(this@MainActivity)
                }
            }

        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sign_out_menu_item -> {
                signOut()
                true
            }
            R.id.add_image_activity -> {
                addImageActivity()
                true
            }
            R.id.video_activity -> {
                goToVideoActivity()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun addImageActivity() {
        val intent = Intent(this, AddImageActivity::class.java)
        //Verify that the Intent will open up the Activity without any problems
        if (intent.resolveActivity(packageManager) != null) {
            someValue = 1 //Update the value
            startActivity(intent)
        }
    }

    private fun goToVideoActivity() {
        val intent = Intent(this, VideoActivity::class.java)
        //Verify that the Intent will open up the Activity without any problems
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun signOut() {
        auth.signOut()
        //Go to Sign-in activity
        goToSignIn()
    }

    private fun goToSignIn() {
        val intent = Intent(this, SignInActivity::class.java)
        //Verify that the Intent will open up the Activity without any problems
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
            finish()
        }
    }
}