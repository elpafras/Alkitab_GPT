package org.sabda.gpt

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.ToastUtil

class YoutubePlayer : AppCompatActivity(), NetworkChangeCallback {
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_youtube_player)

        isConnected = NetworkUtil.isNetworkAvailable(this)
        updateConnectionStatus(isConnected)
        NetworkUtil.registerNetworkChangeReceiver(
            this
        ) { isConnected: Boolean -> this.updateConnectionStatus(isConnected) }

        val videoId = intent.getStringExtra(EXTRA_VIDEO_ID)
        val startTime = intent.getIntExtra(EXTRA_START_TIME, 0)

        setupYoutubePlayer(videoId!!, startTime)
        val close = findViewById<ImageButton>(R.id.close)
        close.setOnClickListener { finish() }
    }

    private fun setupYoutubePlayer(videoId: String, startTime: Int) {
        val youTubePlayerView = findViewById<YouTubePlayerView>(R.id.player_view)
        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                super.onReady(youTubePlayer)
                youTubePlayer.loadVideo(videoId, startTime.toFloat())
            }
        })
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        if (!isConnected) {
            ToastUtil.showToast(this,"")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtil.unregisterNetworkChangeReceiver(this)
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }

    companion object {
        var EXTRA_VIDEO_ID: String = "EXTRA_VIDEO_ID"
        var EXTRA_START_TIME: String = "EXTRA_START_TIME"
    }
}