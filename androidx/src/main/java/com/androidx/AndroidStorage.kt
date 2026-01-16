package com.androidx

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import androidx.annotation.WorkerThread
import com.androidx.MediaStoreUtils.EXTERNAL_AUDIO_PRIMARY_URI
import com.androidx.MediaStoreUtils.EXTERNAL_IMAGE_PRIMARY_URI
import com.androidx.MediaStoreUtils.EXTERNAL_VIDEO_PRIMARY_URI
import com.androidx.media.DirectoryFiles
import com.androidx.media.MimeType
import com.androidx.media.StandardDirectory
import com.androidx.utils.FileUtils
import com.androidx.utils.IOUtils
import com.androidx.utils.MediaUtils
import com.androidx.utils.UriUtils
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.Arrays
import kotlin.math.max

/**
 * user author: didikee
 * create time: 4/15/21 3:02 PM
 * description: 文件存储类，主要目的是方便存储安卓中常见的媒体类型
 * 照片：DCIM,Picture
 * 视频: DCIM,MOVIES
 * 音乐: Audios,Rington
 * 文件: Document,Download
 */
object AndroidStorage {
    // 当读取的时候就读取所有卷的数据，但是储存的时候只能往主要卷存数据
    @JvmField
    val EXTERNAL_IMAGE_URI: Uri = MediaStoreUtils.EXTERNAL_IMAGE_URI

    @JvmField
    val EXTERNAL_VIDEO_URI: Uri = MediaStoreUtils.EXTERNAL_VIDEO_URI

    @JvmField
    val EXTERNAL_AUDIO_URI: Uri = MediaStoreUtils.EXTERNAL_AUDIO_URI

    @JvmField
    val EXTERNAL_DOWNLOAD_URI: Uri =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Downloads.EXTERNAL_CONTENT_URI else Uri.parse(
            ""
        )
    private val STANDARD_DIRECTORIES = ArrayList<String>()

    private const val TAG = "StorageUtils"


