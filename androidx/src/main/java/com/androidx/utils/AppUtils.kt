package com.androidx.utils

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.webkit.URLUtil
import com.androidx.media.MimeType

/**
 * user author: didikee
 * create time: 2025/4/22 下午12:30
 * description:
 */
object AppUtils {
    fun getVersionDisplay(context: Context?): String {
        context?.let {
            val versionName = getVersionName(it)
            val versionCode = getVersionCode(it)
            return "${versionName}(${versionCode})"
        }
        return ""
    }

    fun getVersionName(context: Context?): String {
        if (context == null) {
            return ""
        }
        val packageManager = context.packageManager
        try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return ""
    }

    fun getVersionCode(context: Context?): Long {
        if (context == null) {
            return -1
        }
        val packageManager = context.packageManager
        try {
            val packageInfo = packageManager.getPackageInfo(context.packageName, 0)
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode
            } else {
                packageInfo.versionCode.toLong()
            }
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }
        return -1
    }

    fun openUrlInBrowser(context: Context, url: String) {
        if (URLUtil.isNetworkUrl(url)) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(Intent.createChooser(intent, ""))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

    }

    fun getGooglePlayLink(pkgName: String): String {
        return "https://play.google.com/store/apps/details?id=$pkgName"
    }

    // 使用选择器的话，荣耀市场不让过审
    fun reviewAPP(context: Context, pkgName: String, userChoose: Boolean = false) {
        try {
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("market://details?id=$pkgName")
            )
            if (userChoose) {
                context.startActivity(
                    Intent.createChooser(intent, "")
                )
            } else {
                context.startActivity(intent)
            }

        } catch (anfe: ActivityNotFoundException) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getGooglePlayLink(pkgName))))
        }
    }

    fun shareText(context: Context, title: String?, content: String) {
        try {
            val intent = Intent(Intent.ACTION_SEND)
            intent.type = "text/plain"
            intent.putExtra(Intent.EXTRA_TEXT, content)
            context.startActivity(Intent.createChooser(intent, title))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun openImage(context: Context?, imageUri: Uri?, title: String?) {
        if (context == null || imageUri == null) {
            return
        }
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.type = "image/*"
            intent.setDataAndType(imageUri, "image/*")
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(intent, title ?: "")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * 调用第三方的视频播放器播放视频
     * 打开任意类的文件请使用
     *
     * @param activity
     * @param videoUri
     * @return
     */
    fun openVideo(activity: Activity?, videoUri: Uri?): Boolean {
        if (activity == null || videoUri == null) {
            return false
        }
        val videoIntent = Intent(Intent.ACTION_VIEW)
        videoIntent.setDataAndType(videoUri, "video/*")
        videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        try {
            activity.startActivity(Intent.createChooser(videoIntent, ""))
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun openPdf(context: Context?, pdfUri: Uri?, title: String?) {
        if (context == null || pdfUri == null) {
            return
        }
        val mimetype = MimeType.PDF
        try {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.type = mimetype
            intent.setDataAndType(pdfUri, mimetype)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val chooser = Intent.createChooser(intent, title ?: "")
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendEmail(context: Context, emailAddress: String, title: String?, text: String?): Boolean {
        // 必须明确使用mailto前缀来修饰邮件地址,如果使用
        //  intent.putExtra(Intent.EXTRA_EMAIL, email)，结果将匹配不到任何应用
        val uri = Uri.parse("mailto:$emailAddress")
        val emailArray = arrayOf(emailAddress)
        val intent = Intent(Intent.ACTION_SENDTO, uri)
        intent.putExtra(Intent.EXTRA_CC, emailArray) // 抄送人
        intent.putExtra(Intent.EXTRA_SUBJECT, title)
        intent.putExtra(Intent.EXTRA_TEXT, text)
        try {
            context.startActivity(
                Intent.createChooser(
                    intent,
                    ""
                )
            )
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return false
    }
}