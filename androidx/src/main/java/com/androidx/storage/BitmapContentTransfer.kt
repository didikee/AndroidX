package com.androidx.storage

import android.graphics.Bitmap
import com.androidx.ContentTransfer
import com.androidx.LogUtils
import com.androidx.media.MimeType
import com.androidx.utils.FileUtils
import java.io.FileOutputStream
import java.io.OutputStream

/**
 *
 * description:
 */
class BitmapContentTransfer(input: Bitmap, val mimeType: String, val quality:Int = 100) : ContentTransfer<Bitmap>(input) {
    override fun convertTo(outputStream: OutputStream): Long {
        if (MimeType.PNG.equals(mimeType)) {
            input.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        } else if (MimeType.WEBP.equals(mimeType)) {
            input.compress(Bitmap.CompressFormat.WEBP, quality, outputStream)
        } else {
            input.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        }
        if (outputStream is FileOutputStream) {
            val channelSize = outputStream.channel.size()
            LogUtils.d("RemoveMetadataTransfer channelSize: ${channelSize}, format: ${FileUtils.formatFileSize(channelSize)}")
            return channelSize
        }
        return 0
    }

    override fun release() {
        input.recycle()
    }
}