package com.androidx.media.video

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.androidx.media.data.VideoFullInfo
import com.androidx.media.data.VideoInfo
import java.io.FileNotFoundException

/**
 * user author: didikee
 * create time: 2025/7/24 23:33
 * description:
 */
object VideoMetadataUtils {
    private const val TAG = "VideoMetadataUtils"

    // 基础版视频信息
    fun getSimpleVideoInfo(context: Context, uri: Uri): VideoInfo? {
        // 先尝试用 MediaMetadataRetriever
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSourceCompat(context, uri)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
            val rotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull() ?: 0
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val mimeType =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ""

            // 文件名、文件大小、修改时间需要用ContentResolver补充
            val fileInfo = queryFileInfo(context, uri)

            if (width != null && height != null && durationMs > 0) {
                return VideoInfo(
                    width = width,
                    height = height,
                    rotation = rotation,
                    fileName = fileInfo?.fileName ?: "",
                    fileSize = fileInfo?.fileSize ?: 0L,
                    mimeType = if (mimeType.isNotEmpty()) mimeType else (fileInfo?.mimeType ?: ""),
                    dateModified = fileInfo?.dateModified ?: 0L,
                    durationMs = durationMs
                )
            }
            // 如果 retriever 获取的宽高是null，退回使用 ContentResolver
        } catch (e: Exception) {
            Log.w(TAG, "MediaMetadataRetriever failed for uri=$uri", e)
        } finally {
            retriever.release()
        }

        // fallback 用 ContentResolver 查询
        return queryFileInfo(context, uri)
    }

    // 全量版视频信息
    fun getFullVideoInfo(context: Context, uri: Uri): VideoFullInfo? {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSourceCompat(context, uri)

            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
                ?.toIntOrNull()
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
                ?.toIntOrNull()
            val rotation =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
                    ?.toIntOrNull() ?: 0
            val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull() ?: 0L
            val videoBitrate =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)
                    ?.toIntOrNull()
//            val audioBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_AUDIO_BITRATE)?.toIntOrNull()
            //TODO 暂时没有找到 METADATA_KEY_AUDIO_BITRATE key
            val audioBitrate = 0
            val hasAudio =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) == "yes"
