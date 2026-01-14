package com.androidx

import android.app.RecoverableSecurityException
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File

/**
 * user author: didikee
 * create time: 2026/1/5 08:14
 * description: 新的uri辅助类
 */
object UriUtils {

    fun rename(
        contentResolver: ContentResolver,
        uri: Uri,
        newNameWithExtension: String
    ): Boolean {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, newNameWithExtension)
        }
        return try {
            val rows = contentResolver.update(
                uri,
                values,
                null,
                null
            )
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun delete(contentResolver: ContentResolver, uri: Uri): Boolean {
        try {
            val rowsDeleted = contentResolver.delete(uri, null, null)
            return rowsDeleted > 0
        } catch (e: SecurityException) {
            e.printStackTrace()
            // 如果是 Android 10+ 且文件来自外部共享目录，可能会抛出 RecoverableSecurityException
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && e is RecoverableSecurityException) {
                LogUtils.w("Delete failed: Security error")
            } else {
                LogUtils.e("Detele failed: Permission denied")
            }
        }
        return false
    }

    // 查询文件名
    fun queryDisplayName(contentResolver: ContentResolver, uri: Uri): String {
        val queryResult = when (uri.scheme) {
            ContentResolver.SCHEME_CONTENT -> {
                // content:// URI
                contentResolver.query(
                    uri,
                    arrayOf(MediaStore.MediaColumns.DISPLAY_NAME),
                    null,
                    null,
                    null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
                    } else ""
                } ?: ""
            }

            ContentResolver.SCHEME_FILE -> {
                // file:// URI
                File(uri.path ?: return "").name
            }

            else -> ""
        }
        return queryResult ?: ""
    }

}