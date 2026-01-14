package com.androidx.share

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.androidx.LogUtils
import com.androidx.utils.UriUtils

/**
 * user author: didikee
 * create time: 2019-12-02 14:23
 * description:
 */
object ShareUtils {
    private const val TAG = "ShareUtils"

    @JvmStatic
    fun share(
        context: Context,
        uri: Uri,
        mimeType: String?,
        options: ShareOptions = ShareOptions()
    ) {
        shareUris(context, arrayListOf(uri), mimeType, options)
    }

    @JvmStatic
    fun shareUris(
        context: Context,
        uris: ArrayList<Uri>,
        mimeType: String?,
        options: ShareOptions = ShareOptions()
    ) {
        if (uris.isEmpty()) {
            LogUtils.e("$TAG: uri list is empty")
            return
        }

        // 如果用户传入的 mimeType 为空或空字符串，就尝试自动获取
        val resolvedMime = checkMimetype(context, uris.first(), mimeType)
        if (resolvedMime.isEmpty()) {
            LogUtils.e("$TAG: mimeType is empty or cannot be resolved")
            return
        }

        // Android Q 及以下检查指定包是否安装
        if (!options.packageName.isNullOrEmpty()
            && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
            && !isAppInstalled(context, options.packageName)
        ) {
            LogUtils.w("$TAG: target package not installed: ${options.packageName}")
            return
        }

        val intent = buildShareIntent(resolvedMime, uris, options)
        context.startActivity(Intent.createChooser(intent, options.title.orEmpty()))
    }


    fun buildShareIntent(
        mimeType: String,
        uris: ArrayList<Uri>,
        options: ShareOptions = ShareOptions(),
    ): Intent {
        val intent = Intent(
            if (uris.size > 1) Intent.ACTION_SEND_MULTIPLE else Intent.ACTION_SEND
        )

        if (!options.packageName.isNullOrEmpty()) {
            if (options.className.isNullOrEmpty()) {
                intent.setPackage(options.packageName)
            } else {
                intent.component = ComponentName(options.packageName, options.className)
            }
        }

        intent.type = mimeType

        if (uris.size == 1) {
            val uri = uris.first()
            intent.putExtra(Intent.EXTRA_STREAM, uri)
            intent.setDataAndType(uri, mimeType)
        } else {
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
        }

        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        return intent
    }

    private fun checkMimetype(context: Context, uri: Uri, mimeType: String?): String {
        return if (mimeType.isNullOrBlank()) {
            val autoMime = UriUtils.getMimeType(context, uri)
            if (autoMime.isNullOrBlank()) {
                ""
            } else autoMime
        } else {
            mimeType
        }
    }


    /**
     * 该api已经无法在android R上工作了，如果必须使用请申请 query all package 权限
     * 参考文档：https://developer.android.google.cn/training/basics/intents/package-visibility
     *
     * @param context
     * @param packageName
     * @return
     */
    @Deprecated("")
    private fun isAppInstalled(context: Context, pkg: String): Boolean =
        try {
            context.packageManager.getLaunchIntentForPackage(pkg) != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }

}