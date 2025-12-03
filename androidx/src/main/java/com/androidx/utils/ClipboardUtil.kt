package com.androidx.utils

import android.content.ClipData
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils

/**
 * Created by didikee on 16/05/2017.
 */
object ClipboardUtil {
    fun putString(context: Context?, text: String?) {
        var text = text
        if (context == null) return

        if (TextUtils.isEmpty(text)) {
            text = ""
            //会直接把最近的剪贴板置空
        }
        val clipboardManager = getClipboardManager(context)
        clipboardManager!!.setPrimaryClip(ClipData.newPlainText("Text", text))
    }

    fun getClipboardManager(context: Context?): ClipboardManager? {
        return if (context == null) null else context.getSystemService(
            Context
                .CLIPBOARD_SERVICE
        ) as ClipboardManager
    }

    /**
     * 获取剪贴板中第一条String
     *
     * @return
     */
    fun getTopClipText(context: Context?): String {
        if (context == null) return ""

        val clipboardManager = getClipboardManager(context)
        if (!clipboardManager!!.hasPrimaryClip()) {
            return ""
        }
        val data = clipboardManager.primaryClip
        if (data != null && clipboardManager.primaryClipDescription!!.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
            return data.getItemAt(0).text.toString()
        }
        return ""
    }
}
