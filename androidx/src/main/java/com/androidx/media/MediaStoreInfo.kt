package com.androidx.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * user author: didikee
 * create time: 2025/12/7 13:14
 * description: MediaStore 查询Uri 的信息，不是Metadata
 */
@Parcelize
data class MediaStoreInfo(
    var id: Long = 0,
    var displayName: String = "",
    var mimeType: String = "",
    @Deprecated("Use relativePath instead")
    var data: String = "",
    // 相对布局
    var relativePath: String = "",
    var size: Long = 0,
    var dateAdded: Long = 0,
    var dateModified: Long = 0,
    var dateTaken: Long = 0,
    // image & video
    var width: Int = 0,
    var height: Int = 0,
    var rotate: Int = 0,
    // Audio & Video
    var duration: Long = 0,
    // Motion photo
    var xmp: String = ""
) : Parcelable {
    fun isVideo(): Boolean {
        return MimeType.isVideo(mimeType)
    }
    fun isAudio(): Boolean {
        return MimeType.isAudio(mimeType)
    }
    fun isImage(): Boolean {
        return MimeType.isImage(mimeType)
    }
}

