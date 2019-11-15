package com.ust.qspace.services

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.ust.qspace.R

class MusicService: Service() {

    lateinit var player: MediaPlayer

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        player = MediaPlayer.create(this, R.raw.qspacemain)
        player.isLooping = true
        player.setVolume(80f, 80f)

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        player.start()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()

        player.stop()
    }
}