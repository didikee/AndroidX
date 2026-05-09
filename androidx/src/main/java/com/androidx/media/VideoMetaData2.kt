package com.androidx.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * user author: didikee
 * create time: 2026/5/8
 * description: 视频编解码信息，不包含文件存储信息
 */
@Parcelize
data class VideoMetaData2(
    var width: Int = 0,
    var height: Int = 0,
    var duration: Long = 0,
    var bitRate: Int = 0,
    var rotation: Int = 0,
    var colorFormat: Int = 0,
    var frameRate: Int = 0,
    var iFrameRate: Float = 0f,
    var videoBitrate: Double = 0.0,
    var audioBitrate: Double = 0.0,
    var mimeType: String = ""
) : Parcelable {

    fun getRealSize(): Resolution {
        val realWidth: Int
        val realHeight: Int
        if (rotation == 90 || rotation == 270) {
            realWidth = height
            realHeight = width
        } else {
            realWidth = width
            realHeight = height
        }
        return Resolution(realWidth, realHeight)
    }

    fun isValid(): Boolean {
        return width > 0 && height > 0 && duration > 0
    }
}
