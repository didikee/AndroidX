package com.androidx.storage

import android.app.Activity
import android.app.RecoverableSecurityException
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.androidx.UriUtils

/**
 * user author: didikee
 * create time: 2026/3/30 14:55
 * description: 删除文件
 */
class MediaDeleteHelper {

    companion object {
        private const val DELETE_REQUEST_CODE = 9527
    }

    private var pendingCallback: DeleteCallback? = null
    fun delete(
        activity: Activity,
        uri: Uri,
        onSuccess: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onError: ((Throwable?) -> Unit)? = null
    ) {
        delete(activity, listOf(uri), onSuccess, onCancel, onError)
    }

    fun delete(
        activity: Activity,
        uris: List<Uri>,
        onSuccess: (() -> Unit)? = null,
        onCancel: (() -> Unit)? = null,
        onError: ((Throwable?) -> Unit)? = null
    ) {
        if (uris.isEmpty()) {
            onError?.invoke(IllegalArgumentException("uris empty"))
            return
        }

        pendingCallback = DeleteCallback(onSuccess, onCancel, onError)

        // 🚀 Android 11+：直接走官方批量删除（避免部分删除问题）
        if (isAndroid11()) {
            requestDeletePermission(activity, uris)
            return
        }
        val resolver = activity.contentResolver
        // 🚀 Android 10：尝试直接删除（适用于自己创建的文件）
        try {
            val failedUris = mutableListOf<Uri>()

            uris.forEach {
                //  UriUtils.delete 已经做了try catch
                val delete = UriUtils.delete(resolver, it)
                if (!delete) {
                    failedUris.add(it)
                }
            }

            if (failedUris.isEmpty()) {
                // ✅ 全部删除成功
                pendingCallback?.onSuccess?.invoke()
                pendingCallback = null
            } else {
                // ❗ 有删除失败 → 统一走授权
                handlePermissionRequest(activity, failedUris)
            }

        } catch (e: SecurityException) {
            pendingCallback?.onError?.invoke(e)
            pendingCallback = null
            return
        }

    }

    private fun isAndroid11(): Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
    }

    private fun isAndroid10(): Boolean{
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
    }



    /**
     * Android 11+ / fallback 权限请求
     */
    private fun requestDeletePermission(
        activity: Activity,
        uris: List<Uri>
    ) {
        try {
            val intentSender = MediaStore.createDeleteRequest(
                activity.contentResolver,
                uris
            ).intentSender

            activity.startIntentSenderForResult(
                intentSender,
                DELETE_REQUEST_CODE,
                null,
                0,
                0,
                0
            )
        } catch (e: Exception) {
            pendingCallback?.onError?.invoke(e)
            pendingCallback = null
        }
    }

    /**
     * Android 10 专用权限处理
     */
    private fun handlePermissionRequest(
        activity: Activity,
        uris: List<Uri>,
    ) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {

                // ⚠️ Android 10 只能一个一个请求（系统限制）
                val uri = uris.first()

                try {
                    activity.contentResolver.delete(uri, null, null)
                } catch (e: SecurityException) {
                    if (e is RecoverableSecurityException) {
                        activity.startIntentSenderForResult(
                            e.userAction.actionIntent.intentSender,
                            DELETE_REQUEST_CODE,
                            null,
                            0,
                            0,
                            0
                        )
                        return
                    }
                }
            }

            pendingCallback?.onError?.invoke(
                IllegalStateException("Cannot request delete permission")
            )
            pendingCallback = null

        } catch (e: Exception) {
            pendingCallback?.onError?.invoke(e)
            pendingCallback = null
        }
    }

    /**
     * 👉 必须在 Activity 的 onActivityResult 调用
     */
    fun onActivityResult(
        requestCode: Int,
        resultCode: Int
    ) {
        if (requestCode != DELETE_REQUEST_CODE) return

        val callback = pendingCallback ?: return

        when (resultCode) {
            Activity.RESULT_OK -> {
                callback.onSuccess?.invoke()
            }

            else -> {
                callback.onCancel?.invoke()
            }
        }

        pendingCallback = null
    }

    private data class DeleteCallback(
        val onSuccess: (() -> Unit)?,
        val onCancel: (() -> Unit)?,
        val onError: ((Throwable?) -> Unit)?
    )

}