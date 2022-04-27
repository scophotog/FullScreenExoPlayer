package com.example.fullscreenexoplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.fullscreenexoplayer.databinding.ActivityMainBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.google.android.exoplayer2.util.Util

class MainActivity : AppCompatActivity() {


    private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(viewBinding.root)
        viewBinding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = PlayerRecyclerViewAdapter()
        }
    }


    class PlayerRecyclerViewAdapter :
        RecyclerView.Adapter<PlayerRecyclerViewAdapter.ViewHolder>() {

        class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {

            val player = view.findViewById<PlayerView>(R.id.player)

            private var _exoPlayerHelper: ExoPlayerHelper? = null

            private val exoPlayerHelper: ExoPlayerHelper
                get() = _exoPlayerHelper!!

            init {
                _exoPlayerHelper = ExoPlayerHelper(view.context)
            }

            private var fullscreen = false

            private lateinit var fullscreenButton: ImageButton


            private var playWhenReady = false
            private var currentWindow = 0
            private var playbackPosition = 0L


            fun initializePlayer() {
                exoPlayerHelper.initializePlayer().also {
                    player.player = it
                    val mediaItem =
                        MediaItem.fromUri("https://storage.googleapis.com/exoplayer-test-media-0/play.mp3")
                    it.setMediaItem(mediaItem)
                    it.playWhenReady = playWhenReady
                    it.seekTo(currentWindow, playbackPosition)
                    it.prepare()
                }

            }


            fun fullScreenStuff() {
                if (fullscreen) {
                    fullscreenButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            view.context,
                            com.google.android.exoplayer2.R.drawable.exo_controls_fullscreen_enter
                        )
                    )

                    (view.context as Activity).window.decorView.systemUiVisibility =
                        View.SYSTEM_UI_FLAG_VISIBLE

                    val params = (player.layoutParams as ConstraintLayout.LayoutParams)
                    params.width = MATCH_PARENT
                    params.height =
                        (200 * (view.context.applicationContext).resources.displayMetrics.density).toInt()
                    player.layoutParams = params
                    fullscreen = false
                } else {
                    fullscreenButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            view.context,
                            com.google.android.exoplayer2.R.drawable.exo_controls_fullscreen_exit
                        )
                    )
                    (view.context as Activity).window.decorView.systemUiVisibility =
                        (View.SYSTEM_UI_FLAG_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)

                    val params = (player.layoutParams as ConstraintLayout.LayoutParams)
                    params.width = MATCH_PARENT
                    params.height = MATCH_PARENT
                    player.layoutParams = params
                    fullscreen = true
                }
            }

            fun releasePlayer() {
                exoPlayerHelper.player.apply {
                    playbackPosition = this.currentPosition
                    currentWindow = this.currentWindowIndex
                    playWhenReady = this.playWhenReady

                }
                exoPlayerHelper.releasePlayer()
                _exoPlayerHelper = null
            }

            @SuppressLint("InlinedApi")
            private fun hideSystemUi() {
                player.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                        or View.SYSTEM_UI_FLAG_FULLSCREEN
                        or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(
                R.layout.recycler_item, parent, false
            )
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.initializePlayer()
            FullScreenPlayerDialog(holder.player, holder.view.context, this)
        }

        override fun getItemCount(): Int {
            return 1
        }
    }

    class FullScreenPlayerDialog(private val exoplayerView: View, context: Context, private val recyclerViewAdapter: PlayerRecyclerViewAdapter) :
        Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {

        val fullScreenIcon =
            exoplayerView.findViewById<ImageButton>(com.google.android.exoplayer2.ui.R.id.exo_fullscreen)

        var isFullScreen = false

        lateinit var parent: ViewGroup

        init {
            fullScreenIcon.setOnClickListener {
                if (isFullScreen) {
                    closeFullScreenDialog()
                } else {
                    openFullScreenDialog()
                }
            }
        }

        override fun onBackPressed() {
            closeFullScreenDialog()
            super.onBackPressed()
        }

        fun openFullScreenDialog() {
            isFullScreen = true
            parent = (exoplayerView.parent as ViewGroup)
            parent.removeView(exoplayerView)
            addContentView(exoplayerView, ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT))
            fullScreenIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    exoplayerView.context,
                    com.google.android.exoplayer2.R.drawable.exo_controls_fullscreen_exit
                )
            )
            show()
        }

        fun closeFullScreenDialog() {
            (exoplayerView.parent as ViewGroup).removeView(exoplayerView)
            parent.addView(exoplayerView)
            isFullScreen = false
            dismiss()
            recyclerViewAdapter.notifyItemChanged(1)
            fullScreenIcon.setImageDrawable(
                ContextCompat.getDrawable(
                    exoplayerView.context,
                    com.google.android.exoplayer2.R.drawable.exo_controls_fullscreen_enter
                )
            )
        }
    }
}