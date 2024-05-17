package com.androidx.storage

import com.androidx.ContentTransfer
import com.androidx.LogUtils
import com.androidx.utils.FileUtils
import com.androidx.utils.IOUtils
import java.io.InputStream
import java.io.OutputStream

/**
 *
 * description:
 */
class StreamContentTransfer(input: InputStream) : ContentTransfer<InputStream>(input) {
    override fun convertTo(outputStream: OutputStream): Long {
        val transfer = IOUtils.transfer2(input, outputStream)
        LogUtils.d("StreamContentTransfer transfer: ${transfer}, format: ${FileUtils.formatFileSize(transfer)}")
        return transfer
    }

    override fun release() {
        IOUtils.close(input)
    }
}