package com.androidx;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.media.DirectoryFiles;
import com.androidx.media.MimeType;
import com.androidx.media.StandardDirectory;
import com.androidx.media.VideoMetaData;
import com.androidx.utils.FileUtils;
import com.androidx.utils.IOUtils;
import com.androidx.utils.MediaUtils;
import com.androidx.utils.UriUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * user author: didikee
 * create time: 4/15/21 3:02 PM
 * description: 文件存储类，主要目的是方便存储安卓中常见的媒体类型
 * 照片：DCIM,Picture
 * 视频: DCIM,MOVIES
 * 音乐: Audios,Rington
 * 文件: Document,Download
 */
public final class AndroidStorage {
    // 当读取的时候就读取所有卷的数据，但是储存的时候只能往主要卷存数据
    public static final Uri EXTERNAL_IMAGE_URI = MediaStoreUtils.INSTANCE.getEXTERNAL_IMAGE_URI();
    public static final Uri EXTERNAL_VIDEO_URI = MediaStoreUtils.INSTANCE.getEXTERNAL_VIDEO_URI();
    public static final Uri EXTERNAL_AUDIO_URI = MediaStoreUtils.INSTANCE.getEXTERNAL_AUDIO_URI();

    public static final Uri EXTERNAL_DOWNLOAD_URI = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?
            MediaStore.Downloads.EXTERNAL_CONTENT_URI : Uri.parse("");
    private static final ArrayList<String> STANDARD_DIRECTORIES = new ArrayList<>();

    private static final String TAG = "StorageUtils";


