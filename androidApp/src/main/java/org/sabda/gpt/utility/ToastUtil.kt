package org.sabda.gpt.utility

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast

fun Context.showToast(message: String) {
    val toast = Toast.makeText(this, message, Toast.LENGTH_LONG)

    toast.view?.let { toastView ->
        toastView.setBackgroundColor(Color.DKGRAY)

        toastView.findViewById<TextView>(android.R.id.message)?.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            setBackgroundColor(Color.BLACK)
            setTextColor(Color.WHITE)
            setPadding(32, 16, 32, 16)
            gravity = Gravity.CENTER
        }
    }

    toast.setGravity(Gravity.CENTER, 0, 200)
    toast.show()
}