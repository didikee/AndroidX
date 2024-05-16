package com.androidx.storage

import android.content.ContentValues
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.androidx.LogUtils
import com.androidx.StorageSaveUtils
import com.androidx.media.MimeType
import com.androidx.utils.UriUtils

class ContentValuesHelper private constructor(
    private val folderPath: String,
    private val filename: String,
    private val mimeType: String,
    // common base
    private var fileLength: Long = 0L,
    private var dateAdded: Long = 0L,
    private var dateModified: Long = 0L,
    // 仅对Q以后的版本有效
    private var dateToken: Long = 0L,
    // image & video
    private var width: Int = 0,
    private var height: Int = 0,
    private var orientation: Int = 0,
    // video & audio
    private var duration: Long = 0L
) {
    companion object {
        fun builder(folderPath: String, filename: String, mimeType: String) =
            ContentValuesHelper(folderPath, filename, mimeType)
    }

    fun fileLength(fileLength: Long) = apply { this.fileLength = fileLength }
    fun dateAdded(dateAdded: Long) = apply { this.dateAdded = dateAdded }
    fun dateModified(dateModified: Long) = apply { this.dateModified = dateModified }
    fun dateToken(dateToken: Long) = apply { this.dateToken = dateToken }
    fun width(width: Int) = apply { this.width = width }
    fun height(height: Int) = apply { this.height = height }
    fun orientation(orientation: Int) = apply { this.orientation = orientation }
    fun duration(duration: Long) = apply { this.duration = duration }

    fun withImage(width: Int, height: Int, rotateDegress: Int) {
        apply {
            this.width = width
            this.height = height
            this.orientation = rotateDegress
        }
    }

    fun withVideo(width: Int, height: Int, duration: Long) {
        apply {
            this.width = width
            this.height = height
            this.duration = duration
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun withVideo(width: Int, height: Int, duration: Long, rotateDegress: Int) {
        apply {
            this.width = width
            this.height = height
            this.duration = duration
            this.orientation = rotateDegress
        }
    }

    fun withAudio(duration: Long) {
        apply {
            this.duration = duration
        }
    }

    fun build(): ContentValues {
        val values = ContentValues()
        // common base
        values.put(MediaStore.MediaColumns.TITLE, filename)
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)

        val currentTimeMillis = System.currentTimeMillis()
        if (dateAdded <= 0) {
            dateAdded = currentTimeMillis
        }
        values.put(MediaStore.MediaColumns.DATE_ADDED, dateAdded / 1000)
        if (dateModified <= 0) {
            dateModified = currentTimeMillis
        }
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, dateModified / 1000)
        // values.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000);

        if (fileLength > 0) {
            values.put(MediaStore.MediaColumns.SIZE, fileLength)
        }
        if (dateToken <= 0) {
            dateToken = currentTimeMillis
        }
        // data_taken 在android10以下只有image 和 video有，audio是没有的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.DATE_TAKEN, dateToken)
        } else {
            if (MimeType.isImage(mimeType) || MimeType.isVideo(mimeType)) {
                values.put(UriUtils.DATE_TAKEN, dateToken)
            } else {
                // audio类型暂不支持
                // 其他类型还未测试
            }
        }
        if (MimeType.isImage(mimeType)) {
            values.put(MediaStore.MediaColumns.WIDTH, width)
            values.put(MediaStore.MediaColumns.HEIGHT, height)
            values.put(MediaStore.MediaColumns.ORIENTATION, orientation)
        } else if (MimeType.isVideo(mimeType)) {
            values.put(MediaStore.MediaColumns.WIDTH, width)
            values.put(MediaStore.MediaColumns.HEIGHT, height)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // 在api29开始视频支持了旋转参数
                values.put(MediaStore.MediaColumns.ORIENTATION, orientation)
            }
            values.put(MediaStore.MediaColumns.DURATION, duration)
        } else if (MimeType.isAudio(mimeType)) {
            values.put(MediaStore.MediaColumns.DURATION, duration)
        } else {
            LogUtils.e("ContentValuesHelper build unSupport mimetype: " + mimeType)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath)
            values.put(MediaStore.MediaColumns.IS_PENDING, true)
        } else {
            val data = StorageSaveUtils.getDataPath(folderPath, filename)
            values.put(MediaStore.MediaColumns.DATA, data)
        }
        return values
    }
}
