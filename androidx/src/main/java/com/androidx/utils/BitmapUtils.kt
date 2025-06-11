package com.androidx.utils

import android.content.ContentResolver
import android.net.Uri
import androidx.exifinterface.media.ExifInterface

/**
 * user author: didikee
 * create time: 2025/5/20 上午8:50
 * description:
 */
object BitmapUtils {

    // 获取图片的旋转方向
    fun getRotation(contentResolver: ContentResolver, uri: Uri): Int {
        contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            return when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
        return 0
    }

}