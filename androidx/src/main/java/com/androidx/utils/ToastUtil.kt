package com.androidx.utils

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast

/**
 * user author: didikee
 * create time: 2025/6/9 下午1:43
 * description: toast util
 */
object ToastUtil {
    private val mainHandler = Handler(Looper.getMainLooper())

    fun show(context: Context?, message: String?, longToast: Boolean = false) {
        show(context, message, if (longToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT)
    }

    fun show(context: Context?, message: String?, duration: Int) {
        if (message.isNullOrBlank()) return

        context?.let {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Toast.makeText(it.applicationContext, message, duration).show()
            } else {
                mainHandler.post {
                    Toast.makeText(it.applicationContext, message, duration).show()
                }
            }
        }
    }
}