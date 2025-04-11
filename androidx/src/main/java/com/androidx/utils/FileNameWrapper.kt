package com.androidx.utils

/**
 *
 * description:
 */
class FileNameWrapper(val filename: String) {
    private var baseName: String
    private var extension: String

    init {
        val lastIndexOf = filename.lastIndexOf(".")
        var base = ""
        var ext = ""
        if (lastIndexOf != -1) {
            base = filename.substring(0, lastIndexOf)
            ext = filename.substring(lastIndexOf + 1)
        }
        baseName = base
        extension = ext
    }

    fun getFullFileName(): String {
        return "${baseName}.${extension}"
    }

    fun getBaseName(): String {
        return baseName
    }

    // extension 是不包含点(.)的
    fun getExtension(): String {
        return extension
    }
}