//            val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
//                ?.toFloatOrNull() ?: retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_RATE)?.toFloatOrNull()
//            val videoCodec = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_CODEC)
            val frameRate = 0f
            val videoCodec = ""

            val mimeType =
                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: ""

            // 文件名、文件大小、修改时间通过 ContentResolver 补充
            val fileInfo = queryFileInfo(context, uri)

            if (width != null && height != null && durationMs > 0) {
                return VideoFullInfo(
                    width = width,
                    height = height,
                    rotation = rotation,
                    fileName = fileInfo?.fileName ?: "",
                    fileSize = fileInfo?.fileSize ?: 0L,
                    mimeType = if (mimeType.isNotEmpty()) mimeType else (fileInfo?.mimeType ?: ""),
                    dateModified = fileInfo?.dateModified ?: 0L,
                    durationMs = durationMs,
                    videoBitrate = videoBitrate,
                    audioBitrate = audioBitrate,
                    frameRate = frameRate,
                    hasAudio = hasAudio,
                    videoCodec = videoCodec
                )
            }
        } catch (e: Exception) {
            Log.w(TAG, "MediaMetadataRetriever failed for uri=$uri", e)
        } finally {
            retriever.release()
        }

        // fallback 简单版用 ContentResolver 信息封装成 FullVideoInfo，缺少详细字段
        val fallback = queryFileInfo(context, uri) ?: return null
        return VideoFullInfo(
            width = fallback.width,
            height = fallback.height,
            rotation = fallback.rotation,
            fileName = fallback.fileName,
            fileSize = fallback.fileSize,
            mimeType = fallback.mimeType,
            dateModified = fallback.dateModified,
            durationMs = 0L,
            videoBitrate = null,
            audioBitrate = null,
            frameRate = null,
            hasAudio = false,
            videoCodec = null
        )
    }

    // ContentResolver 查询视频文件基本信息
    private fun queryFileInfo(context: Context, uri: Uri): VideoInfo? {
        val resolver = context.contentResolver
        val projection = arrayOf(
            MediaStore.Video.VideoColumns.WIDTH,
            MediaStore.Video.VideoColumns.HEIGHT,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.SIZE,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Video.VideoColumns.DATE_MODIFIED,
            MediaStore.Video.VideoColumns.DURATION
        )
        try {
            resolver.query(uri, projection, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val width = cursor.getIntOrDefault(MediaStore.Video.VideoColumns.WIDTH, 0)
                    val height = cursor.getIntOrDefault(MediaStore.Video.VideoColumns.HEIGHT, 0)
                    val fileName =
                        cursor.getStringOrNull(MediaStore.Video.VideoColumns.DISPLAY_NAME) ?: ""
                    val fileSize = cursor.getLongOrDefault(MediaStore.Video.VideoColumns.SIZE, 0L)
                    val mimeType =
                        cursor.getStringOrNull(MediaStore.Video.VideoColumns.MIME_TYPE) ?: ""
                    val dateModified =
                        cursor.getLongOrDefault(MediaStore.Video.VideoColumns.DATE_MODIFIED, 0L)
                    val durationMs =
                        cursor.getLongOrDefault(MediaStore.Video.VideoColumns.DURATION, 0L)
                    return VideoInfo(
                        width = width,
                        height = height,
                        rotation = 0,
                        fileName = fileName,
                        fileSize = fileSize,
                        mimeType = mimeType,
                        dateModified = dateModified,
                        durationMs = durationMs
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "queryFileInfo failed for uri=$uri", e)
        }
        return null
    }

    // Cursor 辅助扩展
    private fun android.database.Cursor.getIntOrDefault(columnName: String, default: Int): Int {
        val idx = getColumnIndex(columnName)
        return if (idx >= 0 && !isNull(idx)) getInt(idx) else default
    }

    private fun android.database.Cursor.getLongOrDefault(columnName: String, default: Long): Long {
        val idx = getColumnIndex(columnName)
        return if (idx >= 0 && !isNull(idx)) getLong(idx) else default
    }

    private fun android.database.Cursor.getStringOrNull(columnName: String): String? {
        val idx = getColumnIndex(columnName)
        return if (idx >= 0 && !isNull(idx)) getString(idx) else null
    }

    private fun MediaMetadataRetriever.setDataSourceCompat(context: Context, uri: Uri) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // API 23+ 可以直接用setDataSource(context, uri)
                this.setDataSource(context, uri)
            } else {
                // 低版本用传统文件路径
                val path = getPathFromUri(context, uri)
                if (path != null) {
                    this.setDataSource(path)
                } else {
                    throw FileNotFoundException("Failed to get file path from Uri: $uri")
                }
            }
        } catch (e: Exception) {
            Log.w("VideoMetadataUtils", "setDataSource with context+uri failed, try fallback fd", e)
            // fallback: open file descriptor 传入fd
            try {
                context.contentResolver.openFileDescriptor(uri, "r")?.use { pfd ->
                    this.setDataSource(pfd.fileDescriptor)
                } ?: throw FileNotFoundException("OpenFileDescriptor is null for Uri: $uri")
            } catch (ex: Exception) {
                Log.e("VideoMetadataUtils", "setDataSource fallback with fileDescriptor failed", ex)
                throw ex
            }
        }
    }

    // 辅助：尝试从 Uri 获取真实路径（不一定所有 Uri 都能成功）
    private fun getPathFromUri(context: Context, uri: Uri): String? {
        // 这里可以写常用几种Uri解析逻辑，比较复杂，简化示例：
        // 最常见是MediaStore内容Uri或者File Uri
        if ("file" == uri.scheme) {
            return uri.path
        }
        // 这里可以加更多逻辑，视具体业务需求而定
        return null
    }
}