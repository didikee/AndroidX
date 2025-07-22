package com.androidx.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.Matrix
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


    /**
     * 旋转bitmap
     *
     * @param source 原图
     * @param angle  角度
     * @return 旋转后的图片
     */
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

}