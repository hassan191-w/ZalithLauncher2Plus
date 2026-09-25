package com.movtery.zalithlauncher.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.movtery.zalithlauncher.R

class SplashActivity : AppCompatActivity() {

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var logoLayout: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var transitioned = false

    private val goMainRunnable = Runnable { goToMain() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        playerView = findViewById(R.id.splash_player_view)
        logoLayout = findViewById(R.id.splash_logo_layout)

        val prefs = getSharedPreferences("minelauncher", MODE_PRIVATE)
        val videoUri = prefs.getString("splash_video_uri", null)

        if (videoUri.isNullOrEmpty()) {
            playerView?.visibility = View.GONE
            logoLayout?.visibility = View.VISIBLE
            handler.postDelayed(goMainRunnable, 1500)
        } else {
            logoLayout?.visibility = View.GONE
            playVideo(Uri.parse(videoUri))
        }
    }

    private fun playVideo(uri: Uri) {
        try {
            playerView?.visibility = View.VISIBLE
            player = ExoPlayer.Builder(this).build().apply {
                setMediaItem(MediaItem.fromUri(uri))
                volume = 0f
                repeatMode = Player.REPEAT_MODE_OFF
                playWhenReady = true
                prepare()
            }
            playerView?.player = player

            player?.addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) goToMain()
                }

                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    goToMain()
                }
            })

            handler.postDelayed(goMainRunnable, 10000)

        } catch (t: Throwable) {
            goToMain()
        }
    }

    private fun goToMain() {
        if (transitioned) return
        transitioned = true
        handler.removeCallbacks(goMainRunnable)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(goMainRunnable)
        player?.release()
        player = null
    }
}