    /**
     * 保存bitmap到存储目录,兼容androidx
     *
     * @param resolver   ContentResolver，参见 [Context.getContentResolver]
     * @param bitmap     图片
     * @param folderPath 相对路径，可以通过[AndroidStorage.getFolderPath]
     * 其他详情参见 [AndroidStorage.getCompatPath]
     * @param filename   文件名，注意拓展名要和bitmap的格式相匹配
     * @return 存储成功的uri，如果失败了则返回null
     */
    @JvmStatic
    @JvmOverloads
    fun saveBitmap(
        resolver: ContentResolver?,
        bitmap: Bitmap?,
        folderPath: String,
        filename: String,
        quality: Int = 100
    ): Uri? {
        if (resolver == null || bitmap == null) {
            LogUtils.e("StorageUtils saveBitmap() contentResolver or bitmap is null.")
            return null
        }
        if (TextUtils.isEmpty(filename)) {
            LogUtils.e("StorageUtils saveBitmap() fileName is empty.")
            return null
        }
        if (TextUtils.isEmpty(folderPath)) {
            LogUtils.e("StorageUtils saveBitmap() folderPath is empty.")
            return null
        }
        //        final Bitmap saveBitmap;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (bitmap.getConfig() == Bitmap.Config.HARDWARE)) {
//            saveBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
//        }else {
//
//        }
        var mimeType = MimeType.getMimeTypeFromFilename(filename)
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.w("saveBitmap: getMimeTypeFromFilename failed, file name is $filename")
            mimeType = MimeType.PNG
        }
        val contentValues = createImageContentValues(
            folderPath,
            filename,
            mimeType,
            bitmap.width,
            bitmap.height,
            0,
            0
        )
        val destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        if (destUri != null) {
            var outputStream: OutputStream? = null
            try {
                outputStream = resolver.openOutputStream(destUri)
                if (outputStream != null) {
                    val compress =
                        bitmap.compress(getCompressFormat(mimeType), quality, outputStream)
                    outputStream.flush()
                    if (compress) {
                        if (outputStream is FileOutputStream) {
                            val size = outputStream.channel.size()
                            updateUriFileLength(resolver, destUri, contentValues, size)
                        }
                        clearPendingStates(resolver, destUri, contentValues)
                        return destUri
                    } else {
                        delete(resolver, destUri)
                        return null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                IOUtils.close(outputStream)
            }
        }

        return null
    }

    @JvmStatic
    @Deprecated("")
    fun saveImage(
        resolver: ContentResolver?,
        inputStream: InputStream?,
        folderPath: String,
        filename: String,
        width: Int,
        height: Int,
        rotate: Int
    ): Uri? {
        val contentValues = createImageContentValues(
            folderPath, filename, "",  /*自动处理*/
            width, height, rotate, 0
        )
        return save(resolver, contentValues, inputStream, EXTERNAL_IMAGE_PRIMARY_URI)
    }

    @JvmStatic
    fun saveImage(
        resolver: ContentResolver?,
        inputStream: InputStream?,
        folderPath: String,
        filename: String,
        width: Int,
        height: Int,
        rotate: Int,
        fileLength: Long = 0,
        mimeType: String = ""  /*为空时自动处理*/
    ): Uri? {
        val contentValues = createImageContentValues(
            folderPath, filename, mimeType,
            width, height, rotate,
            Math.max(0L, fileLength)
        )
        return save(resolver, contentValues, inputStream, EXTERNAL_IMAGE_PRIMARY_URI)
    }

    @JvmStatic
    fun saveImage(
        resolver: ContentResolver,
        contentValues: ContentValues,
        contentTransfer: ContentTransfer<*>
    ): Uri? {
        val insertUri = resolver.insert(EXTERNAL_IMAGE_PRIMARY_URI, contentValues)
        var outputStream: OutputStream? = null
        try {
            outputStream = resolver.openOutputStream(insertUri!!)
            val transfer = contentTransfer.convertTo(outputStream!!)
            if (transfer > 0) {
                updateUriFileLength(
                    resolver,
                    insertUri, contentValues, transfer
                )
                clearPendingStates(resolver, insertUri, contentValues)
                return insertUri
            } else {
                delete(resolver, insertUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            contentTransfer.release()
            IOUtils.close(outputStream)
        }
        return null
    }

    @JvmStatic
    fun saveAudio(
        resolver: ContentResolver?,
        inputStream: InputStream?,
        folderPath: String,
        filename: String
    ): Uri? {
        val contentValues = createAudioContentValues(folderPath, filename, "",  /*自动处理*/0, 0)
        return save(resolver, contentValues, inputStream, EXTERNAL_AUDIO_PRIMARY_URI)
    }

    @JvmStatic
    fun saveVideo(
        resolver: ContentResolver?,
        inputStream: InputStream?,
        folderPath: String,
        filename: String,
        width: Int,
        height: Int,
        rotate: Int,
        duration: Long,
        fileLength: Long
    ): Uri? {
        val contentValues = createVideoContentValues(
            folderPath, filename, "",  /*自动处理*/
            width, height, rotate, duration, fileLength
        )
        return save(resolver, contentValues, inputStream, EXTERNAL_VIDEO_PRIMARY_URI)
    }

    fun saveTo(
        resolver: ContentResolver?,
        contentValues: ContentValues?,
        inputStream: InputStream?,
        contentUri: Uri?
    ): Uri? {
        return save(resolver, contentValues, inputStream, contentUri)
    }

    /**
     * 保存音频，文件等
     *
     * @param resolver
     * @param contentValues
     * @param inputStream
     * @param contentUri
     * @return
     */
    private fun save(
        resolver: ContentResolver?,
        contentValues: ContentValues?,
        inputStream: InputStream?,
        contentUri: Uri?
    ): Uri? {
        if (resolver == null || contentUri == null || contentValues == null || inputStream == null) {
            LogUtils.e("StorageUtils save() contentResolver or bitmap is null.")
            return null
        }
        val insertUri = resolver.insert(contentUri, contentValues)
        var outputStream: OutputStream? = null
        try {
            outputStream = resolver.openOutputStream(insertUri!!)
            val transfer = IOUtils.transfer2(inputStream, outputStream)
            if (transfer > 0) {
                updateFileLengthAndClearPending(
                    resolver,
                    insertUri, contentValues, transfer
                )
                return insertUri
            } else {
                delete(resolver, insertUri)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            IOUtils.close(inputStream)
            IOUtils.close(outputStream)
        }
        return null
    }

    /**
     * 清楚isPending标志
     * isPending为true时，MediaStore API会忽略它。表现形式就是相册里看不到。
     * 知道isPending 为false时才可见。
     *
     *
     * 注意完成写入uri操作后一定要清楚isPending标志位，否则即使写入成功，MediaStore依然是不可见的。
     */
    @JvmStatic
    fun clearPendingStates(resolver: ContentResolver, uri: Uri, contentValues: ContentValues) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && contentValues.getAsBoolean(MediaStore.MediaColumns.IS_PENDING)) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, false)
            resolver.update(uri, contentValues, null, null)
        }
    }

