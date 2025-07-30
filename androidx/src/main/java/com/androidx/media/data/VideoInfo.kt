package com.androidx.media.data

/**
 * user author: didikee
 * create time: 2025/7/24 23:24
 * description:
 * ✅ 1. 简易版 SimpleVideoInfo
 * 提供：
 * 分辨率（处理旋转）
 * 宽高（修正后的）
 * 文件名
 * 文件大小
 * MIME 类型
 * 修改时间
 */
class VideoInfo(
    width: Int,
    height: Int,
    rotation: Int,
    fileName: String,
    fileSize: Long,
    mimeType: String,
    dateModified: Long,
    durationMs: Long,
) : BaseVideoInfo(width, height, rotation, fileName, fileSize, mimeType, dateModified,durationMs)

