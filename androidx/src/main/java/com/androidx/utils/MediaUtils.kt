package com.androidx.utils

import android.content.Context
import android.media.ExifInterface
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.androidx.AndroidStorage.isAboveVersionQ
import com.androidx.LogUtils
import java.io.InputStream

/**
 * user author: didikee
 * create time: 4/21/21 11:04 PM
 * description:
 */
object MediaUtils {
    /**
     * 获取多媒体的文件类型
     * @param inputStream
     * @return
     */
    fun getMimeType(inputStream: InputStream?): String {
        return ""
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param imageUri 图片绝对路径
     * @return degree旋转的角度
     */
    fun getImageDegree(context: Context, imageUri: Uri): Int {
        var degree = 0
        try {
            val exifInterface = if (isAboveVersionQ) {
                ExifInterface(context.contentResolver.openInputStream(imageUri)!!)
            } else {
                ExifInterface(
                    UriUtils.getPathFromUri(
                        context,
                        imageUri
                    )
                )
            }
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return degree
    }

    fun getImageDegree(path: String): Int {
        var degree = 0
        try {
            val exifInterface = ExifInterface(path)
            val orientation = exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return degree
    }

    // 获取视频或者音频的时长
    fun getMediaDuration(context: Context, videoUri: Uri): Long {
        val retriever = MediaMetadataRetriever()
        var mediaDuration = 0L

        try {
            // 设置数据源（Uri）
            retriever.setDataSource(context, videoUri)
            mediaDuration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0

        } catch (e: Exception) {
            LogUtils.e("MediaMetadataUtils Error retrieving video metadata: ${e.message}")
        } finally {
            // 释放资源
            try {
                retriever.release()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mediaDuration
    }
}
