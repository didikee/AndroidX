package com.androidx.media.data

import com.androidx.media.Resolution

/**
 * user author: didikee
 * create time: 2025/7/24 23:28
 * description:
 */
open class BaseVideoInfo(
    val width: Int,           // 原始宽度（视频文件中的宽度）
    val height: Int,          // 原始高度
    val rotation: Int,        // 旋转角度
    val fileName: String,
    val fileSize: Long,
    val mimeType: String,
    val dateModified: Long,
    val durationMs: Long,
) {
    val realWidth: Int
        get() = if (rotation == 90 || rotation == 270) height else width

    val realHeight: Int
        get() = if (rotation == 90 || rotation == 270) width else height

    val realResolution: Resolution
        get() = Resolution(realWidth, realHeight)
}