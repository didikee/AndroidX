package com.androidx.picker

import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.androidx.AndroidUtils
import java.nio.charset.StandardCharsets

/**
 * description:
 */
class MediaItemDataHandler : DataHandler<MediaItem> {
    val result: ArrayList<MediaItem> = ArrayList()

    override fun handle(
        cursor: Cursor, projections: ArrayList<String>,
        uri: Uri, displayName: String,
        mimeType: String, size: Long, dateAdded: Long,
        dateModified: Long, data: String, relativePath: String
    ): MediaItem {
        var width = 0
        var height = 0
        var duration: Long = 0
        var xmp = ""
        val osVersion = AndroidUtils.getOSVersion()
        for (projection in projections) {
            when (projection) {
                MediaStore.MediaColumns.WIDTH -> width =
                    cursor.getInt(cursor.getColumnIndexOrThrow(projection))

                MediaStore.MediaColumns.HEIGHT -> height =
                    cursor.getInt(cursor.getColumnIndexOrThrow(projection))

                MediaStore.MediaColumns.DURATION -> {
                    if (osVersion >= Build.VERSION_CODES.Q) {
                        duration = cursor.getLong(cursor.getColumnIndexOrThrow(projection))
                    } else {
                        // 低于android q无法直接查询到时长
                    }
                }
                "xmp" -> {
                    if (osVersion >= 30) {
                        val xmpIndex = cursor.getColumnIndex(MediaStore.Images.Media.XMP)
                        if (xmpIndex != -1) {
                            val xmpData = cursor.getBlob(xmpIndex)
                            if (xmpData != null && xmpData.size > 0) {
                                xmp = String(xmpData, StandardCharsets.UTF_8)
                            }
                        }

                    }
                }
            }
        }
        val mediaItem = MediaItem(uri)
        // 设置公共参数
        mediaItem.displayName = displayName
        mediaItem.size = size
        mediaItem.mimeType = mimeType
        mediaItem.dateAdded = dateAdded
        mediaItem.dateModified = dateModified
        mediaItem.data = data
        mediaItem.relativePath = relativePath
        //封装实体
        mediaItem.width = width
        mediaItem.height = height
        mediaItem.duration = duration
        mediaItem.xmp = xmp
        result.add(mediaItem)
        return mediaItem
    }

    override fun getDataResult(): ArrayList<MediaItem> {
        return result
    }
}
