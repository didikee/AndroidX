package com.androidx.media

import android.os.Build
import android.text.TextUtils
import android.webkit.MimeTypeMap
import com.androidx.utils.LegacyMimeUtils
import java.util.Locale

/**
 * user author: didikee
 * create time: 2019-12-03 11:38
 * description:
 *
 * mimetype列表参考：[MimeTypes列表](https://developer.android.com/reference/androidx/media3/common/MimeTypes)
 */
object MimeType {
    const val UNKNOWN: String = ""
    const val ALL: String = "*/*"
    const val PREFIX_VIDEO: String = "video"
    const val PREFIX_AUDIO: String = "audio"
    const val PREFIX_IMAGE: String = "image"

    // Text
    const val TEXT: String = "text/plain"

    // image
    const val IMAGE: String = "image/*"
    const val PNG: String = "image/png"
    const val JPEG: String = "image/jpeg"
    const val GIF: String = "image/gif"
    const val WEBP: String = "image/webp"
    const val HEIC: String = "image/heic"
    const val HEIF: String = "image/heif"
    const val AVIF: String = "image/avif"

    // video
    const val VIDEO: String = "video/*"
    const val MP4: String = "video/mp4"
    const val _3GP: String = "video/3gp"

    // audio
    const val AUDIO: String = "audio/*"
    const val MP3: String = "audio/mpeg"
    const val AAC: String = "audio/aac"
    const val _3GPP: String = "audio/3gpp"
    const val WAV: String = "audio/x-wav"
    const val AMR: String = "audio/amr"
    const val M4A: String = "audio/mpeg"
    const val AC3:String = "audio/ac3"
    const val WMA:String = "audio/x-ms-wma"
    const val FLAC:String = "audio/flac"
    const val OGG:String = "audio/ogg"

    // Document files
    const val PDF: String = "application/pdf"

    @JvmStatic
    fun isVideo(mimeType: String?): Boolean {
        return !TextUtils.isEmpty(mimeType) && mimeType!!.lowercase(Locale.getDefault()).startsWith(
            PREFIX_VIDEO
        )
    }

    @JvmStatic
    fun isImage(mimeType: String?): Boolean {
        return !TextUtils.isEmpty(mimeType) && mimeType!!.lowercase(Locale.getDefault()).startsWith(
            PREFIX_IMAGE
        )
    }

    @JvmStatic
    fun isAudio(mimeType: String?): Boolean {
        return !TextUtils.isEmpty(mimeType) && mimeType!!.lowercase(Locale.getDefault()).startsWith(
            PREFIX_AUDIO
        )
    }

    /**
     * 根据类型，获取对应的拓展名
     * 核心类，已经被隐藏无法直接查看
     * http://androidxref.com/4.4.4_r1/xref/libcore/luni/src/main/java/libcore/net/MimeUtils.java
     *
     * @param extension
     * @return
     */
    @JvmStatic
    fun getMimeTypeFromExtension(extension: String?): String {
        val mimeTypeFromExtension = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
        } else {
            LegacyMimeUtils.guessMimeTypeFromExtension(extension)
        }
        return if (TextUtils.isEmpty(mimeTypeFromExtension)) {
            UNKNOWN
        } else {
            mimeTypeFromExtension!!
        }
    }

    /**
     * 根据拓展名，或者类型名
     * @param mimeType
     * @return
     */
    fun getExtensionFromMimeType(mimeType: String?): String {
        val extensionFromMimeType = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
        } else {
            LegacyMimeUtils.guessExtensionFromMimeType(mimeType)
        }
        return if (TextUtils.isEmpty(extensionFromMimeType)) {
            ""
        } else {
            extensionFromMimeType!!
        }
    }

    @JvmStatic
    fun getMimeTypeFromFilename(filename: String): String {
        var extension = ""
        try {
            extension = filename.substring(filename.lastIndexOf(".") + 1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (TextUtils.isEmpty(extension)) {
            return UNKNOWN
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                // aac要单独判断，因为4.4没有aac格式
                if ("aac".equals(extension, ignoreCase = true)) {
                    return AAC
                }
            }
            return getMimeTypeFromExtension(extension.lowercase(Locale.getDefault()))
        }
    }
}
