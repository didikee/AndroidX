package com.androidx.file

/**
 *
 * description:
 */
class FileNameWrapper(val filename: String) {
    var baseName: String = ""
    var extension: String = ""

    init {
        val pair = getFileNameAndExtension(filename)
        baseName = pair.first
        extension = pair.second
    }


    // 是否是一个标准的文件名
    fun isValid(): Boolean {
        return baseName.isNotEmpty() && extension.isNotEmpty()
    }

    fun getFullFileName(): String {
        return "${baseName}.${extension}"
    }

    // extension 是不包含点(.)的
    fun getFileExtension(defaultExt: String = ""): String {
        if (extension.isEmpty()) {
            return defaultExt
        }
        return extension
    }


    private fun getFileNameAndExtension(fileName: String): Pair<String, String> {
        if (fileName.isBlank() || fileName == "." || fileName == ".." || fileName.endsWith(".")) {
            return "" to ""
        }

        val lastDotIndex = fileName.lastIndexOf('.')

        return if (lastDotIndex <= 0) {
            fileName to "" // 没有扩展名 或 以 "." 开头
        } else {
            fileName.substring(0, lastDotIndex) to fileName.substring(lastDotIndex + 1)
        }
    }

    override fun toString(): String {
        return "FilenameWrapper(filename='$filename', nameWithoutExtension='$baseName', extension='$extension')"
    }
}