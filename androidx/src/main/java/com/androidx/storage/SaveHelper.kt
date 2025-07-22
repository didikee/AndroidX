package com.androidx.storage

import android.content.ContentResolver
import android.content.ContentValues
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.androidx.MediaStoreUtils
import java.io.IOException
import java.io.OutputStream

/**
 * user author: didikee
 * create time: 4/23/20 9:15 AM
 * description: 一个用于保存文件的辅助类
 */
object SaveHelper {
    /**
     * 保存照片
     * @param contentResolver
     * @param contentValues
     * @param bytes
     * @return
     */
    fun saveImage(
        contentResolver: ContentResolver,
        contentValues: ContentValues,
        bytes: ByteArray?
    ): Uri? {
        val imageContentUri = MediaStoreUtils.EXTERNAL_IMAGE_PRIMARY_URI
        return saveMedia(contentResolver, imageContentUri, contentValues, bytes)
    }

    /**
     * 保存视频
     * @param contentResolver
     * @param contentValues 应该包含视频的MediaStore元数据（如TITLE, DISPLAY_NAME, MIME_TYPE等）
     * @param bytes 视频文件的字节数据
     * @return 保存成功后返回视频的Uri，失败返回null
     */
    fun saveVideo(
        contentResolver: ContentResolver,
        contentValues: ContentValues,
        bytes: ByteArray?
    ): Uri? {
        val videoContentUri = MediaStoreUtils.EXTERNAL_VIDEO_PRIMARY_URI
        return saveMedia(contentResolver, videoContentUri, contentValues, bytes)
    }

    /**
     * 保存媒体文件的通用私有方法
     */
    private fun saveMedia(
        contentResolver: ContentResolver,
        contentUri: Uri,
        contentValues: ContentValues,
        bytes: ByteArray?
    ): Uri? {
        if (bytes == null) {
            return null
        }
        val uri = contentResolver.insert(contentUri, contentValues) ?: return null

        var outputStream: OutputStream? = null
        try {
            outputStream = contentResolver.openOutputStream(uri)
            outputStream?.let {
                it.write(bytes)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, false)
                    contentResolver.update(uri, contentValues, null, null)
                }
                return uri
            }
        } catch (e: Exception) {
            e.printStackTrace()
            contentResolver.delete(uri, null, null)
        } finally {
            try {
                outputStream?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return null
    }
}
