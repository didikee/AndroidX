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
    private val contentResolver: ContentResolver
    private var externalContentUri: Uri?
    private val order: String?
    private val selection: String?
    private val selectionArgs: Array<String>?
    private val targetFolderPath: String?
    private val targetMimeTypes: Array<String>?
    private val extraProjections: Array<String>?
    private val blockMimeTypes: Array<String>?


    init {
        contentResolver = builder.resolver
        externalContentUri = builder.contentUri
        order = builder.order
        selection = builder.selection
        selectionArgs = builder.selectionArgs
        targetMimeTypes = builder.targetMimeTypes
        blockMimeTypes = builder.blockMimeTypes
        targetFolderPath = builder.targetFolderPath
        extraProjections = builder.extraProjections
    }

    protected fun addExtraProjections(
        projections: java.util.ArrayList<String?>,
        extraProjections: java.util.ArrayList<String?>
    ) {
        for (projection in extraProjections) {
            if (!projections.contains(projection)) {
                projections.add(projection)
            }
        }
    }

    private fun <T> execute(dataHandler: DataHandler<T>): java.util.ArrayList<T> {
        val isAndroid10 = isAndroid10()
        val externalStorageLegacy = isExternalStorageLegacy
        // common projections
        val projections = java.util.ArrayList<String?>()
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
        val allExtraProjections = java.util.ArrayList<String?>()
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
        } else {
            // empty
        }
        //可选，增加额外的类型参数
        if (extraProjections != null) {
            allExtraProjections.addAll(Arrays.asList<String?>(*extraProjections))
        }
        addExtraProjections(projections, allExtraProjections)
        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                externalContentUri!!,
                projections.toTypedArray<String?>(),
                selection, selectionArgs, order
            )
        } catch (e: Exception) {
            e("MediaLoader query failed: " + e.getLocalizedMessage())
        }

        // 部分Rom 在没有权限或者异常时返回不为null的cursor，但是数量是0
        if (cursor == null || cursor.getCount() == 0) {
            return java.util.ArrayList<T>()
        }

        while (cursor.moveToNext()) {
            // 这些是公用的参数
            val id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID))
            val mimeType =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE))
            // 根据mimetype来过滤文件
            if (contains(blockMimeTypes, mimeType)) {
                continue
            }
            if (targetMimeTypes != null && targetMimeTypes.size > 0 && !contains(
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
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
                } else {
                    relativePath =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH))
                }
            } else {
                data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA))
            }
            // 文件夹过滤
            if (!isTargetFolder(data, relativePath, targetFolderPath)) {
                continue
            }

            // 现在全部改为uri来实现
            val uri = ContentUris.withAppendedId(externalContentUri!!, id.toLong())
            // 延迟解析的公共参数
            val displayName =
                cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME))
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
        if (content == null || content.length == 0) {
            return false
        }
        if (array != null && array.size > 0) {
            for (s in array) {
                if (content.equals(s, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    protected fun isTargetFolder(
        data: String?,
        relativePath: String?,
        folderPath: String?
    ): Boolean {
        if (TextUtils.isEmpty(folderPath)) {
            return true
        }
        // 文件夹过滤
        // android9:手机存储/DCIM/VideoMusicEditor/
        if (data != null && data.length > 0 && data.startsWith(
                Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + File.separator + folderPath
            )
        ) {
            return true
        }
        // android10:DCIM/VideoMusicEditor/adb.mp4
        if (relativePath != null && relativePath.length > 0 && relativePath.startsWith(folderPath!!)) {
            return true
        }
        return false
    }

    class Builder(var resolver: ContentResolver) {
        var contentUri: Uri? = null
        var order: String = ORDER_DATE_MODIFIED_DESC // 默认按照时间排序，最近的媒体在最前面，符合90%的使用场景
        var selection: String? = null
        var selectionArgs: Array<String>? = null
        var extraProjections: Array<String>? = null

        // 过滤
        var targetFolderPath: String? = null

        // 目标类型
        var targetMimeTypes: Array<String>? = null

        // 屏蔽的类型
        var blockMimeTypes: Array<String>? = null

        fun setContentUri(contentUri: Uri?): Builder {
            this.contentUri = contentUri
            return this
        }

        fun setOrder(order: String): Builder {
            this.order = order
            return this
        }

        fun setSelection(selection: String?, selectionArgs: Array<String>?): Builder {
            this.selection = selection
            this.selectionArgs = selectionArgs
            return this
        }

        fun setExtraProjections(extraProjections: Array<String>?): Builder {
            this.extraProjections = extraProjections
            return this
        }

        fun setTargetFolder(targetFolderPath: String?): Builder {
            this.targetFolderPath = targetFolderPath
            return this
        }

        fun ofImage(): Builder {
            this.contentUri = AndroidStorage.EXTERNAL_IMAGE_URI
            return this
        }

        fun ofVideo(): Builder {
            this.contentUri = AndroidStorage.EXTERNAL_VIDEO_URI
            return this
        }

        fun ofAudio(): Builder {
            this.contentUri = AndroidStorage.EXTERNAL_AUDIO_URI
            return this
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        fun ofDownload(): Builder {
            this.contentUri = AndroidStorage.EXTERNAL_DOWNLOAD_URI
            return this
        }

        fun setTargetMimeTypes(targetMimeTypes: Array<String>?): Builder {
            this.targetMimeTypes = targetMimeTypes
            return this
        }

        fun setBlockMimeTypes(blockMimeTypes: Array<String>?): Builder {
            this.blockMimeTypes = blockMimeTypes
            return this
        }

        fun get(): java.util.ArrayList<MediaItem> {
            return MediaLoader(this).execute<MediaItem>(MediaItemDataHandler())
        }

        /**
         * 以文件夹的形式获得所有的媒体文件
         *
         * @return
         */
        fun getFolders(defaultFolderName: String?): java.util.ArrayList<MediaFolder?> {
            val folderDataHandler = FolderDataHandler()
            val mediaFolders = MediaLoader(this).execute<MediaFolder?>(folderDataHandler)
            val allItems = folderDataHandler.getAllItems()
            if (allItems.size > 0) {
                //构造所有媒体文件的集合
                val allImagesFolder = MediaFolder()
                allImagesFolder.name =
                    (if (android.text.TextUtils.isEmpty(defaultFolderName)) "Recently" else defaultFolderName)!!
                allImagesFolder.path = ""
                allImagesFolder.items = allItems
                mediaFolders.add(0, allImagesFolder) //确保第一条是所有图片
            }
            return mediaFolders
        }

        val uris: ArrayList<Uri>
            /**
             * 获取所有的媒体文件，以uri的形式
             *
             * @return
             */
            get() = MediaLoader(this)
                .execute<Uri>(UriDataHandler())

        fun <T> get(dataHandler: DataHandler<T>): java.util.ArrayList<T> {
            return MediaLoader(this).execute<T>(dataHandler)
        }
    }


    companion object {
        // 排序，根据添加日期排序
        val ORDER_DATE_ADDED_DESC: String = MediaStore.MediaColumns.DATE_ADDED + " DESC"

        // 排序，根据最后修改的日期排序（从新到旧）
        val ORDER_DATE_MODIFIED_DESC: String = MediaStore.MediaColumns.DATE_MODIFIED + " DESC"
    }
}
