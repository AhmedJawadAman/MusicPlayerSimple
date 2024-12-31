package com.example.musicplayer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 101
    private val REQUEST_NOTIFICATION_PERMISSION = 102
    private lateinit var recyclerView: RecyclerView
    private lateinit var mp3Adapter: Mp3Adapter
    private lateinit var buttonPlayPause: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        mp3Adapter = Mp3Adapter(emptyList()) { audioFile ->
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("filePath", audioFile.path)
            intent.putExtra("songTitle", audioFile.name)
            startActivity(intent)
        }

        recyclerView.adapter = mp3Adapter

        checkNotificationPermission()

        if (checkPermission()) {
            fetchMp3Files()
        } else {
            requestPermission()
        }


        buttonPlayPause = findViewById(R.id.btn_play_pause)

        // Observe play/pause state
        MusicPlayerViewModel.isPlaying.observe(this, Observer { isPlaying ->
            buttonPlayPause.text = if (isPlaying) "Pause" else "Play"
        })

        buttonPlayPause.setOnClickListener {
            val isPlaying = MusicPlayerViewModel.isPlaying.value ?: false
            val action = if (isPlaying) "PAUSE" else "PLAY"
            val serviceIntent = Intent(this, MusicService::class.java).apply {
                this.action = action
            }
            startService(serviceIntent)
        }
    }

    private fun checkPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        ActivityCompat.requestPermissions(this, arrayOf(permission), REQUEST_CODE)
    }

    private fun fetchMp3Files() {
        val audioList = mutableListOf<AudioFile>()
        val projection = arrayOf(MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DISPLAY_NAME)


        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            null
        )

        cursor?.use {
            val dataIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA)
            val nameIndex = it.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)

            while (it.moveToNext()) {
                val filePath = it.getString(dataIndex)
                val fileName = it.getString(nameIndex)

                if (filePath.endsWith(".mp3")) {
                    audioList.add(AudioFile(fileName, filePath))
                }
            }
        }

        mp3Adapter.updateData(audioList)
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_NOTIFICATION_PERMISSION
                )
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchMp3Files()
            } else {
                Toast.makeText(this, "Permission denied. Unable to fetch MP3 files.", Toast.LENGTH_SHORT).show()
            }
        } else if (requestCode == REQUEST_NOTIFICATION_PERMISSION) {
            if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                showNotificationPermissionDialog()
            }
        }
    }

    private fun showNotificationPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("Enable Notifications")
            .setMessage("Notification permission is required to display playback controls. Please enable it in settings.")
            .setPositiveButton("Open Settings") { _, _ ->
                val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                    putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                }
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
