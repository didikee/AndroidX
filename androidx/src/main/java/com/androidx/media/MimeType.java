package com.androidx.media;

import android.os.Build;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.androidx.utils.LegacyMimeUtils;

/**
 * user author: didikee
 * create time: 2019-12-03 11:38
 * description:
 *
 * mimetype列表参考：<a href="https://developer.android.com/reference/androidx/media3/common/MimeTypes">MimeTypes列表</a>
 */
public final class MimeType {
    public static final String UNKNOWN = "";
    public static final String ALL = "*/*";
    public static final String PREFIX_VIDEO = "video";
    public static final String PREFIX_AUDIO = "audio";
    public static final String PREFIX_IMAGE = "image";
    // Text
    public static final String TEXT = "text/plain";
    // image
    public static final String IMAGE = "image/*";
    public static final String PNG = "image/png";
    public static final String JPEG = "image/jpeg";
    public static final String GIF = "image/gif";
    public static final String WEBP = "image/webp";
    public static final String HEIC = "image/heic";
    public static final String HEIF = "image/heif";
    // video
    public static final String VIDEO = "video/*";
    public static final String MP4 = "video/mp4";
    public static final String _3GP = "video/3gp";
    // audio
    public static final String AUDIO = "audio/*";
    public static final String MP3 = "audio/mpeg";
    public static final String AAC = "audio/aac";
    public static final String _3GPP = "audio/3gpp";
    public static final String WAV = "audio/x-wav";
    public static final String AMR = "audio/amr";
    public static final String M4A = "audio/mpeg";

    // Document files
    public static final String PDF = "application/pdf";

    public static boolean isVideo(String mimeType) {
        return !TextUtils.isEmpty(mimeType) && mimeType.toLowerCase().startsWith(PREFIX_VIDEO);
    }

    public static boolean isImage(String mimeType) {
        return !TextUtils.isEmpty(mimeType) && mimeType.toLowerCase().startsWith(PREFIX_IMAGE);
    }

    public static boolean isAudio(String mimeType) {
        return !TextUtils.isEmpty(mimeType) && mimeType.toLowerCase().startsWith(PREFIX_AUDIO);
    }

    /**
     * 根据类型，获取对应的拓展名
     * 核心类，已经被隐藏无法直接查看
     * http://androidxref.com/4.4.4_r1/xref/libcore/luni/src/main/java/libcore/net/MimeUtils.java
     *
     * @param extension
     * @return
     */
    public static String getMimeTypeFromExtension(String extension) {
        String mimeTypeFromExtension;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            mimeTypeFromExtension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        } else {
            mimeTypeFromExtension = LegacyMimeUtils.guessMimeTypeFromExtension(extension);
        }
        if (TextUtils.isEmpty(mimeTypeFromExtension)) {
            return MimeType.UNKNOWN;
        } else {
            return mimeTypeFromExtension;
        }
    }

    /**
     * 根据拓展名，或者类型名
     * @param mimeType
     * @return
     */
    public static String getExtensionFromMimeType(String mimeType) {
        String extensionFromMimeType;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
            extensionFromMimeType = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
        } else {
            extensionFromMimeType = LegacyMimeUtils.guessExtensionFromMimeType(mimeType);
        }
        if (TextUtils.isEmpty(extensionFromMimeType)) {
            return "";
        } else {
            return extensionFromMimeType;
        }
    }

    public static String getMimeTypeFromFilename(String filename) {
        String extension = "";
        try {
            extension = filename.substring(filename.lastIndexOf(".") + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(extension)) {
            return UNKNOWN;
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                // aac要单独判断，因为4.4没有aac格式
                if ("aac".equalsIgnoreCase(extension)) {
                    return AAC;
                }
            }
            return getMimeTypeFromExtension(extension.toLowerCase());
        }
    }

}
