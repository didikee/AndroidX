package com.androidx.utils

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import com.androidx.media.MimeType
import java.io.File
import java.io.FileOutputStream

/**
 * user author: didikee
 * create time: 2025/5/20 上午8:50
 * description:
 */
object BitmapUtils {

    // 获取图片的旋转方向
    fun getRotation(contentResolver: ContentResolver, uri: Uri): Int {
        contentResolver.openInputStream(uri)?.use { input ->
            val exif = ExifInterface(input)
            return when (exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }
        return 0
    }


    /**
     * 旋转bitmap
     *
     * @param source 原图
     * @param angle  角度
     * @return 旋转后的图片
     */
    fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    /**
     * 缩放 Bitmap 到指定目标尺寸
     * @param srcBitmap 原始 Bitmap（可为null）
     * @param targetWidth 目标宽度（px）
     * @param targetHeight 目标高度（px）
     * @param keepAspectRatio 是否保持宽高比（true：等比例缩放，false：强制缩放到指定尺寸）
     * @return 缩放后的 Bitmap（null表示输入无效）
     */
    fun scaleBitmap(
        srcBitmap: Bitmap?,
        targetWidth: Int,
        targetHeight: Int,
        keepAspectRatio: Boolean = true
    ): Bitmap? {
        // 边界条件检查
        if (srcBitmap == null || srcBitmap.isRecycled) {
            return null
        }
        if (targetWidth <= 0 || targetHeight <= 0) {
            return null
        }
        if (srcBitmap.width == targetWidth && srcBitmap.height == targetHeight) {
            return srcBitmap // 尺寸一致，无需缩放
        }

        // 计算缩放比例
        val srcWidth = srcBitmap.width.toFloat()
        val srcHeight = srcBitmap.height.toFloat()
        val targetW = targetWidth.toFloat()
        val targetH = targetHeight.toFloat()

        var scaleX = targetW / srcWidth
        var scaleY = targetH / srcHeight

        if (keepAspectRatio) {
            // 等比例缩放：取最小缩放比例，保证图片完整显示在目标尺寸内
            val scale = minOf(scaleX, scaleY)
            scaleX = scale
            scaleY = scale
        }

        // 创建缩放矩阵
        val matrix = Matrix()
        matrix.setScale(scaleX, scaleY)

        // 执行缩放，使用高效的采样模式
        return try {
            Bitmap.createBitmap(
                srcBitmap,
                0,
                0,
                srcBitmap.width,
                srcBitmap.height,
                matrix,
                true // 开启过滤，让缩放后的图片更清晰
            )
        } catch (e: OutOfMemoryError) {
            // 内存不足时的降级处理
            null
        } catch (e: IllegalArgumentException) {
            // 尺寸参数异常时的处理
            null
        }
    }

    /**
     * 简化版：等比例缩放到指定最大宽度/高度（更常用）
     * @param srcBitmap 原始 Bitmap
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 缩放后的 Bitmap
     */
    fun scaleBitmapToMaxSize(
        srcBitmap: Bitmap?,
        maxWidth: Int,
        maxHeight: Int
    ): Bitmap? {
        return scaleBitmap(srcBitmap, maxWidth, maxHeight, true)
    }

    /**
     * 简化版：强制缩放到指定尺寸（不保持比例）
     * @param srcBitmap 原始 Bitmap
     * @param targetWidth 目标宽度
     * @param targetHeight 目标高度
     * @return 缩放后的 Bitmap
     */
    fun scaleBitmapToExactSize(
        srcBitmap: Bitmap?,
        targetWidth: Int,
        targetHeight: Int
    ): Bitmap? {
        return scaleBitmap(srcBitmap, targetWidth, targetHeight, false)
    }

    /**
     * 旋转并覆盖保存图片文件
     *
     * @param file 待旋转的图片文件
     * @param degrees 旋转角度
     * @return 是否成功
     */
    fun rotateImageFile(file: File, degrees: Int): Boolean {
        if (!file.exists()) return false

        return try {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            val rotated =
                Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)

            FileOutputStream(file).use { out ->
                rotated.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            bitmap.recycle()
            rotated.recycle()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun saveBitmapToFile(
        bitmap: Bitmap,
        outputFile: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ) {
        outputFile.outputStream().use { out ->
            bitmap.compress(format, quality, out)
        }
    }

    fun saveBitmapAutoFormat(
        bitmap: Bitmap,
        outputFile: File,
        quality: Int = 100
    ) {
        val format = guessCompressFormat(outputFile)
        outputFile.outputStream().use { out ->
            bitmap.compress(format, quality, out)
        }
    }

    /**
     * 用 sourceFile 覆盖 targetFile（图片）
     */
    fun replaceImageFile(
        sourceFile: File,
        targetFile: File
    ) {
        require(sourceFile.exists()) { "Source file not exists" }

        sourceFile.inputStream().use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    /**
     * 根据文件扩展名推断图片格式
     */
    private fun guessCompressFormat(file: File): Bitmap.CompressFormat {
        return when (file.extension.lowercase()) {
            "jpg", "jpeg" -> Bitmap.CompressFormat.JPEG
            "png" -> Bitmap.CompressFormat.PNG
            "webp" -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSLESS
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
            else -> Bitmap.CompressFormat.JPEG // 兜底
        }
    }

    /**
     * 获取 Bitmap.CompressFormat 对应的 MIME 类型
     */
    fun getMimeType(format: Bitmap.CompressFormat): String = when (format) {
        Bitmap.CompressFormat.JPEG -> MimeType.JPEG
        Bitmap.CompressFormat.PNG -> MimeType.PNG
        Bitmap.CompressFormat.WEBP -> MimeType.WEBP
        else -> MimeType.IMAGE
    }

    /**
     * 获取 Bitmap.CompressFormat 对应的文件扩展名
     */
    fun getExtension(format: Bitmap.CompressFormat): String = when (format) {
        Bitmap.CompressFormat.JPEG -> "jpg"
        Bitmap.CompressFormat.PNG -> "png"
        Bitmap.CompressFormat.WEBP -> "webp"
        else -> "jpg"
    }

}