package com.androidx.file

import android.content.Context
import java.io.File

/**
 * user author: didikee
 * create time: 2025/5/12 下午12:48
 * description:
 */
object FileX {
    fun getCacheSize(context: Context): Long {
        val cacheDir = context.cacheDir
        return getFolderSize(cacheDir)
    }

    // 递归计算文件夹大小
    fun getFolderSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            size += if (file.isDirectory) {
                getFolderSize(file)
            } else {
                file.length()
            }
        }
        return size
    }

    fun clearCache(context: Context): Long {
        return cleanDir(context.cacheDir)
    }

    // 会将输入的根目录一起删除掉
    fun deleteDir(dir: File?): Long {
        var deletedSize = 0L
        if (dir == null || !dir.exists()) return 0L

        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            deletedSize += if (file.isDirectory) {
                deleteDir(file)
            } else {
                val fileSize = file.length()
                if (file.delete()) fileSize else 0L
            }
        }
        // 删除自身目录（不计算大小）
        dir.delete()
        return deletedSize
    }

    // 不会删除清理输入的根目录
    fun cleanDir(dir: File?): Long {
        var deletedSize = 0L
        if (dir == null || !dir.exists()) return 0L

        val files = dir.listFiles() ?: return 0L
        for (file in files) {
            deletedSize += if (file.isDirectory) {
                cleanDir(file)
            } else {
                val fileSize = file.length()
                if (file.delete()) fileSize else 0L
            }
        }
        return deletedSize
    }

}