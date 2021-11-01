package com.snilloc.firebaseapp1.ui

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.snilloc.firebaseapp1.databinding.ActivityAddImageBinding
import java.util.*

private const val TAG = "ADD_IMAGE_ACTIVITY"

class AddImageActivity: AppCompatActivity() {
    private lateinit var binding: ActivityAddImageBinding
    private lateinit var uploadPhotoUri: Uri
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddImageBinding.inflate(layoutInflater)

        val view = binding.root
        setContentView(view)

        //Initialize FirebaseStorage
        storage = Firebase.storage


        //Create a Storage Reference
        //Points to the root
        storageReference = storage.reference

        binding.apply {
            chooseBtn.setOnClickListener {
                choosePhotoFromGallery()
            }

            uploadBtn.setOnClickListener {
                uploadPhotoToFirebase()
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
            Log.d(TAG, "Photo Uri: $uploadPhotoUri")
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
}