    /**
     * 保存bitmap到存储目录,兼容androidx
     *
     * @param resolver   ContentResolver，参见 {@link Context#getContentResolver()}
     * @param bitmap     图片
     * @param folderPath 相对路径，可以通过{@link AndroidStorage#getFolderPath(String, String)}
     *                   其他详情参见 {@link AndroidStorage#getCompatPath(String, String)}
     * @param filename   文件名，注意拓展名要和bitmap的格式相匹配
     * @return 存储成功的uri，如果失败了则返回null
     */
    public static Uri saveBitmap(ContentResolver resolver, Bitmap bitmap, String folderPath, String filename, int quality) {
        if (resolver == null || bitmap == null) {
            LogUtils.e("StorageUtils saveBitmap() contentResolver or bitmap is null.");
            return null;
        }
        if (TextUtils.isEmpty(filename)) {
            LogUtils.e("StorageUtils saveBitmap() fileName is empty.");
            return null;
        }
        if (TextUtils.isEmpty(folderPath)) {
            LogUtils.e("StorageUtils saveBitmap() folderPath is empty.");
            return null;
        }
//        final Bitmap saveBitmap;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && (bitmap.getConfig() == Bitmap.Config.HARDWARE)) {
//            saveBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false);
//        }else {
//
//        }
        String mimeType = MimeType.getMimeTypeFromFilename(filename);
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.w("saveBitmap: getMimeTypeFromFilename failed, file name is " + filename);
            mimeType = MimeType.PNG;
        }
        ContentValues contentValues = createImageContentValues(folderPath, filename, mimeType, bitmap.getWidth(), bitmap.getHeight(), 0, 0);
        Uri destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        if (destUri != null) {
            OutputStream outputStream = null;
            try {
                outputStream = resolver.openOutputStream(destUri);
                if (outputStream != null) {
                    boolean compress = bitmap.compress(getCompressFormat(mimeType), quality, outputStream);
                    outputStream.flush();
                    if (compress) {
                        if (outputStream instanceof FileOutputStream) {
                            long size = ((FileOutputStream) outputStream).getChannel().size();
                            updateUriFileLength(resolver, destUri, contentValues, size);
                        }
                        clearPendingStates(resolver, destUri, contentValues);
                        return destUri;
                    } else {
                        delete(resolver, destUri);
                        return null;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(outputStream);
            }
        }

        return null;
    }

    public static Uri saveBitmap(ContentResolver resolver, Bitmap bitmap, String folderPath, String filename) {
        return saveBitmap(resolver, bitmap, folderPath, filename, 100);
    }

    @Deprecated
    public static Uri saveImage(ContentResolver resolver,
                                InputStream inputStream,
                                String folderPath,
                                String filename,
                                int width,
                                int height,
                                int rotate) {
        ContentValues contentValues = createImageContentValues(folderPath, filename, ""/*自动处理*/,
                width, height, rotate, 0);
        return save(resolver, contentValues, inputStream, MediaStoreUtils.INSTANCE.getEXTERNAL_IMAGE_PRIMARY_URI());
    }

    public static Uri saveImage(ContentResolver resolver,
                                InputStream inputStream,
                                String folderPath,
                                String filename,
                                int width,
                                int height,
                                int rotate,
                                long fileLength) {
        ContentValues contentValues = createImageContentValues(folderPath, filename, ""/*自动处理*/,
                width, height, rotate, Math.max(0, fileLength));
        return save(resolver, contentValues, inputStream, MediaStoreUtils.INSTANCE.getEXTERNAL_IMAGE_PRIMARY_URI());
    }

    @Nullable
    public static Uri saveImage(ContentResolver resolver, ContentValues contentValues, ContentTransfer<?> contentTransfer) {
        Uri insertUri = resolver.insert(MediaStoreUtils.INSTANCE.getEXTERNAL_IMAGE_PRIMARY_URI(), contentValues);
        OutputStream outputStream = null;
        try {
            outputStream = resolver.openOutputStream(insertUri);
            long transfer = contentTransfer.convertTo(outputStream);
            if (transfer > 0) {
                updateUriFileLength(resolver, insertUri, contentValues, transfer);
                clearPendingStates(resolver, insertUri, contentValues);
                return insertUri;
            } else {
                delete(resolver, insertUri);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            contentTransfer.release();
            IOUtils.close(outputStream);
        }
        return null;
    }

    public static Uri saveAudio(ContentResolver resolver,
                                InputStream inputStream,
                                String folderPath,
                                String filename
    ) {
        ContentValues contentValues = createAudioContentValues(folderPath, filename, ""/*自动处理*/, 0, 0);
        return save(resolver, contentValues, inputStream, MediaStoreUtils.INSTANCE.getEXTERNAL_AUDIO_PRIMARY_URI());
    }

    public static Uri saveVideo(ContentResolver resolver,
                                InputStream inputStream,
                                String folderPath,
                                String filename,
                                int width,
                                int height,
                                int rotate,
                                long duration,
                                long fileLength
    ) {
        ContentValues contentValues = createVideoContentValues(folderPath, filename, ""/*自动处理*/,
                width, height, rotate, duration, fileLength);
        return save(resolver, contentValues, inputStream, MediaStoreUtils.INSTANCE.getEXTERNAL_VIDEO_PRIMARY_URI());
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
    @Nullable
    private static Uri save(ContentResolver resolver,
                            ContentValues contentValues,
                            InputStream inputStream,
                            Uri contentUri) {
        if (resolver == null || contentUri == null || contentValues == null || inputStream == null) {
            LogUtils.e("StorageUtils save() contentResolver or bitmap is null.");
            return null;
        }
        Uri insertUri = resolver.insert(contentUri, contentValues);
        OutputStream outputStream = null;
        try {
            outputStream = resolver.openOutputStream(insertUri);
            long transfer = IOUtils.transfer2(inputStream, outputStream);
            if (transfer > 0) {
                updateFileLengthAndClearPending(resolver, insertUri, contentValues, transfer);
                return insertUri;
            } else {
                delete(resolver, insertUri);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(inputStream);
            IOUtils.close(outputStream);
        }
        return null;
    }

    /**
     * 清楚isPending标志
     * isPending为true时，MediaStore API会忽略它。表现形式就是相册里看不到。
     * 知道isPending 为false时才可见。
     * <p>
     * 注意完成写入uri操作后一定要清楚isPending标志位，否则即使写入成功，MediaStore依然是不可见的。
     */
    public static void clearPendingStates(ContentResolver resolver, Uri uri, ContentValues contentValues) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && contentValues.getAsBoolean(MediaStore.MediaColumns.IS_PENDING)) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
            resolver.update(uri, contentValues, null, null);
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
    public static void updateUriFileLength(ContentResolver resolver, Uri uri, ContentValues contentValues, long fileLength) {
        Long asLong = contentValues.getAsLong(MediaStore.MediaColumns.SIZE);
        long oldFileLength = asLong == null ? 0 : asLong.longValue();
        if (fileLength > 0 && oldFileLength != fileLength) {
            contentValues.put(MediaStore.MediaColumns.SIZE, fileLength);
            resolver.update(uri, contentValues, null, null);
        }
    }

    public static void updateFileLengthAndClearPending(ContentResolver resolver, Uri uri, ContentValues contentValues, long fileLength) {
        Long asLong = contentValues.getAsLong(MediaStore.MediaColumns.SIZE);
        long oldFileLength = asLong == null ? 0 : asLong.longValue();
        boolean clearPending = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && contentValues.getAsBoolean(MediaStore.MediaColumns.IS_PENDING);
        // 清除原始contentValues中的所有数据
        contentValues.clear();
        if (fileLength > 0 && oldFileLength != fileLength) {
            contentValues.put(MediaStore.MediaColumns.SIZE, fileLength);
        }
        if (clearPending) {
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
        }
        if (contentValues.size() > 0) {
            resolver.update(uri, contentValues, null, null);
        } else {
            LogUtils.w("updateFileLengthAndClearPending cancel: fileLength == 0 or clearPending = false");
        }

    }


    /**
     * 删除uri
     *
     * @param resolver
     * @param uri
     * @return 返回uri在数据库中对应的行位置,-1表示删除失败
     */
    public static boolean delete(ContentResolver resolver, Uri uri) {
        int row = -1;
        try {
            if (resolver != null && uri != null) {
                row = resolver.delete(uri, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return row > -1;
    }

    public static ContentValues createImageContentValues(String folderPath, String filename, String mimeType, int width, int height, int rotate, long fileLength) {
        String compatPath = getCompatPath(folderPath, filename);
        ContentValues contentValues = createBaseValues(compatPath, filename, getMimeType(filename, mimeType, MimeType.JPEG), Math.max(0, fileLength));
        if (width > 0 && height > 0) {
            contentValues.put(MediaStore.MediaColumns.WIDTH, width);
            contentValues.put(MediaStore.MediaColumns.HEIGHT, height);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.ORIENTATION, rotate);
        }
        return contentValues;
    }

    public static ContentValues createAudioContentValues(String folderPath, String filename,
                                                         String mimeType, long duration, long fileLength) {
        String compatPath = getCompatPath(folderPath, filename);
        ContentValues contentValues = createBaseValues(compatPath, filename, getMimeType(filename, mimeType, MimeType.MP3), Math.max(0, fileLength));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && duration > 0) {
            contentValues.put(MediaStore.Audio.AudioColumns.DURATION, duration);
        }
        return contentValues;
    }

    public static ContentValues createVideoContentValues(String folderPath, String filename, String mimeType, int width, int height, int rotate, long duration, long fileLength) {
        String compatPath = getCompatPath(folderPath, filename);
        ContentValues contentValues = createBaseValues(compatPath, filename, getMimeType(filename, mimeType, MimeType.MP4), Math.max(0, fileLength));
        contentValues.put(MediaStore.MediaColumns.WIDTH, width);
        contentValues.put(MediaStore.MediaColumns.HEIGHT, height);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 在api29开始视频支持了旋转参数
            contentValues.put(MediaStore.MediaColumns.ORIENTATION, rotate);
            contentValues.put(MediaStore.MediaColumns.DURATION, duration);
        }
        return contentValues;
    }

    public static ContentValues createBaseValues(String compatPath, String filename, String mimeType, long fileLength) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.MediaColumns.DATE_MODIFIED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        if (fileLength > 0) {
            values.put(MediaStore.MediaColumns.SIZE, fileLength);
        }
        // data_taken 在android10以下只有image 和 video有，audio是没有的
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        } else {
            if (MimeType.isImage(mimeType) || MimeType.isVideo(mimeType)) {
                //TODO audio类型暂不支持,其他的还未测试
                values.put(UriUtils.DATE_TAKEN, System.currentTimeMillis());
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, compatPath);
            values.put(MediaStore.MediaColumns.IS_PENDING, true);
        } else {
            values.put(MediaStore.MediaColumns.DATA, compatPath);
        }
        return values;
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
     *                   Environment.DIRECTORY_PICTURES + File.separator + "folder";
     *                   Environment.DIRECTORY_DCIM + File.separator + "folder";
     * @param filename   文件名
     * @return 适用于安卓所有的路径
     */
    public static String getCompatPath(String folderPath, String filename) {
        return getCompatPath(folderPath, filename, Build.VERSION.SDK_INT);
    }

    public static String getCompatPath(String folderPath, String filename, int sdkVersion) {
        if (TextUtils.isEmpty(folderPath) || TextUtils.isEmpty(filename)) {
            // 默认返回
            return "";
        }
        boolean startSeparator = folderPath.startsWith(File.separator);
        boolean endSeparator = folderPath.endsWith(File.separator);
        String subRelativePath = folderPath;
        if (startSeparator || endSeparator) {
            subRelativePath = folderPath.substring(startSeparator ? 1 : 0,
                    endSeparator ? folderPath.length() - 1 : folderPath.length());
        }
        String compatPath;
        if (sdkVersion >= Build.VERSION_CODES.Q) {
            compatPath = subRelativePath;
        } else {
            File externalStorageDirectory = Environment.getExternalStorageDirectory();
            File dir = new File(externalStorageDirectory.getAbsolutePath() + File.separator
                    + subRelativePath + File.separator);
            if (!dir.exists()) {
                boolean mkdirs = dir.mkdirs();
            }
            compatPath = externalStorageDirectory.getAbsolutePath() + File.separator
                    + subRelativePath + File.separator + filename;
        }
        return compatPath;
    }

    /**
     * 获取mimetype
     *
     * @param filename        文件名，带拓展名
     * @param mimeType        类型
     * @param defaultMimeType 默认类型
     * @return
     */
    private static String getMimeType(String filename, String mimeType, String defaultMimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            String mimeTypeFromFilename = MimeType.getMimeTypeFromFilename(filename);
            if (TextUtils.isEmpty(mimeTypeFromFilename)) {
                return defaultMimeType;
            } else {
                return mimeTypeFromFilename;
            }
        } else {
            return mimeType;
        }
    }

    private static Bitmap.CompressFormat getCompressFormat(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.e("getCompressFormat: image mime type is empty.");
            return Bitmap.CompressFormat.PNG;
        }
        if (mimeType.equalsIgnoreCase(MimeType.PNG)) {
            return Bitmap.CompressFormat.PNG;
        }
        if (mimeType.equalsIgnoreCase(MimeType.JPEG)) {
            return Bitmap.CompressFormat.PNG;
        }
        if (mimeType.equalsIgnoreCase(MimeType.WEBP)) {
            return Bitmap.CompressFormat.PNG;
        }
        LogUtils.e("getCompressFormat: unSupport image mime type= " + mimeType);
        return Bitmap.CompressFormat.PNG;
    }

