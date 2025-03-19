package org.sabda.gpt;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements NetworkUtil.NetworkChangeCallback {

    Button alkitabGpt, selengkapnya, situs, aisabda;
    DrawerLayout drawerLayout;
    EditText input;
    ExamplePrompt examplePrompt;
    ImageButton send;
    ImageView drawerImage, dots;
    String randomPrompt;
    TextView charCount;
    boolean isDrawer = false;
    boolean isConnected;
    int MAX_CHAR_COUNT = 250;

    ExpandableListView expandableListView;
    CustomExpandableListAdapter customExpandableListAdapter;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeView();
        initializeExpandableListView();
        initializeListeners();
        initializeNetwork();
        setInitialInput();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(this, callback);

    }

    private void initializeView() {
        input = findViewById(R.id.input);
        charCount = findViewById(R.id.characterCount);
        send = findViewById(R.id.send);
        alkitabGpt = findViewById(R.id.alkitabGPT);
        situs = findViewById(R.id.situsAlkitabGpt);
        aisabda = findViewById(R.id.situsAi);
        selengkapnya = findViewById(R.id.lebihlanjut);
        drawerImage = findViewById(R.id.drawer);
        drawerLayout = findViewById(R.id.drawerLayout);
        dots = findViewById(R.id.dots);
        expandableListView = findViewById(R.id.expandableListView);
    }

    private void initializeExpandableListView() {
        prepareListData();
        customExpandableListAdapter = new CustomExpandableListAdapter(this, listDataHeader, listDataChild);
        expandableListView.setAdapter(customExpandableListAdapter);

        expandableListView.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedChild = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
            Log.d("MainActChild", "initializeExpandableListView: " + selectedChild);
            handleChildClick(selectedChild);

            return true;
        });

        expandableListView.expandGroup(0);
        expandableListView.expandGroup(1);
        expandableListView.expandGroup(2);
    }

    private void initializeListeners() {
        dots.setOnClickListener(this::showPopUp);
        drawerImage.setOnClickListener(this::toggleDrawer);
        input.addTextChangedListener(new InputTextWatcher());
        input.setOnKeyListener(this::handleEnterKey);
        input.setOnEditorActionListener(this::handleEditorAction);
        send.setOnClickListener(this::handleSendButtonClick);
        alkitabGpt.setOnClickListener(v -> handleClick("https://chatgpt.com/g/g-QjHkF2IEk-alkitab-gpt-bible-man"));
        situs.setText(R.string.situs);
        situs.setOnClickListener(v -> handleClick2("https://gpt.sabda.org/", "Situs: Alkitab GPT"));
        aisabda.setOnClickListener(v -> handleClick2("https://ai.sabda.org/", "Situs: AI-Sabda"));
        selengkapnya.setOnClickListener(view -> startActivity(new Intent(this, Selengkapnya.class)));
    }

    private void initializeNetwork() {
        isConnected = NetworkUtil.isNetworkAvailable(this);
        updateConnectionStatus(isConnected);
        if (!isConnected){
            ToastUtil.showToast(this);
        }
        NetworkUtil.registerNetworkChangeReceiver(this, this::updateConnectionStatus);
    }

    private void setInitialInput() {
        examplePrompt = new ExamplePrompt();
        randomPrompt = examplePrompt.getRandomPrompt();
        input.setText(randomPrompt);
        updateCharCount(input.length());
    }

    private void handleChildClick(String selectedChild) {
        switch (selectedChild){
            case "Studi Alkitab":
                Log.d("TAG", "handleChildClicked");
                handleClick2("https://alkitab.sabda.org", "Situs: Studi Alkitab");
                break;
            case "Media Alkitab":
                Log.d("TAG", "handleChildClicked");
                handleClick2("https://sabda.id/badeno/", "Situs: Media Alkitab");
                break;
            case "Alkitab":
                Log.d("TAG", "handleChildClicked");
                openApps("org.sabda.alkitab.action.VIEW", "org.sabda.alkitab");
                break;
            case "AlkiPEDIA":
                Log.d("TAG", "handleChildClicked");
                openApps("org.sabda.pedia.action.VIEW", "org.sabda.pedia");
                break;
            case "Tafsiran":
                Log.d("TAG", "handleChildClicked");
                openApps("org.sabda.tafsiran.action.VIEW", "org.sabda.tafsiran");
                break;
            case "Kamus Alkitab":
                Log.d("TAG", "handleChildClicked");
                openApps("org.sabda.kamus.action.VIEW", "org.sabda.kamus");
                break;
            case "Sabda Bot":
                Log.d("TAG", "handleChildClicked");
                handleClick("https://t.me/sabdabot");
                break;
            case "Lain-lain":
                Log.d("TAG", "handleChildClicked");
                handleClick("https://play.google.com/store/apps/dev?id=4791022353258811724&hl=id");
                break;
            case "AI-4-Church & AI-4-GOD":
                Log.d("TAG", "handleChildClicked");
                handleClick("https://chat.whatsapp.com/EkBAirjrEKK4wa31yqQb6b");
                break;
        }
    }

    private void toggleDrawer(View v) {
        if (isDrawer) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }

    private boolean handleEnterKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
            handleSendInput();
            return true;
        }
        return false;
    }

    private boolean handleEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_SEND) {
            handleSendInput();
            return true;
        }
        return false;
    }

    private void handleSendButtonClick(View v) {
        String inputText = input.getText().toString();
        int length = inputText.length();

        if (length < 5) {
            Toast.makeText(MainActivity.this, "Masukkan prompt dengan panjang minimal 5 karakter", Toast.LENGTH_SHORT).show();
        } else if (length > MAX_CHAR_COUNT) {
            Toast.makeText(MainActivity.this, "Masukkan prompt dengan panjang maksimal 250 karakter", Toast.LENGTH_SHORT).show();
        } else {
            if (isConnected) {
                Intent intent = new Intent(MainActivity.this, AlkitabGPT.class);
                intent.putExtra("inputtext", inputText);
                startActivity(intent);
                input.setText("");
            } else {
                ToastUtil.showToast(this);
            }
        }
    }

    private void handleSendInput() {
        String inputText = input.getText().toString();
        handleClick("https://gpt.sabda.org/chat.php?t=" + inputText);
        input.setText("");
    }

    private void prepareListData() {
        listDataHeader = new ArrayList<>();
        listDataChild = new HashMap<>();

        listDataHeader.add(getString(R.string.situs2));
        listDataHeader.add(getString(R.string.aplikasi_5));
        listDataHeader.add(getString(R.string.komunitas));

        List<String> listGroup1 = Arrays.asList(
                getString(R.string.studi_alkitab),
                getString(R.string.alkitab_media)
        );

        List<String> listGroup2 = Arrays.asList(
                getString(R.string.alkitab),
                getString(R.string.alkipedia),
                getString(R.string.tafsiran),
                getString(R.string.kamus_alkitab),
                getString(R.string.bot),
                getString(R.string.lain)
        );

        List<String> listGroup3 = Arrays.asList(
                getString(R.string.ai4cg)
        );


        listDataChild.put(listDataHeader.get(0), listGroup1);
        listDataChild.put(listDataHeader.get(1), listGroup2);
        listDataChild.put(listDataHeader.get(2), listGroup3);

        Log.d("MainActivity", "ListDataHeader: " + listDataHeader);
        Log.d("MainActivity", "ListDataChild: " + listDataChild.toString());

    }

    private void showPopUp(View v) {
        PopupMenu menu = new PopupMenu(this, v);
        MenuInflater inflater = menu.getMenuInflater();
        inflater.inflate(R.menu.drawer_item_dots, menu.getMenu());
        menu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.AlkitabGPT){
                handleClick("https://chatgpt.com/g/g-QjHkF2IEk-alkitab-gpt-bible-man");
            } else if (itemId == R.id.pelajari){
                startActivity(new Intent(MainActivity.this, Selengkapnya.class));
            } else if (itemId == R.id.tentang){
                startActivity(new Intent(MainActivity.this, Tentang.class));
            } else if (itemId == R.id.question) {
                startActivity(new Intent(MainActivity.this, QuestionAnswer.class));
            } else if (itemId == R.id.realese) {
                showReleaseDialog();
            }

            return true;
        });
        menu.show();

    }

    private void showReleaseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.release_note));
        builder.setMessage(getString(R.string.message_release_notes));
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, R.color.white));
    }

    private void handleClick(String url) {
        if (isConnected) {
            NetworkUtil.openUrl(this, url);
        } else {
            ToastUtil.showToast(this);
        }
    }

    private void handleClick2(String url, String title) {
        if (isConnected) {
            NetworkUtil.openWebView(this, url, title);
        } else {
            ToastUtil.showToast(this);
        }
    }

    private void updateCharCount(int length){
        int remaining = MAX_CHAR_COUNT - length;
        charCount.setText(getString(R.string.cc, remaining));
    }

    private void updateConnectionStatus(boolean isConnected){
        this.isConnected = isConnected;
        send.setEnabled(isConnected);
        alkitabGpt.setEnabled(isConnected);
        situs.setEnabled(isConnected);

        if (!isConnected){
            ToastUtil.showToast(this);
        }
    }

    private void openApps(String destination, String packageName) {
        Log.d("TAG", "openApps:" + packageName);
        if (isAppInstalled(packageName)){
            Log.d("TAG", packageName + "installed");
            Intent intent = new Intent(destination);
            //intent.setPackage(packageName);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                redirectToPlayStore(packageName);
            }
        } else {
            Log.d("TAG", packageName + " not installed");
            redirectToPlayStore(packageName);
        }
    }

    private void redirectToPlayStore(String packageName) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
    }

    private boolean isAppInstalled(String packageName) {
        PackageManager pm = getPackageManager();
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);

        //clearDrawerSelection();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NetworkUtil.unregisterNetworkChangeReceiver(this);
    }

    private class InputTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int length = s.length();
            updateCharCount(length);

            if (length > MAX_CHAR_COUNT) {
                input.setText(s.subSequence(0, MAX_CHAR_COUNT));
                input.setSelection(MAX_CHAR_COUNT);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {}
    }

    @Override
    public void onNetworkChange(boolean isConnected) {
        updateConnectionStatus(isConnected);
    }
}