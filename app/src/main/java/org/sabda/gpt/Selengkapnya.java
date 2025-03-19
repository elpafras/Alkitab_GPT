package org.sabda.gpt;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.chrisbanes.photoview.PhotoView;

import java.util.Objects;

public class Selengkapnya extends AppCompatActivity implements NetworkUtil.NetworkChangeCallback  {

    Button aidan, aisquared, fokus, coba, alkitabGPT;
    PhotoView Aidan, square, fokus1, AlkitabGPT, bibleGPT;
    Button v_aidan, p_aidan, v_alkitab, p_alkitab, v_aiSquared, p_aiSquared, v_fokus, p_fokus, v_bibleGpt, p_bibleGpt;
    ImageView back;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selengkapnya);

        isConnected = NetworkUtil.isNetworkAvailable(this);
        NetworkUtil.registerNetworkChangeReceiver(this, this::updateConnectionStatus);

        initializeViews();
        updateConnectionStatus(isConnected);

        back.setOnClickListener(v -> finish());
        setImageResources();
        ClickListener();

    }

    private void initializeViews() {
        back = findViewById(R.id.kembali);

        /*inisialisasi button navigasi*/
        aidan = findViewById(R.id.aidanAlkitabButton);
        aisquared = findViewById(R.id.aisquaredbutton);
        fokus = findViewById(R.id.fokusbutton);
        alkitabGPT = findViewById(R.id.alkitabGPTbutton);
        coba = findViewById(R.id.cobabutton);

        /*inisialisasi button image*/
        Aidan = findViewById(R.id.aidanAlkitab);
        square = findViewById(R.id.AI_SquareSquare);
        fokus1 = findViewById(R.id.FOKUS);
        AlkitabGPT = findViewById(R.id.AlkitabGPT);
        bibleGPT = findViewById(R.id.bibleGpt);

        /*inisialisasi button video & pdf*/
        v_aidan = findViewById(R.id.videoaidanAlkitab);
        v_alkitab = findViewById(R.id.videoAlkitabGPT);
        v_aiSquared = findViewById(R.id.videoaiSquared);
        v_fokus = findViewById(R.id.videofokus);
        v_bibleGpt = findViewById(R.id.videoBibleGpt);

        p_aidan = findViewById(R.id.pdfaidanAlkitab);
        p_alkitab = findViewById(R.id.pdfAlkitabGPT);
        p_aiSquared = findViewById(R.id.pdfaiSquared);
        p_fokus = findViewById(R.id.pdffokus);
        p_bibleGpt = findViewById(R.id.pdfBibleGpt);
    }

    private void setImageResources() {
        Aidan.setImageResource(R.drawable.aidanalkitab);
        square.setImageResource(R.drawable.aisquare);
        fokus1.setImageResource(R.drawable.fokus);
        AlkitabGPT.setImageResource(R.drawable.alkitabgptdetail);
        bibleGPT.setImageResource(R.drawable.biblegpt);
    }

    private void ClickListener() {
        Aidan.setOnClickListener(v -> showImage(R.drawable.aidanalkitab));
        square.setOnClickListener(v -> showImage(R.drawable.aisquare));
        fokus1.setOnClickListener(v -> showImage(R.drawable.fokus));
        AlkitabGPT.setOnClickListener(v -> showImage(R.drawable.alkitabgptdetail));
        bibleGPT.setOnClickListener(v -> showImage(R.drawable.biblegpt));

        aidan.setOnClickListener(v -> handleClick("https://ai.sabda.org/ai-dan-alkitab/", "Seminar: AI dan Alkitab"));
        alkitabGPT.setOnClickListener(v -> handleClick("https://ai.sabda.org/alkitab-gpt/", "Seminar: Alkitab GPT"));
        aisquared.setOnClickListener(v -> handleClick("https://ai.sabda.org/pa-dengan-ai-cara-ai-squared/", "Metode AI Squared"));
        fokus.setOnClickListener(v -> handleClick("https://ai.sabda.org/bagaimana-ai/", "Metode F.0.K.U.S"));

        v_aidan.setOnClickListener(v -> handleYoutubeClick("gDLTyyabCvI", 218));
        v_alkitab.setOnClickListener(v -> handleYoutubeClick("Pf9DPEcX8JA", 797));
        v_aiSquared.setOnClickListener(v -> handleYoutubeClick("qHPwPjuAEs4", 783));
        v_fokus.setOnClickListener(v -> handleYoutubeClick("GHcRwNXdnvQ", 0));
        v_bibleGpt.setOnClickListener(v -> handleYoutubeClick("vZkGKgtRPeU", 0));

        p_aidan.setOnClickListener(v -> handlePdfClick("https://www.slideshare.net/slideshow/embed_code/key/aDfLeASoUQYFx0"));
        p_alkitab.setOnClickListener(v -> handlePdfClick("https://www.slideshare.net/slideshow/embed_code/key/F09kT2IosMpVwf"));
        p_aiSquared.setOnClickListener(v -> handlePdfClick("https://www.slideshare.net/slideshow/embed_code/key/MufsoIHKBIlR6V"));
        p_fokus.setOnClickListener(v -> handlePdfClick("https://www.slideshare.net/slideshow/embed_code/key/7GNdvGTo9M4Fc9"));
        p_bibleGpt.setOnClickListener(v -> handlePdfClick("https://www.slideshare.net/slideshow/embed_code/key/j402m0w1jgBb9m"));

        coba.setOnClickListener(view -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    private void showImage(int imageResId) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.image_dialog);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        PhotoView photoView = dialog.findViewById(R.id.photoView);
        photoView.setImageResource(imageResId);

        ImageButton closeButton = dialog.findViewById(R.id.close);
        closeButton.setOnClickListener(v -> dialog.dismiss());

        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        dialog.getWindow().getDecorView().getRootView().setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void handleClick(String url, String title){
        if (isConnected){
            NetworkUtil.openWebView(this, url, title);
        } else {
            ToastUtil.showToast(this);
        }
    }

    private void handleYoutubeClick(String videoId, int startTime){
        if (isConnected){
            showYoutube(videoId, startTime);
        } else {
            ToastUtil.showToast(this);
        }
    }

    private void showYoutube(String videoId, int startTime) {
        Intent intent = new Intent(this, YoutubePlayer.class);
        intent.putExtra(YoutubePlayer.EXTRA_VIDEO_ID, videoId);
        intent.putExtra(YoutubePlayer.EXTRA_START_TIME, startTime);
        startActivity(intent);
    }

    private void handlePdfClick(String pdfUrl){
        if (isConnected){
            showPdf(pdfUrl);
        } else {
            ToastUtil.showToast(this);
        }
    }

    private void showPdf(String pdfUrl) {
        Intent intent = new Intent(this, materi.class);
        intent.putExtra(materi.EXTRA_URL, pdfUrl);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtil.unregisterNetworkChangeReceiver(this);
    }

    private void updateConnectionStatus(boolean isConnected) {
        this.isConnected = isConnected;

        Button[] buttons = {aidan, aisquared, fokus, alkitabGPT, v_aidan, v_alkitab, v_aiSquared, v_fokus, v_bibleGpt, p_aidan, p_alkitab, p_aiSquared, p_fokus, p_bibleGpt};

        for (Button button : buttons) {
            button.setEnabled(isConnected);
        }

        if (!isConnected){
            ToastUtil.showToast(this);
        }

    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        updateConnectionStatus(isConnected);
    }

}