    public static boolean isAboveVersionQ() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    /**
     * 获取类似.../{Picture/APP_CUSTOM_FOLDER}/...结构的目录
     *
     * @param standardDir 一级目录，不能为空。为空时Q以上可以运行，低版本会出问题
     * @param customDir   二级目录，可以为空
     * @return .../{STANDARD_DIRECTORIES/customDir}/...
     * @deprecated 推荐使用 {@link #getFolderPath(StandardDirectory, String)}
     */
    @Deprecated
    public static String getFolderPath(@NonNull String standardDir, @Nullable String customDir) {
        if (!isStandardDirectory(standardDir)) {
            throw new UnsupportedOperationException("Cannot use file directories other than STANDARD_DIRECTORIES");
        }
        if (TextUtils.isEmpty(customDir)) {
            return standardDir;
        }
        return standardDir + File.separator + customDir;
    }

    /**
     * 获取类似.../{Picture/APP_CUSTOM_FOLDER}/...结构的目录
     *
     * @param standardDir 一级目录，不能为空。为空时Q以上可以运行，低版本会出问题
     * @param customDir   二级目录，可以为空
     * @return .../{STANDARD_DIRECTORIES/customDir}/...
     */
    public static String getFolderPath(@NonNull StandardDirectory standardDir, @Nullable String customDir) {
        if (TextUtils.isEmpty(customDir)) {
            return standardDir.getDirectoryName();
        }
        return standardDir.getDirectoryName() + File.separator + customDir;
    }


