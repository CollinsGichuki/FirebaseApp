package com.snilloc.firebaseapp1

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.snilloc.firebaseapp1.databinding.ActivityMainBinding
import java.util.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private val TAG = "MAIN_ACTIVITY"
    private lateinit var downloadPhotoUri: Uri
    private lateinit var uploadPhotoUri: Uri
    private lateinit var allThePhotoNames: MutableList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Initialize the auth
        auth = Firebase.auth
        //Initialize FirebaseStorage
        storage = Firebase.storage
        //Initialize allPhotonames to an empty mutableList
        allThePhotoNames = mutableListOf()

        //Create a Storage Reference
        //Points to the root
        storageReference = storage.reference

        //Get the photos stored
        getAllPhotos()

        //Points to child "meme-photos"
        //loadImage()

        binding.apply {
            chooseBtn.setOnClickListener {
                choosePhotoFromGallery()
            }
            uploadBtn.setOnClickListener {
                uploadPhotoToFirebase()
            }
        }

    }

    private fun getAllPhotos() {
        Log.d(TAG, "Getting all photos")
        //Reference to the bucket containing all the photos
        val allPhotosRef = storageReference.child("memes-photos/")

//        listOf(allPhotosRef.downloadUrl.addOnSuccessListener {
//            Log.d(TAG, "List download successful")
//            val photoUrl = it
//            Log.d(TAG, "PhotoUrl: $photoUrl")
//        }.addOnCanceledListener {
//            Log.d(TAG, "List download unsuccessful")
//        })

        allPhotosRef.listAll().addOnSuccessListener {
            val results = it.items.size
            val photoLists: List<StorageReference> = it.items


            for (photoList in photoLists) {
                val photoUrl = photoList.name
                allThePhotoNames.add(photoUrl)
                Log.d(TAG, "This the url: ${photoUrl}/n}")
            }
            if (allThePhotoNames.size == photoLists.size) {
                val randomUrlPosition = (0..allThePhotoNames.size).random()
                val randomPhotoUrl = allThePhotoNames[randomUrlPosition]
                loadImage(randomPhotoUrl)
            }
//            val downloadedUrl = photoLists[1]
//            Log.d(TAG, "The first item: $downloadedUrl}")

            Log.d(TAG, "Size of list is: $results")
        }.addOnFailureListener(this) { exception: Exception ->
            Toast.makeText(this, "Getting all photos failed: $exception", Toast.LENGTH_LONG).show()
            Log.d(TAG, exception.toString())
        }
    }

    private fun loadImage(photoUrl: String) {
        binding.progressBar.visibility = View.VISIBLE
        //"memes-photos/1528372.jpg"
        storageReference.child("memes-photos/$photoUrl").downloadUrl.addOnSuccessListener(this) {
            Log.d(TAG, "Download successful")
            downloadPhotoUri = it
            Log.d(TAG, "Photo url: $downloadPhotoUri")
            binding.progressBar.visibility = View.INVISIBLE
            loadPhotos()
        }.addOnFailureListener(this) { exception: Exception ->
            binding.progressBar.visibility = View.INVISIBLE
            Toast.makeText(this, "Creating account failed: $exception", Toast.LENGTH_LONG).show()
            Log.d(TAG, exception.toString())
        }
    }

    private fun loadPhotos() {
        val imageView = findViewById<ImageView>(R.id.image1)
        Glide.with(this)
            .load(downloadPhotoUri)
            .centerCrop()
            .error(R.drawable.ic_baseline_error_24)
            .into(imageView)

        Log.d(TAG, downloadPhotoUri.toString())
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

    private fun choosePhotoFromGallery() {
        val intent = Intent()
            .setType("image/*")
            .setAction(Intent.ACTION_GET_CONTENT)

        startActivityForResult(Intent.createChooser(intent, "Select photo"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            uploadPhotoUri = data.data!!
            val bitmap: Bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uploadPhotoUri)
            //Set the image
            binding.image1.setImageBitmap(bitmap)
        }
    }

    private fun uploadPhotoToFirebase() {
        if (this::uploadPhotoUri.isInitialized) {
            //Show the progress bar
            binding.progressBar.visibility = View.VISIBLE

            //Create random uuid to upload the photos
            val uuid = UUID.randomUUID()
            val photoUuid = uuid.toString()

            storageReference.child("memes-photos/$photoUuid.jpg")
                .putFile(uploadPhotoUri)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.INVISIBLE
                    Log.d(TAG, "Photo uploaded successfully")
                    Toast.makeText(this, "Photo successfully uploaded", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener(this) { exception: Exception ->
                    Log.d(TAG, "Photo upload failed: $exception")
                    Toast.makeText(this, "Error uploading photo: $exception", Toast.LENGTH_LONG)
                        .show()

                }
        } else {
            Toast.makeText(this, "Choose a photo to upload", Toast.LENGTH_LONG).show()
            Log.d(TAG, "No photo selected")
            return
        }
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.sign_out_menu_item) {
            signOut()
            true
        } else {
            super.onOptionsItemSelected(item)
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