package com.androidx.media.data

/**
 * user author: didikee
 * create time: 2025/7/24 23:24
 * description:
 */
class VideoFullInfo(
    width: Int,
    height: Int,
    rotation: Int,
    fileName: String,
    fileSize: Long,
    mimeType: String,
    dateModified: Long,
    durationMs: Long,
    val videoBitrate: Int?,
    val audioBitrate: Int?,
    val frameRate: Float?,
    val hasAudio: Boolean,
    val videoCodec: String?
) : BaseVideoInfo(width, height, rotation, fileName, fileSize, mimeType, dateModified, durationMs)
