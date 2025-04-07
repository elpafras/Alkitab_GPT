package org.sabda.gpt

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.ViewGroup
import android.view.Window
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.github.chrisbanes.photoview.PhotoView
import org.sabda.gpt.databinding.ActivitySelengkapnyaBinding
import org.sabda.gpt.utility.NetworkUtil
import org.sabda.gpt.utility.NetworkUtil.NetworkChangeCallback
import org.sabda.gpt.utility.showToast
import java.util.Objects

class Selengkapnya : AppCompatActivity(), NetworkChangeCallback {

    private lateinit var binding: ActivitySelengkapnyaBinding
    private var isConnected: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelengkapnyaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isConnected = NetworkUtil.isNetworkAvailable(this)
        NetworkUtil.registerNetworkChangeReceiver(this) {isConnected ->
            this.updateConnectionStatus(isConnected)
        }

        updateConnectionStatus(isConnected)
        setupListener()
    }

    private fun setupListener() {
        binding.kembali.setOnClickListener { finish() }

        val imageViewMap = mapOf(
            binding.aidanAlkitab to R.drawable.aidanalkitab,
            binding.AISquareSquare to R.drawable.aisquare,
            binding.FOKUS to R.drawable.fokus,
            binding.AlkitabGPT to R.drawable.alkitabgptdetail,
            binding.bibleGpt to R.drawable.biblegpt
        )

        imageViewMap.forEach { (imageView, imageResId) ->
            imageView.setImageResource(imageResId)
        }

        // Image click listeners
        val imageButtonMap = mapOf(
            binding.aidanAlkitab to R.drawable.aidanalkitab,
            binding.AISquareSquare to R.drawable.aisquare,
            binding.FOKUS to R.drawable.fokus,
            binding.AlkitabGPT to R.drawable.alkitabgptdetail,
            binding.bibleGpt to R.drawable.biblegpt
        )

        imageButtonMap.forEach { (button, imageResId) ->
            button.setOnClickListener { showImage(imageResId) }
        }

        // Button click listeners
        val webButtonMap = mapOf(
            binding.aidanAlkitabButton to Pair("ai-dan-alkitab/", "Seminar: AI dan Alkitab"),
            binding.aisquaredbutton to Pair("pa-dengan-ai-cara-ai-squared/", "Metode AI Squared"),
            binding.fokusbutton to Pair("bagaimana-ai/", "Metode F.0.K.U.S"),
            binding.alkitabGPTbutton to Pair("alkitab-gpt/", "Seminar: Alkitab GPT")
        )

        webButtonMap.forEach { (button, data) ->
            button.setOnClickListener { handleClick("https://ai.sabda.org/${data.first}", data.second) }
        }

        binding.cobabutton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
        }

        // Video buttons
        val videoButtonMap = mapOf(
            binding.videoaidanAlkitab to Pair("gDLTyyabCvI", 218),
            binding.videoAlkitabGPT to Pair("Pf9DPEcX8JA", 797),
            binding.videoaiSquared to Pair("qHPwPjuAEs4", 783),
            binding.videofokus to Pair("GHcRwNXdnvQ", 0),
            binding.videoBibleGpt to Pair("vZkGKgtRPeU", 0)
        )

        videoButtonMap.forEach { (button, data) ->
            button.setOnClickListener { handleYoutubeClick(data.first, data.second) }
        }

        // PDF buttons
        val pdfButtonMap = mapOf(
            binding.pdfaidanAlkitab to "aDfLeASoUQYFx0",
            binding.pdfAlkitabGPT to "F09kT2IosMpVwf",
            binding.pdfaiSquared to "MufsoIHKBIlR6V",
            binding.pdffokus to "7GNdvGTo9M4Fc9",
            binding.pdfBibleGpt to "j402m0w1jgBb9m"
        )

//        pdfButtonMap.forEach { (button, url) ->
//            button.setOnClickListener { handlePdfClick("https://www.slideshare.net/slideshow/embed_code/key/${url}") }
//        }
    }

    private fun showImage(imageResId: Int) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.image_dialog)
        Objects.requireNonNull(dialog.window)
            ?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        val photoView = dialog.findViewById<PhotoView>(R.id.photoView)
        photoView.setImageResource(imageResId)

        val closeButton = dialog.findViewById<ImageButton>(R.id.close)
        closeButton.setOnClickListener { dialog.dismiss() }

        dialog.setCancelable(true)
        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.decorView?.rootView?.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    private fun handleClick(url: String, title: String) {
        if (isConnected) NetworkUtil.openWebView(this, url, title) else applicationContext.showToast(getString(R.string.toast_offline))
    }

    private fun handleYoutubeClick(videoId: String, startTime: Int) {
        if (isConnected) showYoutube(videoId, startTime) else applicationContext.showToast(getString(R.string.toast_offline))
    }

    private fun showYoutube(videoId: String, startTime: Int) {
        val intent = Intent(this, YoutubePlayer::class.java)
        intent.putExtra(YoutubePlayer.EXTRA_VIDEO_ID, videoId)
        intent.putExtra(YoutubePlayer.EXTRA_START_TIME, startTime)
        startActivity(intent)
    }

//    private fun handlePdfClick(pdfUrl: String) {
//        if (isConnected) {
//            showPdf(pdfUrl)
//        } else {
//            ToastUtil.showToast(this)
//        }
//    }

//    private fun showPdf(pdfUrl: String) {
//        val intent = Intent(this, Materi::class.java)
//        intent.putExtra(Materi.EXTRA_URL, pdfUrl)
//        startActivity(intent)
//    }

    override fun onDestroy() {
        super.onDestroy()
        NetworkUtil.unregisterNetworkChangeReceiver(this)
    }

    private fun updateConnectionStatus(isConnected: Boolean) {
        this.isConnected = isConnected

        val buttons = listOf(
            binding.aidanAlkitabButton,
            binding.aisquaredbutton,
            binding.fokusbutton,
            binding.alkitabGPTbutton,
            binding.videoaidanAlkitab,
            binding.videoAlkitabGPT,
            binding.videoaiSquared,
            binding.videofokus,
            binding.videoBibleGpt,
            binding.pdfaidanAlkitab,
            binding.pdfAlkitabGPT,
            binding.pdfaiSquared,
            binding.pdffokus,
            binding.pdfBibleGpt
        )

        buttons.forEach { it.isEnabled = isConnected }

        if (!isConnected) applicationContext.showToast(getString(R.string.toast_offline))
    }

    override fun onNetworkChange(isConnected: Boolean) {
        updateConnectionStatus(isConnected)
    }
}
