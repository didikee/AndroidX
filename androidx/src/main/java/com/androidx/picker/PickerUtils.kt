package com.androidx.picker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.androidx.AndroidStorage
import com.androidx.LogUtils
import java.util.Locale


/**
 * user author: didikee
 * create time: 2026/3/13 15:36
 * description:
 */
object PickerUtils {
    fun isMotionExtension(displayName: String): Boolean {
        val lowerCase = displayName.lowercase(Locale.getDefault())
        return lowerCase.endsWith("jpeg") || lowerCase.endsWith("jpg") || lowerCase.endsWith("heic")
                || lowerCase.endsWith("avif")
    }

    fun isMotionMimeType(mimeType: String): Boolean {
        return mimeType.equals("image/jpeg", true)
                || mimeType.equals("image/heic", true)
                || mimeType.equals("image/avif", true)
    }

    /**
     * 检查是否有读取媒体的权限
     * @param context Context
     * @param contentUri 媒体类型 URI
     * @return true 表示有权限，false 表示无权限
     */
    fun hasMediaPermission(context: Context, contentUri: Uri?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true
        }

        val permissions: Array<String>? = getRequiredPermissions(contentUri)
        if (permissions == null || permissions.size == 0) {
            return true
        }

        for (permission in permissions) {
            if (ContextCompat.checkSelfPermission(context, permission)
                != PackageManager.PERMISSION_GRANTED
            ) {
                LogUtils.w("PickerUtils", "Missing permission: " + permission + " for URI: " + contentUri)
                return false
            }
        }
        return true
    }


    /**
     * 根据媒体类型 URI 获取所需的权限
     * @param contentUri 媒体类型 URI
     * @return 所需权限数组
     */
    private fun getRequiredPermissions(contentUri: Uri?): Array<String>? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ 使用新的媒体权限
            if (AndroidStorage.EXTERNAL_IMAGE_URI == contentUri) {
                return arrayOf<String>(Manifest.permission.READ_MEDIA_IMAGES)
            } else if (AndroidStorage.EXTERNAL_VIDEO_URI == contentUri) {
                return arrayOf<String>(Manifest.permission.READ_MEDIA_VIDEO)
            } else if (AndroidStorage.EXTERNAL_AUDIO_URI == contentUri) {
                return arrayOf<String>(Manifest.permission.READ_MEDIA_AUDIO)
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Android 6.0 - Android 12 使用存储权限
            return arrayOf<String>(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        return null
    }
}