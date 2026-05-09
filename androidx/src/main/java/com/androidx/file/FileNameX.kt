package com.androidx.file

import android.text.TextUtils
import android.text.format.DateFormat

/**
 * user author: didikee
 * create time: 2026/4/20 17:22
 * description:
 */
object FileNameX {
    const val DEFAULT_FILENAME_TIME_STR = "yyyyMMdd_HHmmss"

    /**
     * 创建一个以时间戳为基础的文件名：20230712_134532.xxx
     *
     * @param extension
     * @return
     */
    fun createTimeFileName(
        extension: String,
        timeInMillis: Long = System.currentTimeMillis()
    ): String {
        return createFilename("", extension, timeInMillis)
    }

    /**
     * 创建一个文件名
     * 1. 没有旧文件名，那么用时间戳生成一个新的
     * 2. 校验旧文件名，重新拼接拓展名，返回新的文件名
     *
     * @param filename
     * @param extension
     * @return
     */
    fun createFilename(baseName: String, extension: String, timeInMillis: Long): String {
        var extension = extension
        var newBaseName = baseName
        if (TextUtils.isEmpty(baseName)) {
            newBaseName =
                DateFormat.format(DEFAULT_FILENAME_TIME_STR, timeInMillis).toString()
        }
        if (newBaseName.contains(".")) {
            val lastIndexOf = newBaseName.lastIndexOf(".")
            newBaseName = newBaseName.substring(0, lastIndexOf)
        }
        if (!TextUtils.isEmpty(extension) && !extension.startsWith(".")) {
            extension = "." + extension
        }
        if (extension.startsWith(".")) {
            return newBaseName + extension
        } else {
            return "${newBaseName}.${extension}"
        }
    }
}