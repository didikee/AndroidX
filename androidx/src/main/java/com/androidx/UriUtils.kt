package com.androidx

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.androidx.AndroidUtils.getOSVersion
import com.androidx.media.MediaStoreInfo
import com.androidx.media.MediaUriInfo
import com.androidx.utils.UriUtils
import java.io.File
import java.nio.charset.StandardCharsets

/**
 * user author: didikee
 * create time: 2026/1/5 08:14
 * description: 新的uri辅助类
 */
object UriUtils {

    fun rename(
        contentResolver: ContentResolver,
        uri: Uri,
        newNameWithExtension: String
    ): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newNameWithExtension)
        }
        return try {
            val rows = contentResolver.update(
                uri,
                values,
                null,
                null
            )
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun delete(contentResolver: ContentResolver, uri: Uri): Boolean {
        try {
            val rowsDeleted = contentResolver.delete(uri, null, null)
            return rowsDeleted > 0
        } catch (e: SecurityException) {
            e.printStackTrace()
            // 如果是 Android 10+ 且文件来自外部共享目录，可能会抛出 RecoverableSecurityException
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                LogUtils.w("Delete failed: Security error")
            } else {
                LogUtils.e("Detele failed: Permission denied")
            }
        }
        return false
    }

    // 查询文件名
    fun queryDisplayName(contentResolver: ContentResolver, uri: Uri): String {
        val queryResult = when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                // content:// URI
                contentResolver.query(
                    uri,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                    } else ""
                } ?: ""
            }

            ContentResolver.SCHEME_FILE -> {
                // file:// URI
                File(uri.path ?: return "").name
            }

            else -> ""
        }
        return queryResult ?: ""
    }


    /**
     * 旋转方向的字段在api29开始才加入，所以目前还是推荐通过媒体解析得到视频的基本媒体信息，文件信息的话倒是可以使用uri查询的形式
     *
     * @param contentResolver
     * @param uri
     * @return
     */
    fun getVideoStoreInfo(contentResolver: ContentResolver?, uri: Uri?): MediaStoreInfo? {
        if (contentResolver != null && uri != null) {
            val projections = UriUtils.getCommonProjects()

            projections.add(MediaStore.Video.Media.DURATION)
            projections.add(MediaStore.Video.Media.WIDTH)
            projections.add(MediaStore.Video.Media.HEIGHT)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN)
                //在sdk >= 29 后才加入了视频的方向api,所以先不取这个字段
                projections.add(MediaStore.Video.Media.ORIENTATION)
            } else {
                projections.add(MediaStore.Video.Media.DATE_TAKEN)
            }
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(
                    uri,
                    projections.toTypedArray<String?>(),
                    null,
                    null,
                    null
                )
                cursor?.let {
                    if (it.moveToFirst()) {
                        val mediaUriInfo = MediaStoreInfo()
                        mediaUriInfo.id = it.getLongSafely(MediaStore.MediaColumns._ID)
                        mediaUriInfo.displayName =
                            it.getStringSafely(MediaStore.MediaColumns.DISPLAY_NAME)
                        mediaUriInfo.mimeType =
                            it.getStringSafely(MediaStore.MediaColumns.MIME_TYPE)

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.relativePath = it.getStringSafely(
                                MediaStore.MediaColumns.RELATIVE_PATH
                            )
                        } else {
                            mediaUriInfo.data =
                                it.getStringSafely(MediaStore.MediaColumns.DATA)
                        }
                        mediaUriInfo.size =
                            it.getLongSafely(MediaStore.MediaColumns.SIZE)
                        mediaUriInfo.dateAdded =
                            it.getLongSafely(MediaStore.MediaColumns.DATE_ADDED)
                        mediaUriInfo.dateModified =
                            it.getLongSafely(MediaStore.MediaColumns.DATE_MODIFIED)

                        // custom
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.dateTaken =
                                it.getLongSafely(UriUtils.DATE_TAKEN)

                            mediaUriInfo.rotate =
                                it.getIntSafely(MediaStore.Video.Media.ORIENTATION)
                        } else {
                            mediaUriInfo.dateTaken =
                                it.getLongSafely(MediaStore.Video.Media.DATE_TAKEN)
                        }
                        mediaUriInfo.duration = it.getLongSafely(MediaStore.Video.Media.DURATION)

                        mediaUriInfo.width =
                            it.getIntSafely(MediaStore.Video.Media.WIDTH)

                        mediaUriInfo.height =
                            it.getIntSafely(MediaStore.Video.Media.HEIGHT)
                        return mediaUriInfo
                    }
                } ?: run {
                    LogUtils.e("getVideoInfo get a empty cursor: " + uri.toString())
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        return null
    }

    /**
     * 获取图片信息
     *
     * @param contentResolver
     * @param uri
     * @return
     */
    fun getImageInfo(contentResolver: ContentResolver?, uri: Uri?): MediaStoreInfo? {
        if (contentResolver != null && uri != null) {
            val projections = UriUtils.getCommonProjects()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN)
            } else {
                projections.add(MediaStore.Images.Media.DATE_TAKEN)
            }
            projections.add(MediaStore.Images.Media.WIDTH)
            projections.add(MediaStore.Images.Media.HEIGHT)
            projections.add(MediaStore.Images.Media.ORIENTATION)
            if (getOSVersion() >= 30) {
                projections.add(MediaStore.Images.Media.XMP)
            }
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(
                    uri,
                    projections.toTypedArray<String?>(),
                    null,
                    null,
                    null
                )
                cursor?.let {
                    if (it.moveToFirst()) {
                        val id = it.getLongSafely(MediaStore.MediaColumns._ID)
                        val displayName =
                            it.getStringSafely(MediaStore.MediaColumns.DISPLAY_NAME)
                        val mimeType = it.getStringSafely(MediaStore.MediaColumns.MIME_TYPE)
                        val relativePath: String
                        val data: String
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            relativePath =
                                it.getStringSafely(MediaStore.MediaColumns.RELATIVE_PATH)
                            data = ""
                        } else {
                            relativePath = ""
                            data = it.getStringSafely(MediaStore.MediaColumns.DATA)
                        }
                        val size = it.getLongSafely(MediaStore.MediaColumns.SIZE)
                        val dateAdded = it.getLongSafely(MediaStore.MediaColumns.DATE_ADDED)
                        val dateModified =
                            it.getLongSafely(MediaStore.MediaColumns.DATE_MODIFIED)

                        // custom
                        val dateToken = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            it.getLongSafely(UriUtils.DATE_TAKEN)
                        } else {
                            it.getLongSafely(MediaStore.Images.Media.DATE_TAKEN)
                        }
                        val width = it.getIntSafely(MediaStore.Images.Media.WIDTH)
                        val height = it.getIntSafely(MediaStore.Images.Media.HEIGHT)
                        val rotate = it.getIntSafely(MediaStore.Images.Media.ORIENTATION)

                        var xmpString = ""
                        if (getOSVersion() >= 30) {
                            val xmpIndex = it.getColumnIndex(MediaStore.Images.Media.XMP)
                            if (xmpIndex != -1) {
                                val xmpData = it.getBlob(xmpIndex)
                                if (xmpData != null && xmpData.size > 0) {
                                    // 将字节数组转换为字符串
                                    xmpString = String(xmpData, StandardCharsets.UTF_8)
                                }
                            }
                        }
                        return MediaStoreInfo(
                            id = id,
                            displayName = displayName,
                            mimeType = mimeType,
                            data = data,
                            relativePath = relativePath,
                            size = size,
                            dateAdded = dateAdded,
                            dateModified = dateModified,
                            dateTaken = dateToken,
                            width = width,
                            height = height,
                            rotate = rotate,
                            duration = 0L,
                            xmp = xmpString
                        )
                    }
                } ?: run {
                    LogUtils.e("getImageInfo get a empty cursor: $uri")
                }
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        return null
    }

    fun Cursor.getStringSafely(column: String): String {
        val index = getColumnIndex(column)
        return if (index != -1 && !isNull(index)) {
            getString(index)
        } else {
            LogUtils.w("CursorExt", "getStringSafely: Column not found or null: $column")
            ""
        }
    }

    fun Cursor.getLongSafely(column: String, default: Long = 0L): Long {
        val index = getColumnIndex(column)
        return if (index != -1 && !isNull(index)) {
            getLong(index)
        } else {
            LogUtils.w("CursorExt", "getLongSafely: Column not found or null: $column")
            default
        }
    }

    fun Cursor.getIntSafely(column: String, default: Int = 0): Int {
        val index = getColumnIndex(column)
        return if (index != -1 && !isNull(index)) {
            getInt(index)
        } else {
            LogUtils.w("CursorExt", "getIntSafely: Column not found or null: $column")
            default
        }
    }

    /**
     * 获取音频信息
     *
     * @param contentResolver
     * @param uri
     * @return
     */
    fun getAudioStoreInfo(contentResolver: ContentResolver?, uri: Uri?): MediaStoreInfo? {
        if (contentResolver != null && uri != null) {
            val projections = UriUtils.getCommonProjects()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN)
            }
            projections.add(MediaStore.Audio.Media.DURATION)
            var cursor: Cursor? = null
            try {
                cursor = contentResolver.query(
                    uri,
                    projections.toTypedArray<String?>(),
                    null,
                    null,
                    null
                )
                cursor?.let {
                    if (it.moveToFirst()) {
                        val mediaUriInfo = MediaStoreInfo()
                        mediaUriInfo.id = it.getLongSafely(MediaStore.MediaColumns._ID)
                        mediaUriInfo.displayName = it.getStringSafely(MediaStore.MediaColumns.DISPLAY_NAME)
                        mediaUriInfo.mimeType = it.getStringSafely(MediaStore.MediaColumns.MIME_TYPE)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.relativePath = it.getStringSafely(MediaStore.MediaColumns.RELATIVE_PATH)
                        } else {
                            mediaUriInfo.data = it.getStringSafely(MediaStore.MediaColumns.DATA)
                        }
                        mediaUriInfo.size = it.getLongSafely(MediaStore.MediaColumns.SIZE)
                        mediaUriInfo.dateAdded = it.getLongSafely(MediaStore.MediaColumns.DATE_ADDED)
                        mediaUriInfo.dateModified = it.getLongSafely(MediaStore.MediaColumns.DATE_MODIFIED)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.dateTaken = it.getLongSafely(UriUtils.DATE_TAKEN)
                        }
                        mediaUriInfo.duration = it.getLongSafely(MediaStore.Audio.Media.DURATION)
                        return mediaUriInfo
                    }
                } ?: run {
                    LogUtils.w("getAudioInfo get a empty cursor: $uri")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                cursor?.close()
            }
        }
        return null
    }

}