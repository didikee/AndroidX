package com.androidx.tools

import android.content.ContentResolver
import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.androidx.LogUtils
import com.androidx.media.ExifUtils.convertDMSFractionToDecimal
import com.androidx.media.ImageExif
import com.androidx.media.Resolution
import com.androidx.utils.UriUtils
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Locale

/**
 * user author: didikee
 * create time: 4/28/21 5:30 PM
 * description:
 */
object ImageUtils {
    fun getExif(path: String): ImageExif? {
        return ExifInterfaceX().decodeExif(path)
    }

    fun getExif(context: Context?, uri: Uri?): ImageExif? {
        if (context == null || uri == null) {
            return null
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return ExifInterfaceX().decodeExif(context.contentResolver, uri)
        } else {
            val pathFromUri = UriUtils.getPathFromUri(context, uri)
            if (!TextUtils.isEmpty(pathFromUri)) {
                val file = File(pathFromUri)
                if (file.exists()) {
                    return ExifInterfaceX().decodeExif(pathFromUri!!)
                }
            }
        }
        return null
    }

    fun getOrientationDescription(res: Resources?, orientation: Int): String {
        val desc = when (orientation) {
            1 -> "Flip horizontally"
            2 -> "Flip vertically"
            3 -> "Rotate 180 degrees"
            4 -> "Rotate 180 degrees and flip horizontally"
            5 -> "Rotate 270 degrees and flip horizontally"
            6 -> "Rotate 90 degrees"
            7 -> "Rotate 90 degrees and flip horizontally"
            8 -> "Rotate 270 degrees"
            else -> "Normal"
        }
        return desc
    }

    fun formatLongitudeAndLatitude(originValue: String?): String {
        if (!TextUtils.isEmpty(originValue)) {
            val d = convertDMSFractionToDecimal(originValue)
            return String.format(Locale.getDefault(), "%.5f", d)
        }
        return ""
    }

    // 判断是否是实况照片
    fun isMotionPhoto(xmp: String?): Boolean {
        if (xmp.isNullOrEmpty()) return false

        return xmp.contains("MotionPhoto=\"1\"", ignoreCase = true) ||
                xmp.contains("Item:Mime=\"video/mp4\"", ignoreCase = true)
    }

    // 返回可编辑的bitmap，但是可能解码会变慢
    fun decodeBitmapMutable(resolver: ContentResolver, imageUri: Uri): Bitmap {
        val source = ImageDecoder.createSource(resolver, imageUri)
        return ImageDecoder.decodeBitmap(source, object : ImageDecoder.OnHeaderDecodedListener {
            override fun onHeaderDecoded(
                decoder: ImageDecoder,
                info: ImageDecoder.ImageInfo,
                source: ImageDecoder.Source
            ) {
                decoder.isMutableRequired = true
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // 禁用 HARDWAVE
                LogUtils.d("ImageInfo: mimeType= ${info.mimeType}, size= ${info.size}, isAnimated= ${info.isAnimated}, colorSpace= ${info.colorSpace}")
            }

        })
    }

    fun loadBitmapFromUri(
        context: Context, uri: Uri,
        maxWidth: Int? = null,
        maxHeight: Int? = null
    ): Bitmap? {
        return try {
            val bounds = decodeBitmapBounds(context, uri) ?: return null

            val inSampleSize = calculateInSampleSize(
                bounds.width,
                bounds.height,
                maxWidth,
                maxHeight
            )

            decodeBitmap(context, uri, inSampleSize)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun decodeBitmap(
        context: Context,
        uri: Uri,
        inSampleSize: Int
    ): Bitmap? {
        val options = BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }

        ImageUtils.openInputStream(context, uri)?.use {
            return BitmapFactory.decodeStream(it, null, options)
        }

        return null
    }


    fun decodeBitmapBounds(
        context: Context,
        uri: Uri
    ): Resolution? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }

        ImageUtils.openInputStream(context, uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }

        if (options.outWidth <= 0 || options.outHeight <= 0) {
            return null
        }

        return Resolution(options.outWidth, options.outHeight)
    }

    fun openInputStream(
        context: Context,
        uri: Uri
    ): InputStream? {
        val uriString = uri.toString()

        return when {
            // ① assets（必须最先判断）
            uriString.startsWith(UriUtils.ASSET_PREFIX) -> {
                val assetPath = uriString.removePrefix(UriUtils.ASSET_PREFIX)
                context.assets.open(assetPath)
            }
            // ② content://
            uri.scheme == ContentResolver.SCHEME_CONTENT -> {
                context.contentResolver.openInputStream(uri)
            }
            // ③ file:// 普通文件
            uri.scheme == ContentResolver.SCHEME_FILE -> {
                uri.path?.let { FileInputStream(it) }
            }

            else -> null
        }
    }


    fun calculateInSampleSize(
        srcWidth: Int,
        srcHeight: Int,
        maxWidth: Int?,
        maxHeight: Int?
    ): Int {
        if (maxWidth == null && maxHeight == null) return 1

        val reqWidth = maxWidth ?: Int.MAX_VALUE
        val reqHeight = maxHeight ?: Int.MAX_VALUE

        var inSampleSize = 1
        val halfWidth = srcWidth / 2
        val halfHeight = srcHeight / 2

        while (
            halfWidth / inSampleSize >= reqWidth ||
            halfHeight / inSampleSize >= reqHeight
        ) {
            inSampleSize *= 2
        }

        return inSampleSize.coerceAtLeast(1)
    }
}
