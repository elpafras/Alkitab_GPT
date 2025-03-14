package org.sabda.gpt;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class QuestionAnswer extends AppCompatActivity {

    ExpandableListView exp;
    CustomExpandableListAdapter customExpandableListAdapter;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    TextView title;
    ImageView out;
    boolean isConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question_answer);

        initViews();
        setupTitle();
        initializeExpandListView();
        initializeNetwork();

        out.setOnClickListener(v -> finish());

    }

    private void initializeNetwork() {
        isConnected = NetworkUtil.isNetworkAvailable(this);
        updateConnectionStatus(isConnected);
        if (!isConnected){
            ToastUtil.showToast(this);
        }
        NetworkUtil.registerNetworkChangeReceiver(this, this::updateConnectionStatus);
    }

    private void updateConnectionStatus(boolean isConnected) {
        this.isConnected = isConnected;

        if (!isConnected){
            ToastUtil.showToast(this);
        }
    }

    private void initViews() {
        exp = findViewById(R.id.questionListView);
        title = findViewById(R.id.title);
        out = findViewById(R.id.out);
    }

    private void setupTitle() {
        title.setText(R.string.question_and_answer);
    }

    private void initializeExpandListView() {
        prepareListData();
        customExpandableListAdapter = new CustomExpandableListAdapter(this, listDataHeader, listDataChild);
        exp.setAdapter(customExpandableListAdapter);

        for (int i = 0; i < customExpandableListAdapter.getGroupCount(); i++) {
            exp.expandGroup(i);
        }

        exp.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            String selectedChild = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition);
            Log.d(TAG, "SelectedChild: " + selectedChild);
            return true;
        });
    }

    private void prepareListData() {
        listDataHeader = createHeaders();
        listDataChild = createChildren(listDataHeader);

        for (String header : listDataHeader) {
            List<String> answer = listDataChild.get(header);
            for (int i = 0; i < answer.size(); i++) {
                answer.set(i, getLinkifiedText(answer.get(i)).toString());
            }
        }
    }

    private SpannableString getLinkifiedText(String text) {
        SpannableString spannableString = new SpannableString(text);

        Pattern urlPattern      = Pattern.compile("http[s]?://\\S+");
        Pattern emailPattern    = Pattern.compile("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}\\b");
        Pattern phonePattern    = Pattern.compile("\\d{11}");

        Matcher urlMatcher      = urlPattern.matcher(spannableString);
        Matcher emailMatcher    = emailPattern.matcher(spannableString);
        Matcher phoneMatcher    = phonePattern.matcher(spannableString);

        int linkColor = isNightMode() ? ContextCompat.getColor(this, R.color.blue) : ContextCompat.getColor(this, R.color.darkblue);

        applyPattern(spannableString, urlMatcher, linkColor, url -> {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(browserIntent);
        });

        applyPattern(spannableString, emailMatcher, linkColor, email -> {
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + email));
            startActivity(emailIntent);
        });

        applyPattern(spannableString, phoneMatcher, linkColor, phone -> {
            Intent whatsappIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://api.whatsapp.com/send?phone=62" + phone + "&text=Halo.%20Saya%20ingin%20bertanya%20terkait%20SABDA."));
            startActivity(whatsappIntent);
        });

        return spannableString;
    }

    private void applyPattern(SpannableString string, Matcher matcher, int linkColor, LinkAction linkAction){
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            String matchedText = matcher.group();

            string.setSpan(new ClickableSpan() {

                @Override
                public void onClick(@NonNull View widget) {
                    linkAction.onLinkClicked(matchedText);
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(false);
                }
            }, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            string.setSpan(new ForegroundColorSpan(linkColor), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private boolean isNightMode() {
        int nightModeFlags = getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags == android.content.res.Configuration.UI_MODE_NIGHT_YES;
    }

    private interface LinkAction {
        void onLinkClicked(String url);
    }

    private List<String> createHeaders() {
        int[] questions = {
            R.string.question1, R.string.question2, R.string.question3,
            R.string.question4, R.string.question5, R.string.question6,
            R.string.question7, R.string.question8, R.string.question9,
            R.string.question10, R.string.question11, R.string.question12,
            R.string.question13, R.string.question14, R.string.question15
        };

        List<String> headers = new ArrayList<>();
        for (int questionResId : questions){
            headers.add(getString(questionResId));
        }

        return headers;
    }

    private HashMap<String, List<String>> createChildren(List<String> headers) {
        int[] answers = {
                R.string.answer1, R.string.answer2, R.string.answer3, R.string.answer4,
                R.string.answer5, R.string.answer6, R.string.answer7, R.string.answer8,
                R.string.answer9, R.string.answer10, R.string.answer11, R.string.answer12,
                R.string.answer13, R.string.answer14, R.string.answer15
        };

        HashMap<String, List<String>> children = new HashMap<>();
        for (int i = 0; i < headers.size(); i++) {
            children.put(headers.get(i), createChildList(answers[i]));
        }

        return children;
    }

    private List<String> createChildList(int answerResId) {
        List<String> childList = new ArrayList<>();
        childList.add(getString(answerResId));
        return childList;
    }

}