    /**
     * 检查是否是安卓MediaStore支持的标准存储目录
     */
    public static boolean isStandardDirectory(String dir) {
        if (STANDARD_DIRECTORIES.isEmpty()) {
            String[] allDir = {
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
            };
            STANDARD_DIRECTORIES.addAll(Arrays.asList(allDir));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                STANDARD_DIRECTORIES.add(Environment.DIRECTORY_AUDIOBOOKS);
            }
        }
        for (String valid : STANDARD_DIRECTORIES) {
            if (valid.equals(dir)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断ROM的外部存储是不是实际上可用
     *
     * @return
     */
    public static boolean isExternalStorageAvailable() {
        try {
            File externalStoragePublicDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            final File tempFile = new File(externalStoragePublicDirectory, System.currentTimeMillis() + ".txt");
            boolean newFile = tempFile.createNewFile();
            if (newFile) {
                boolean delete = tempFile.delete();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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
    public static Uri copyImageTo(ContentResolver resolver, File imageFile, String folderPath, String newFilename) {
        String filename = TextUtils.isEmpty(newFilename) ? imageFile.getName() : newFilename;
//        String mimeType = MimeType.getMimeTypeFromFilename(filename);
//        if (TextUtils.isEmpty(mimeType)) {
//            mimeType = MimeType.JPEG;
//        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int width = options.outWidth;
        int height = options.outHeight;

        int imageDegree = MediaUtils.getImageDegree(imageFile.getAbsolutePath());

        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
        try {
            return AndroidStorage.saveImage(resolver, new FileInputStream(imageFile), folderPath, filename, width, height, imageDegree);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @WorkerThread
    public static Uri copyVideoTo(ContentResolver resolver, File videoFile, String folderPath, String newFilename) {
        String filename = TextUtils.isEmpty(newFilename) ? videoFile.getName() : newFilename;
        VideoMetaData videoMetaData = UriUtils.getVideoMetaData(videoFile);
        try {
            return AndroidStorage.saveVideo(resolver,
                    new FileInputStream(videoFile), folderPath, filename,
                    videoMetaData.getWidth(), videoMetaData.getHeight(), videoMetaData.getRotation(),
                    videoMetaData.getDuration(), videoFile.length());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @WorkerThread
    public static Uri copyAudioTo(ContentResolver resolver, File audioFile, String folderPath, String newFilename) {
        String filename = TextUtils.isEmpty(newFilename) ? audioFile.getName() : newFilename;
        try {
            return AndroidStorage.saveAudio(resolver,
                    new FileInputStream(audioFile), folderPath, filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
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
    public static Uri copyFileToDownloads(@NonNull Context context, @NonNull File anyFile,
                                          @Nullable String newFilename, @Nullable String customDirName) {
        ContentResolver resolver = context.getContentResolver();
        String filename = TextUtils.isEmpty(newFilename) ? anyFile.getName() : newFilename;
        String folderPath = getFolderPath(DirectoryFiles.DOWNLOADS, customDirName);
        String compatPath = getCompatPath(folderPath, filename);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContentValues contentValues = createBaseValues(compatPath, filename,
                    getMimeType(filename, "", ""), 0);
            try {
                return save(resolver, contentValues, new FileInputStream(anyFile), MediaStore.Downloads.EXTERNAL_CONTENT_URI);
            } catch (FileNotFoundException fileNotFoundException) {
                fileNotFoundException.printStackTrace();
            }
        } else {
            FileInputStream fileInputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                fileInputStream = new FileInputStream(anyFile);
                fileOutputStream = new FileOutputStream(compatPath);
                boolean transfer = IOUtils.transfer(fileInputStream, fileOutputStream);
                File file = new File(compatPath);
                if (transfer && file.exists()) {
                    FileUtils.scanFile(context, file);
                    return Uri.fromFile(file);
                } else {
                    if (file.exists()) {
                        boolean delete = file.delete();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                IOUtils.close(fileInputStream);
                IOUtils.close(fileOutputStream);
            }
        }
        return null;
    }

}
