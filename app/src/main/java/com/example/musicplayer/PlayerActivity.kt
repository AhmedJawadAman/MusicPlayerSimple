package com.example.musicplayer

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer

class PlayerActivity : AppCompatActivity() {

    private var isPlaying = true

    private lateinit var buttonPlayPause: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        buttonPlayPause = findViewById(R.id.btn_play_pause)

        val filePath = intent.getStringExtra("filePath")
        val songTitle = intent.getStringExtra("songTitle") ?: "Unknown Song"

        if (filePath != null) {
            val serviceIntent = Intent(this, MusicService::class.java).apply {
                putExtra("filePath", filePath)
                putExtra("songTitle", songTitle)
                action = "PLAY"
            }
            startService(serviceIntent)
        }

        buttonPlayPause.setOnClickListener {
            val action = if (isPlaying) "PAUSE" else "PLAY"
            val serviceIntent = Intent(this, MusicService::class.java).apply {
                this.action = action
            }
            startService(serviceIntent)
            buttonPlayPause.text = if (isPlaying) "Play" else "Pause"
            isPlaying = !isPlaying
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

    override fun onDestroy() {
        super.onDestroy()
        // Do not stop the service when the activity is destroyed to keep music playing
    }
}
