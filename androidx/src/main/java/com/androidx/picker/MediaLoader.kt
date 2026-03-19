package com.androidx.picker

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.RequiresApi
import com.androidx.AndroidStorage
import com.androidx.AndroidUtils.getOSVersion
import com.androidx.AndroidUtils.isAndroid10
import com.androidx.AndroidUtils.isExternalStorageLegacy
import com.androidx.LogUtils.e
import java.io.File
import java.util.Arrays

/**
 * user author: didikee
 * create time: 5/26/21 4:17 下午
 * description:
 */
class MediaLoader private constructor(builder: Builder) {

    private val contentResolver: ContentResolver = builder.resolver
    private var externalContentUri: Uri? = builder.contentUri
    private val order: String = builder.order
    private val selection: String? = builder.selection
    private val selectionArgs: Array<String>? = builder.selectionArgs
    private val targetFolderPath: String? = builder.targetFolderPath
    private val targetMimeTypes: Array<String>? = builder.targetMimeTypes
    private val extraProjections: Array<String>? = builder.extraProjections
    private val blockMimeTypes: Array<String>? = builder.blockMimeTypes

    private fun addExtraProjections(
        projections: ArrayList<String>,
        extraProjections: ArrayList<String>
    ) {
        for (projection in extraProjections) {
            if (!projections.contains(projection)) {
                projections.add(projection)
            }
        }
    }

