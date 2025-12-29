package com.androidx.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.text.TextUtils
import com.androidx.LogUtils
import com.androidx.media.MimeType

/**
 * user author: didikee
 * create time: 2025/12/2 16:29
 * description:
 */
object IntentUtils {

    fun appSettingsIntent(packageName: String): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            setData(Uri.fromParts("package", packageName, null))
        }
    }

    fun pickVideo(activity: Activity, requestCode: Int, multiSelect: Boolean = false) {
        pickMedia(activity, "video/*", requestCode, multiSelect)
    }

    fun pickImage(activity: Activity, requestCode: Int, multiSelect: Boolean = false) {
        pickMedia(activity, "image/*", requestCode, multiSelect)
    }

    fun pickAudio(activity: Activity, requestCode: Int, multiSelect: Boolean = false) {
        pickMedia(activity, "audio/*", requestCode, multiSelect)
    }

    fun pickMedia(
        activity: Activity,
        mimeType: String,
        requestCode: Int,
        multiSelect: Boolean = false
    ) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            setType(mimeType)
            if (multiSelect) {
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // 允许选择多个文件
            }
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        activity.startActivityForResult(intent, requestCode)
    }

    fun openAudio(activity: Activity?, uri: Uri?): Boolean {
        return openWith(activity, uri, "audio/*", "")
    }

    fun openImage(activity: Activity?, uri: Uri?): Boolean {
        return openWith(activity, uri, MimeType.IMAGE, "")
    }

    fun openGif(activity: Activity?, uri: Uri?): Boolean {
        return openWith(activity, uri, MimeType.GIF, "")
    }

    fun openVideo(activity: Activity?, uri: Uri?): Boolean {
        return openWith(activity, uri, MimeType.VIDEO, "")
    }

    @JvmOverloads
    fun openWith(activity: Activity?, uri: Uri?, mimeType: String?, title: String? = ""): Boolean {
        var mimeType = mimeType
        if (activity == null || uri == null) {
            return false
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "*/*"
        }
        val videoIntent = Intent(Intent.ACTION_VIEW)
        videoIntent.setDataAndType(uri, mimeType)
        videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            activity.startActivity(Intent.createChooser(videoIntent, title))
            return true
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun navigateToMain(activity: Activity,mainActivityClz :Class<out Activity>?) {
        mainActivityClz?.let {
            val intent = Intent(activity, mainActivityClz)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
            activity.startActivity(intent)
        } ?: run {
            // 未注册
            LogUtils.e("navigateToMain MainActivity unregister")
        }
    }
}