    /**
     * 更新uri的文件大小
     *
     * @param resolver
     * @param uri
     * @param contentValues
     * @param fileLength
     */
    @JvmStatic
    fun updateUriFileLength(
        resolver: ContentResolver,
        uri: Uri,
        contentValues: ContentValues,
        fileLength: Long
    ) {
        val asLong = contentValues.getAsLong(MediaStore.MediaColumns.SIZE)
        val oldFileLength = asLong ?: 0
        if (fileLength > 0 && oldFileLength != fileLength) {
            contentValues.put(MediaStore.MediaColumns.SIZE, fileLength)
            resolver.update(uri, contentValues, null, null)
        }
    }

    @JvmStatic
    fun updateFileLengthAndClearPending(
        resolver: ContentResolver,
        uri: Uri,
        contentValues: ContentValues,
        fileLength: Long
    ) {
        val asLong = contentValues.getAsLong(MediaStore.MediaColumns.SIZE)
        val oldFileLength = asLong ?: 0
        val clearPending =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && contentValues.getAsBoolean(
                MediaStore.MediaColumns.IS_PENDING
            )
        // 清除原始contentValues中的所有数据
        contentValues.clear()
        if (fileLength > 0 && oldFileLength != fileLength) {
            contentValues.put(MediaStore.MediaColumns.SIZE, fileLength)
        }
        if (clearPending) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, false)
        }
        if (contentValues.size() > 0) {
            resolver.update(uri, contentValues, null, null)
        } else {
            LogUtils.w("updateFileLengthAndClearPending cancel: fileLength == 0 or clearPending = false")
        }
    }


    /**
     * 删除uri
     *
     * @param resolver
     * @param uri
     * @return 返回uri在数据库中对应的行位置,-1表示删除失败
     */
    @JvmStatic
    fun delete(resolver: ContentResolver?, uri: Uri?): Boolean {
        var row = -1
        try {
            if (resolver != null && uri != null) {
                row = resolver.delete(uri, null, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return row > -1
    }

    @JvmStatic
    fun createImageContentValues(
        folderPath: String,
        filename: String,
        mimeType: String,
        width: Int,
        height: Int,
        rotate: Int,
        fileLength: Long
    ): ContentValues {
        val compatPath = getCompatPath(folderPath, filename)
        val contentValues = createBaseValues(
            compatPath, filename, getMimeType(filename, mimeType, MimeType.JPEG),
            max(0.0, fileLength.toDouble()).toLong()
        )
        if (width > 0 && height > 0) {
            contentValues.put(MediaStore.MediaColumns.WIDTH, width)
            contentValues.put(MediaStore.MediaColumns.HEIGHT, height)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.ORIENTATION, rotate)
        }
        return contentValues
    }

    @JvmStatic
    fun createAudioContentValues(
        folderPath: String, filename: String,
        mimeType: String, duration: Long, fileLength: Long
    ): ContentValues {
        val compatPath = getCompatPath(folderPath, filename)
        val contentValues = createBaseValues(
            compatPath, filename, getMimeType(filename, mimeType, MimeType.MP3),
            max(0.0, fileLength.toDouble()).toLong()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && duration > 0) {
            contentValues.put(MediaStore.Audio.AudioColumns.DURATION, duration)
        }
        return contentValues
    }

    @JvmStatic
    fun createVideoContentValues(
        folderPath: String,
        filename: String,
        mimeType: String,
        width: Int,
        height: Int,
        rotate: Int,
        duration: Long,
        fileLength: Long
    ): ContentValues {
        val compatPath = getCompatPath(folderPath, filename)
        val contentValues = createBaseValues(
            compatPath, filename, getMimeType(filename, mimeType, MimeType.MP4),
            max(0.0, fileLength.toDouble()).toLong()
        )
        contentValues.put(MediaStore.MediaColumns.WIDTH, width)
        contentValues.put(MediaStore.MediaColumns.HEIGHT, height)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 在api29开始视频支持了旋转参数
            contentValues.put(MediaStore.MediaColumns.ORIENTATION, rotate)
            contentValues.put(MediaStore.MediaColumns.DURATION, duration)
        }
        return contentValues
    }

    @JvmStatic
    fun createBaseValues(
        compatPath: String?,
        filename: String?,
        mimeType: String?,
        fileLength: Long
    ): ContentValues {
        val values = ContentValues()
        values.put(MediaStore.MediaColumns.TITLE, filename)
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000)
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
        if (fileLength > 0) {
            values.put(MediaStore.MediaColumns.SIZE, fileLength)
        }
        // data_taken 在android10以下只有image 和 video有，audio是没有的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis())
        } else {
            if (MimeType.isImage(mimeType) || MimeType.isVideo(mimeType)) {
                //TODO audio类型暂不支持,其他的还未测试
                values.put(UriUtils.DATE_TAKEN, System.currentTimeMillis())
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, compatPath)
            values.put(MediaStore.MediaColumns.IS_PENDING, true)
        } else {
            values.put(MediaStore.MediaColumns.DATA, compatPath)
        }
        return values
    }

    /**
     * 相对路径
     * 对于低于安卓10：手机存储/相对路径.../文件名
     * 高于等于安卓10：相对路径.../文件名
     * 例如，想保存一张照片到 手机存储/Picture/MyFolder/my_photo.jpg
     * 对于低于安卓10：/storage/emluanm0/Picture/MyFolder/my_photo.jpg
     * 高于等于安卓10：Picture/MyFolder/my_photo.jpg
     *
     * @param folderPath 相对路径:Picture/MyFolder
     * Environment.DIRECTORY_PICTURES + File.separator + "folder";
     * Environment.DIRECTORY_DCIM + File.separator + "folder";
     * @param filename   文件名
     * @return 适用于安卓所有的路径
     */
    @JvmStatic
    fun getCompatPath(folderPath: String, filename: String): String {
        return getCompatPath(folderPath, filename, Build.VERSION.SDK_INT)
    }

    @JvmStatic
    fun getCompatPath(folderPath: String, filename: String, sdkVersion: Int): String {
        if (TextUtils.isEmpty(folderPath) || TextUtils.isEmpty(filename)) {
            // 默认返回
            return ""
        }
        val startSeparator = folderPath.startsWith(File.separator)
        val endSeparator = folderPath.endsWith(File.separator)
        var subRelativePath = folderPath
        if (startSeparator || endSeparator) {
            subRelativePath = folderPath.substring(
                if (startSeparator) 1 else 0,
                if (endSeparator) folderPath.length - 1 else folderPath.length
            )
        }
        val compatPath: String
        if (sdkVersion >= Build.VERSION_CODES.Q) {
            compatPath = subRelativePath
        } else {
            val externalStorageDirectory = Environment.getExternalStorageDirectory()
            val dir = File(
                (externalStorageDirectory.absolutePath + File.separator
                        + subRelativePath + File.separator)
            )
            if (!dir.exists()) {
                val mkdirs = dir.mkdirs()
            }
            compatPath = (externalStorageDirectory.absolutePath + File.separator
                    + subRelativePath + File.separator + filename)
        }
        return compatPath
    }

    /**
     * 获取mimetype
     *
     * @param filename        文件名，带拓展名
     * @param mimeType        类型
     * @param defaultMimeType 默认类型
     * @return
     */
    private fun getMimeType(filename: String, mimeType: String, defaultMimeType: String): String {
        if (TextUtils.isEmpty(mimeType)) {
            val mimeTypeFromFilename = MimeType.getMimeTypeFromFilename(filename)
            return if (TextUtils.isEmpty(mimeTypeFromFilename)) {
                defaultMimeType
            } else {
                mimeTypeFromFilename
            }
        } else {
            return mimeType
        }
    }

    private fun getCompressFormat(mimeType: String): Bitmap.CompressFormat {
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.e("getCompressFormat: image mime type is empty.")
            return Bitmap.CompressFormat.PNG
        }
        if (mimeType.equals(MimeType.PNG, ignoreCase = true)) {
            return Bitmap.CompressFormat.PNG
        }
        if (mimeType.equals(MimeType.JPEG, ignoreCase = true)) {
            return Bitmap.CompressFormat.PNG
        }
        if (mimeType.equals(MimeType.WEBP, ignoreCase = true)) {
            return Bitmap.CompressFormat.PNG
        }
        LogUtils.e("getCompressFormat: unSupport image mime type= $mimeType")
        return Bitmap.CompressFormat.PNG
    }

    @JvmStatic
    val isAboveVersionQ: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    /**
     * 获取类似.../{Picture/APP_CUSTOM_FOLDER}/...结构的目录
     *
     * @param standardDir 一级目录，不能为空。为空时Q以上可以运行，低版本会出问题
     * @param customDir   二级目录，可以为空
     * @return .../{STANDARD_DIRECTORIES/customDir}/...
     */
    @JvmStatic
    @Deprecated("推荐使用 {@link #getFolderPath(StandardDirectory, String)}")
    fun getFolderPath(standardDir: String, customDir: String?): String {
        if (!isStandardDirectory(standardDir)) {
            throw UnsupportedOperationException("Cannot use file directories other than STANDARD_DIRECTORIES")
        }
        if (TextUtils.isEmpty(customDir)) {
            return standardDir
        }
        return standardDir + File.separator + customDir
    }

    /**
     * 获取类似.../{Picture/APP_CUSTOM_FOLDER}/...结构的目录
     *
     * @param standardDir 一级目录，不能为空。为空时Q以上可以运行，低版本会出问题
     * @param customDir   二级目录，可以为空
     * @return .../{STANDARD_DIRECTORIES/customDir}/...
     */
    @JvmStatic
    fun getFolderPath(
        standardDir: StandardDirectory,
        customDir: String?,
        childDir: String = ""
    ): String {
        // 使用3级目录结构
        if (childDir.isNotEmpty() && !TextUtils.isEmpty(customDir)) {
            return standardDir.directoryName + File.separator + customDir + File.separator + childDir
        }
        // 使用2级目录结构
        if (!TextUtils.isEmpty(customDir)) {
            return standardDir.directoryName + File.separator + customDir
        }
        // 使用1级目录结构
        return standardDir.directoryName
    }


    /**
     * 检查是否是安卓MediaStore支持的标准存储目录
     */
    @JvmStatic
    fun isStandardDirectory(dir: String): Boolean {
        if (STANDARD_DIRECTORIES.isEmpty()) {
            val allDir = arrayOf(
                Environment.DIRECTORY_MUSIC,
                Environment.DIRECTORY_PODCASTS,
                Environment.DIRECTORY_RINGTONES,
                Environment.DIRECTORY_ALARMS,
                Environment.DIRECTORY_NOTIFICATIONS,
                Environment.DIRECTORY_PICTURES,
                Environment.DIRECTORY_MOVIES,
                Environment.DIRECTORY_DOWNLOADS,
                Environment.DIRECTORY_DCIM,
                Environment.DIRECTORY_DOCUMENTS,
            )
            STANDARD_DIRECTORIES.addAll(Arrays.asList(*allDir))
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                STANDARD_DIRECTORIES.add(Environment.DIRECTORY_AUDIOBOOKS)
            }
        }
        for (valid in STANDARD_DIRECTORIES) {
            if (valid == dir) {
                return true
            }
        }
        return false
    }

    @JvmStatic
    val isExternalStorageAvailable: Boolean
        /**
         * 判断ROM的外部存储是不是实际上可用
         *
         * @return
         */
        get() {
            try {
                val externalStoragePublicDirectory =
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val tempFile = File(
                    externalStoragePublicDirectory,
                    System.currentTimeMillis().toString() + ".txt"
                )
                val newFile = tempFile.createNewFile()
                if (newFile) {
                    val delete = tempFile.delete()
                    return true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return false
        }

    /**
     * 复制图片文件到指定的地方
     *
     * @param resolver    ContentResolver
     * @param imageFile   图片文件
     * @param folderPath  保存路径，这个路径是和AndroidStorage配合使用的
     * @param newFilename 新的文件名
     * @return 复制后的文件
     */
    @WorkerThread
    @JvmStatic
    fun copyImageTo(
        resolver: ContentResolver?,
        imageFile: File,
        folderPath: String,
        newFilename: String?
    ): Uri? {
        val filename = if (TextUtils.isEmpty(newFilename)) imageFile.name else newFilename!!
        //        String mimeType = MimeType.getMimeTypeFromFilename(filename);
//        if (TextUtils.isEmpty(mimeType)) {
//            mimeType = MimeType.JPEG;
//        }
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(imageFile.absolutePath, options)
        val width = options.outWidth
        val height = options.outHeight

        val imageDegree = MediaUtils.getImageDegree(imageFile.absolutePath)


        try {
            return saveImage(
                resolver,
                FileInputStream(imageFile),
                folderPath,
                filename,
                width,
                height,
                imageDegree
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @WorkerThread
    @JvmStatic
    fun copyVideoTo(
        resolver: ContentResolver?,
        videoFile: File,
        folderPath: String,
        newFilename: String?
    ): Uri? {
        val filename = if (TextUtils.isEmpty(newFilename)) videoFile.name else newFilename!!
        val videoMetaData = UriUtils.getVideoMetaData(videoFile)
        try {
            return saveVideo(
                resolver,
                FileInputStream(videoFile), folderPath, filename,
                videoMetaData.width, videoMetaData.height, videoMetaData.rotation,
                videoMetaData.duration, videoFile.length()
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    @WorkerThread
    @JvmStatic
    fun copyAudioTo(
        resolver: ContentResolver?,
        audioFile: File,
        folderPath: String,
        newFilename: String?
    ): Uri? {
        val filename = if (TextUtils.isEmpty(newFilename)) audioFile.name else newFilename!!
        try {
            return saveAudio(
                resolver,
                FileInputStream(audioFile), folderPath, filename
            )
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    /**
     * 复制文件到下载目录中，此目录可以接受任意类型的文件
     *
     * @param context       上下文
     * @param anyFile       任意类型的文件
     * @param newFilename   复制后的文件名，为空时使用原始的文件名
     * @param customDirName 二级目录文件夹名称,为空则放在Downloads目录下
     * @return
     */
    @WorkerThread
    @JvmStatic
    fun copyFileToDownloads(
        context: Context, anyFile: File,
        newFilename: String?, customDirName: String?
    ): Uri? {
        val resolver = context.contentResolver
        val filename = if (TextUtils.isEmpty(newFilename)) anyFile.name else newFilename!!
        val folderPath = getFolderPath(DirectoryFiles.DOWNLOADS, customDirName)
        val compatPath = getCompatPath(folderPath, filename)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = createBaseValues(
                compatPath, filename,
                getMimeType(filename, "", ""), 0
            )
            try {
                return save(
                    resolver,
                    contentValues,
                    FileInputStream(anyFile),
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                )
            } catch (fileNotFoundException: FileNotFoundException) {
                fileNotFoundException.printStackTrace()
            }
        } else {
            var fileInputStream: FileInputStream? = null
            var fileOutputStream: FileOutputStream? = null
            try {
                fileInputStream = FileInputStream(anyFile)
                fileOutputStream = FileOutputStream(compatPath)
                val transfer = IOUtils.transfer(fileInputStream, fileOutputStream)
                val file = File(compatPath)
                if (transfer && file.exists()) {
                    FileUtils.scanFile(context, file)
                    return Uri.fromFile(file)
                } else {
                    if (file.exists()) {
                        val delete = file.delete()
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                IOUtils.close(fileInputStream)
                IOUtils.close(fileOutputStream)
            }
        }
        return null
    }

    /**
     * 将 ByteArrayOutputStream 保存到下载目录
     *
     * @param context       上下文
     * @param outputStream  要保存的数据
     * @param filename      文件名（必须提供，ByteArray 无法推断）
     * @param customDirName 二级目录名称，空则直接放 Downloads
     */
    @WorkerThread
    @JvmStatic
    fun saveFileToDownloads(
        context: Context,
        outputStream: ByteArrayOutputStream,
        filename: String,
        mimeType: String,
        customDirName: String?
    ): Uri? {
        val resolver = context.contentResolver
        val folderPath = getFolderPath(DirectoryFiles.DOWNLOADS, customDirName)
        val compatPath = getCompatPath(folderPath, filename)
        val finalMimeType = mimeType.ifEmpty {
            getMimeType(filename, "", "")
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = createBaseValues(
                compatPath,
                filename,
                finalMimeType,
                0
            )

            return try {
                save(
                    resolver,
                    contentValues,
                    ByteArrayInputStream(outputStream.toByteArray()),
                    MediaStore.Downloads.EXTERNAL_CONTENT_URI
                )
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } else {
            var fos: FileOutputStream? = null
            try {
                val file = File(compatPath)
                file.parentFile?.mkdirs()

                fos = FileOutputStream(file)
                fos.write(outputStream.toByteArray())
                fos.flush()

                if (file.exists() && file.length() > 0) {
                    FileUtils.scanFile(context, file)
                    return Uri.fromFile(file)
                } else {
                    file.delete()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                IOUtils.close(fos)
            }
        }
        return null
    }

    @WorkerThread
    @JvmStatic
    fun saveBitmapToFile(
        bitmap: Bitmap,
        outputFile: File,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.PNG,
        quality: Int = 100
    ): Boolean {
        if (outputFile.exists()) {
            outputFile.delete()
        }
        var outputStream: FileOutputStream? = null
        try {
            outputStream = FileOutputStream(outputFile)
            bitmap.compress(format, quality, outputStream)
            return true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        } finally {
            IOUtils.close(outputStream)
        }
        return false
    }
}