    private fun <T> execute(dataHandler: DataHandler<T>): ArrayList<T> {
        val isAndroid10 = isAndroid10()
        val externalStorageLegacy = isExternalStorageLegacy
        // common projections
        val projections = ArrayList<String>()
        projections.add(MediaStore.MediaColumns._ID)
        projections.add(MediaStore.MediaColumns.DISPLAY_NAME)
        projections.add(MediaStore.MediaColumns.SIZE)
        projections.add(MediaStore.MediaColumns.MIME_TYPE)
        projections.add(MediaStore.MediaColumns.DATE_ADDED)
        projections.add(MediaStore.MediaColumns.DATE_MODIFIED)
        if (isAndroid10) {
            if (externalStorageLegacy) {
                projections.add(MediaStore.MediaColumns.DATA)
            } else {
                // 相对路径   /storage/0000-0000/DCIM/Vacation/IMG1024.JPG} would have a path of {@code DCIM/Vacation/}.
                projections.add(MediaStore.MediaColumns.RELATIVE_PATH)
            }
        } else {
            // 真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            projections.add(MediaStore.MediaColumns.DATA)
        }
        if (externalContentUri == null) {
            externalContentUri = AndroidStorage.EXTERNAL_IMAGE_URI
        }
        val allExtraProjections = ArrayList<String>()
        // 根据特定的类型添加特定类型的参数
        if (AndroidStorage.EXTERNAL_IMAGE_URI.toString() == externalContentUri.toString()) {
            allExtraProjections.add(MediaStore.MediaColumns.WIDTH)
            allExtraProjections.add(MediaStore.MediaColumns.HEIGHT)
            if (getOSVersion() >= 30) {
                allExtraProjections.add(MediaStore.Images.Media.XMP)
            }
        } else if (AndroidStorage.EXTERNAL_VIDEO_URI.toString() == externalContentUri.toString()) {
            allExtraProjections.add(MediaStore.MediaColumns.WIDTH)
            allExtraProjections.add(MediaStore.MediaColumns.HEIGHT)
            if (isAndroid10) {
                allExtraProjections.add(MediaStore.Video.Media.DURATION)
            }
        } else if (AndroidStorage.EXTERNAL_AUDIO_URI.toString() == externalContentUri.toString()) {
            if (isAndroid10) {
                allExtraProjections.add(MediaStore.Audio.Media.DURATION)
            }
        }
        //可选，增加额外的类型参数
        if (extraProjections != null) {
            allExtraProjections.addAll(Arrays.asList(*extraProjections))
        }
        addExtraProjections(projections, allExtraProjections)
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                externalContentUri!!,
                projections.toTypedArray(),
                selection, selectionArgs, order
            )
        } catch (e: Exception) {
            e("MediaLoader query failed: ${e.localizedMessage}")
        }

        // 部分Rom 在没有权限或者异常时返回不为null的cursor，但是数量是0
        if (cursor == null || cursor.count == 0) {
            return ArrayList()
        }

        while (cursor.moveToNext()) {
            // 这些是公用的参数
            val id = cursor.getStringSafe(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            val mimeType =
                cursor.getStringSafe(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
            // 根据mimetype来过滤文件
            if (contains(blockMimeTypes, mimeType)) {
                continue
            }
            if (targetMimeTypes != null && targetMimeTypes.isNotEmpty() && !contains(
                    targetMimeTypes,
                    mimeType
                )
            ) {
                continue
            }
            var data = ""
            var relativePath = ""
            if (isAndroid10) {
                if (externalStorageLegacy) {
                    data =
                        cursor.getStringSafe(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                } else {
                    relativePath =
                        cursor.getStringSafe(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH))
                }
            } else {
                data =
                    cursor.getStringSafe(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            }
            // 文件夹过滤
            if (!isTargetFolder(data, relativePath, targetFolderPath)) {
                continue
            }

            // 现在全部改为uri来实现
            val uri = ContentUris.withAppendedId(externalContentUri!!, id.toLong())
            // 延迟解析的公共参数
            val displayName =
                cursor.getStringSafe(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
            val size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE))
            val dateAdded =
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED))
            val dateModified =
                cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED))

            dataHandler.handle(
                cursor,
                allExtraProjections,
                uri,
                displayName,
                mimeType,
                size,
                dateAdded,
                dateModified,
                data,
                relativePath
            )
        }
        return dataHandler.getDataResult()
    }

    private fun contains(array: Array<String>?, content: String?): Boolean {
        if (content.isNullOrEmpty()) {
            return false
        }
        if (array != null && array.isNotEmpty()) {
            for (s in array) {
                if (content.equals(s, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    private fun isTargetFolder(data: String?, relativePath: String?, folderPath: String?): Boolean {
        if (TextUtils.isEmpty(folderPath)) {
            return true
        }
        // 文件夹过滤
        // android9:手机存储/DCIM/VideoMusicEditor/
        if (!data.isNullOrEmpty() && data.startsWith(
                Environment.getExternalStorageDirectory().absolutePath + File.separator + folderPath
            )
        ) {
            return true
        }
        // android10:DCIM/VideoMusicEditor/adb.mp4
        if (!relativePath.isNullOrEmpty() && relativePath.startsWith(folderPath!!)) {
            return true
        }
        return false
    }

    class Builder(
        val resolver: ContentResolver
    ) {
        internal var contentUri: Uri? = null
        internal var order: String = ORDER_DATE_MODIFIED_DESC // 默认按照时间排序，最近的媒体在最前面，符合90%的使用场景
        internal var selection: String? = null
        internal var selectionArgs: Array<String>? = null
        internal var extraProjections: Array<String>? = null
        internal var targetFolderPath: String? = null
        internal var targetMimeTypes: Array<String>? = null
        internal var blockMimeTypes: Array<String>? = null

        fun setContentUri(contentUri: Uri?): Builder = apply { this.contentUri = contentUri }

        fun setOrder(order: String): Builder = apply { this.order = order }

        fun setSelection(selection: String?, selectionArgs: Array<String>?): Builder = apply {
            this.selection = selection
            this.selectionArgs = selectionArgs
        }

        fun setExtraProjections(extraProjections: Array<String>?): Builder = apply {
            this.extraProjections = extraProjections
        }

        fun setTargetFolder(targetFolderPath: String?): Builder = apply {
            this.targetFolderPath = targetFolderPath
        }

        fun ofImage(): Builder = apply { contentUri = AndroidStorage.EXTERNAL_IMAGE_URI }

        fun ofVideo(): Builder = apply { contentUri = AndroidStorage.EXTERNAL_VIDEO_URI }

        fun ofAudio(): Builder = apply { contentUri = AndroidStorage.EXTERNAL_AUDIO_URI }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        fun ofDownload(): Builder = apply { contentUri = AndroidStorage.EXTERNAL_DOWNLOAD_URI }

        fun setTargetMimeTypes(targetMimeTypes: Array<String>?): Builder = apply {
            this.targetMimeTypes = targetMimeTypes
        }

        fun setBlockMimeTypes(blockMimeTypes: Array<String>?): Builder = apply {
            this.blockMimeTypes = blockMimeTypes
        }

        fun get(): ArrayList<MediaItem> = MediaLoader(this).execute(MediaItemDataHandler())

        /**
         * 以文件夹的形式获得所有的媒体文件
         */
        fun getFolders(defaultFolderName: String?): ArrayList<MediaFolder> {
            val folderDataHandler = FolderDataHandler()
            val mediaFolders = MediaLoader(this).execute(folderDataHandler)
            val allItems = folderDataHandler.allItems
            if (allItems.isNotEmpty()) {
                //构造所有媒体文件的集合
                val allImagesFolder = MediaFolder()
                allImagesFolder.name =
                    if (TextUtils.isEmpty(defaultFolderName)) "Recently" else defaultFolderName!!
                allImagesFolder.path = ""
                allImagesFolder.items = allItems
                mediaFolders.add(0, allImagesFolder) //确保第一条是所有图片
            }
            return mediaFolders
        }

        /**
         * 获取所有的媒体文件，以uri的形式
         */
        fun getUris(): ArrayList<Uri> = MediaLoader(this).execute(UriDataHandler())

        fun <T> get(dataHandler: DataHandler<T>): ArrayList<T> =
            MediaLoader(this).execute(dataHandler)
    }

    companion object {
        // 排序，根据添加日期排序
        const val ORDER_DATE_ADDED_DESC: String = MediaStore.MediaColumns.DATE_ADDED + " DESC"

        // 排序，根据最后修改的日期排序（从新到旧）
        const val ORDER_DATE_MODIFIED_DESC: String = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"

        private fun Cursor.getStringSafe(columnIndex: Int): String {
            return getString(columnIndex) ?: ""
        }
    }
}
