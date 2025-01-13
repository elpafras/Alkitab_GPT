package org.sabda.gpt.utility

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import org.sabda.gpt.R

object ToastUtil {
    fun showToast(ctx: Context?) {
        val toast = Toast(ctx)

        val textView = TextView(ctx)
        textView.setText(R.string.toast_offline)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
        textView.setBackgroundColor(Color.BLACK)
        textView.setTextColor(Color.WHITE)
        textView.setPadding(16, 8, 16, 8)
        textView.gravity = Gravity.CENTER
        toast.view = textView

        toast.setGravity(Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL, 0, 0)

        toast.duration = Toast.LENGTH_LONG

        toast.show()
    }
}
