package org.sabda.gpt.utility

import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.TextView
import android.widget.Toast

object ToastUtil {
    fun showToast(ctx: Context?, message: String) {
        if (ctx == null) return // Cegah crash jika context null

        val toast = Toast.makeText(ctx, message, Toast.LENGTH_LONG)

        // Pastikan toast memiliki view sebelum mencoba mengubah tampilannya
        val toastView = toast.view
        if (toastView != null) {
            val textView = toastView.findViewById<TextView>(android.R.id.message)
            textView?.apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
                setBackgroundColor(Color.BLACK)
                setTextColor(Color.WHITE)
                setPadding(32, 16, 32, 16)
                gravity = Gravity.CENTER
            }

            // Pastikan view dapat ditampilkan
            toastView.setBackgroundColor(Color.DKGRAY)
        }

        // Atur posisi toast agar muncul di tengah layar
        toast.setGravity(Gravity.CENTER, 0, 200)
        toast.show()
    }
}