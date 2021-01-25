package com.snilloc.firebaseapp1.ui

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.*
import android.webkit.URLUtil
import android.widget.MediaController
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.snilloc.firebaseapp1.R
import com.snilloc.firebaseapp1.databinding.ActivityVideoBinding
import java.util.*

private const val GET_VIDEO = 123
private const val TAG = "VIDEO_ACTIVITY"

class VideoActivity : AppCompatActivity() {
    private lateinit var selectedVideoUri: Uri
    private lateinit var binding: ActivityVideoBinding
    private lateinit var simpleVideoView: VideoView
    private lateinit var mediaController: MediaController
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityVideoBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        //Keeps the phone awake when using the app
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        //Initialize FirebaseStorage
        storage = Firebase.storage
        //Create a Storage Reference
        //Points to the root
        storageReference = storage.reference

        //Initialize the simpleVideoView
        simpleVideoView = binding.videoView
        //Initialize the MediaController
        mediaController = MediaController(this@VideoActivity)

        mediaController.setAnchorView(this.simpleVideoView)

        //Connect the MediaController with the Videoview
        mediaController.setMediaPlayer(simpleVideoView)
        //Connect the VideoView with the MediaController
        simpleVideoView.setMediaController(mediaController)

        binding.uplaodVideoBtn.setOnClickListener {
            uploadVideoToFirebase()
        }
    }

    private fun uploadVideoToFirebase() {
        if (this::selectedVideoUri.isInitialized) {
            binding.progressBar.visibility = View.VISIBLE
            //Create random uuid to upload the photos
            val uuid = UUID.randomUUID()
            val videoUuid = uuid.toString()

            storageReference.child("videos/$videoUuid.mp4")
                .putFile(selectedVideoUri)
                .addOnSuccessListener {
                    binding.progressBar.visibility = View.INVISIBLE
                    Log.d(TAG, "Video uploaded successfully")
                    Toast.makeText(this, "Video successfully uploaded", Toast.LENGTH_LONG).show()
                }.addOnFailureListener(this) { exception: Exception ->
                    Log.d(TAG, "Video upload failed: $exception")
                    Toast.makeText(this, "Error uploading video: $exception", Toast.LENGTH_LONG)
                        .show()
                }
        } else {
            Toast.makeText(this, "Choose a video to upload", Toast.LENGTH_LONG).show()
            Log.d(TAG, "No video selected")
            return
        }
    }

    private fun initializePlayer() {
        //Show the Progress Bar
        binding.progressBar.visibility = View.VISIBLE
        //Set the selectedVideo's url to be displayed on the VideoView
        simpleVideoView.setVideoURI(selectedVideoUri)

        //Listen for when the video has finished buffering
        simpleVideoView.setOnPreparedListener {
            binding.progressBar.visibility = View.INVISIBLE
            //Start playing the video
            simpleVideoView.requestFocus()
            simpleVideoView.start()
        }

        //Show text and seek to the beginning of the video when the video ends
        binding.videoView.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            Toast.makeText(this, "Playback completed", Toast.LENGTH_SHORT).show()
            binding.videoView.seekTo(1)
        })
    }

    private fun releasePlayer() {
        simpleVideoView.stopPlayback()
    }

    override fun onStop() {
        super.onStop()
        //Stop playing the video
        releasePlayer()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_video_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.choose_video) {
            Log.d("Video_Activity", "Options Selected")
            openGalleryToSelectVideo()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    private fun openGalleryToSelectVideo() {
        //Open gallery to fetch a video
        Log.d("Video_Activity", "openGalleryToSelectVideo")
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select file"), GET_VIDEO)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == GET_VIDEO && data != null) {
            selectedVideoUri = data.data!!
            initializePlayer()
        }
    }
}