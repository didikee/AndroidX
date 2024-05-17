package com.androidx

import java.io.OutputStream

/**
 *
 * description:
 */
abstract class ContentTransfer<T>(protected val input: T) {
    abstract fun convertTo(outputStream: OutputStream): Long
    abstract fun release()
}