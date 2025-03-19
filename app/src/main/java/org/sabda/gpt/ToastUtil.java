package org.sabda.gpt;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

public class ToastUtil {

    public static void showToast(Context ctx) {
        Toast toast = new Toast(ctx);

        TextView textView = new TextView(ctx);
        textView.setText(R.string.toast_offline);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        textView.setBackgroundColor(Color.BLACK);
        textView.setTextColor(Color.WHITE);
        textView.setPadding(16, 8, 16, 8);
        textView.setGravity(Gravity.CENTER);
        toast.setView(textView);

        toast.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL, 0 , 0);

        toast.setDuration(Toast.LENGTH_LONG);

        toast.show();
    }
}
