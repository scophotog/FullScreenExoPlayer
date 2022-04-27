package com.example.fullscreenexoplayer

import android.content.Context
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class ExoPlayerHelper(private val context: Context) {

    private var _player: ExoPlayer? = null
    val player: ExoPlayer
        get() = _player!!

    fun initializePlayer(): ExoPlayer {
        _player = if (_player == null) {
            ExoPlayer.Builder(context).build()
        } else {
            _player
        }
        return player
    }

    fun releasePlayer() {
        player.release()
        _player = null
    }

}