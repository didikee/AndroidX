package com.androidx;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.WorkerThread;
import android.text.TextUtils;
import android.util.Log;

import com.androidx.media.MimeType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * user author: didikee
 * create time: 2019-11-29 17:35
 * description: 这个类是一个桥接作用,用于将内外的数据连接起来,兼容Android10以下的低版本
 * 例如用c编写的公用组件,比如ffmpeg等等.
 *
 *
 */
public class StorageConnector {
    private static final String TAG = "StorageConnector";
    private static final String DIR_DATA_CONNECT_CACHE = "connect";
    protected final Context context;
    protected final File internalTempDir;
    // 用于复制外部的uri
    protected File inputUriFile;
    /**
     * 用于第三方c的使用
     * 例如ffmpeg的工作流如下:
     *           copy                        c run                           copy to
     * inputUri ------> inputUriFile ------->  internalTempSaveFile  ---------> outputUri
     *
     * 可以经过改良来兼容低于android10的版本
     *           copy（查询路径，如果查到直接赋值）                c run                           copy to
     * inputUri -------------------------------> inputUriFile ------->  internalTempSaveFile  ---------> outputUri
     *
     * 这样就做到了输入uri,然后得到uri
     */
    protected File internalTempSaveFile;
    private final Uri inputUri;

    private final String filename;
    private final String folderPath;
    private String mimeType;

    public StorageConnector(Context context, String filename, String folderPath) {
        this(context, filename, folderPath, null);
    }

    /**
     * 指定输入和输出位置,然后给一个临时存放文件的文件夹(事后可以清除改文件夹的内容)
     * @param inputUri
     * @param filename
     */
    public StorageConnector(Context context, String filename, String folderPath, Uri inputUri) {
        this.context = context;
        this.folderPath = folderPath;
        this.inputUri = inputUri;
        this.internalTempDir = getCacheDir(context);
        this.filename = filename;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * 创建c工作的环境
     * 有两种情况需要考虑
     * 第一: c库只是保存时需要一个java file路径,输入的都是数据(字符串,bitmap,byte[]等),那么只有output uri和对于的outputFile
     * 第二种:c库输入和输出都是一个java 的媒体文件路径,那么输入和输出的uri都要指定才行,并且获取时也要一一对应
     */
    @WorkerThread
    public boolean prepare() {
        if (TextUtils.isEmpty(folderPath)) {
            LogUtils.e("StorageConnector prepare() folderPath is empty");
            return false;
        }
        if (TextUtils.isEmpty(filename)) {
            LogUtils.e("StorageConnector prepare() filename is empty");
            return false;
        }
        if (inputUri == null) {
            Log.d(TAG, "prepare skip input uri prepare");
        } else {
            String realPath = getRealPath(inputUri);
            if (!TextUtils.isEmpty(realPath)) {
                File realInputFile = new File(realPath);
                if (realInputFile.exists()) {
                    inputUriFile = realInputFile;
                }
            }
            if (inputUriFile != null && inputUriFile.exists()) {
                // do nothing
                // 我们不需要复制uri了，直接使用uri对应的文件地址对于c库来说更便捷，特别是ffmpeg之类的库
            } else {
                // 1. create copy file
                inputUriFile = new File(internalTempDir, getRandomFileName());
                // 2. copy file
                boolean b = copyUri2File(context, inputUri, inputUriFile);
                if (!b) {
                    return false;
                }
            }

        }
        // 3. create temp save file for c program
        internalTempSaveFile = createOutputFile();
        if (inputUriFile != null) {
            LogUtils.d("StorageConnector prepare input: " + inputUriFile.getAbsolutePath());
        }
        LogUtils.d("StorageConnector prepare output: " + internalTempSaveFile.getAbsolutePath());
        return true;
    }

    protected File createOutputFile() {
        return new File(internalTempDir, filename);
    }

    public String getRealPath(Uri inputUri) {
        // TODO 等待实现
        return null;
    }

    public Uri getInputUri() {
        return inputUri;
    }

    public String getFilename() {
        return filename;
    }

    public String getFolderPath() {
        return folderPath;
    }

    /**
     * 给c程序使用的输入和输出,完全的Java api
     * @return
     */
    public File getInputFile() {
        return inputUriFile;
    }

    /**
     * 给c程序使用的输入和输出,完全的Java api
     * @return
     */
    public File getOutputFile() {
        return internalTempSaveFile;
    }

    public Uri save() {
        return saveFile(internalTempSaveFile);
    }

    public Uri saveFile(File inputFile) {
        if (inputFile == null || !inputFile.exists()) {
            Log.e(TAG, "c program maybe work failed,temp save is not exists");
            return null;
        }
        String fileName = inputFile.getName();
        // 默认是空的，可以设置mime type
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = MimeType.getMimeTypeFromFilename(fileName);
        }
        // 1. 获取 contentValues
        ContentValues contentValues = getContentValues(inputFile, folderPath, fileName, mimeType);
        LogUtils.d("folderPath: " + folderPath + " mimeType: " + mimeType);
        LogUtils.d("StorageConnector saveFile() contentValues: " + contentValues.toString());
        ContentResolver contentResolver = context.getContentResolver();
        // 2. 获取对应媒体的插入 uri
        Uri mediaLocation = getMediaLocation(mimeType);
        LogUtils.d("StorageConnector getMediaLocation() uri: " + mediaLocation.toString());
        // 3. 插入数据，如果成功继续，如果失败就直接返回
        Uri uri = contentResolver.insert(mediaLocation, contentValues);
        if (uri == null) {
            Log.e(TAG, "save StorageUriUtils.makeImageUri failed");
            return null;
        }
        OutputStream outputStream = null;
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(inputFile);
            outputStream = contentResolver.openOutputStream(uri);
            if (outputStream == null) {
                return null;
            }
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
                contentResolver.update(uri, contentValues, null, null);
            }
            return uri;
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri, null, null);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public ContentValues getContentValues(File file, String folderPath, String filename, String mimeType) {
        return StorageUriUtils.getContentValues(file, folderPath, filename, mimeType);
    }

    protected boolean copyUri2File(Context context, Uri uri, File outputFile) {
        long start = System.currentTimeMillis();
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(outputFile);
            if (inputStream != null) {
                //获得原文件流
                byte[] data = new byte[2048];
                //输出流
                //开始处理流
                while (inputStream.read(data) != -1) {
                    outputStream.write(data);
                }
                inputStream.close();
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        Log.d(TAG, "copyUri2File file size: " + outputFile.length() + " spent: " + (System.currentTimeMillis() - start) + "ms");
        return true;
    }

    protected String getRandomFileName() {
        return System.currentTimeMillis() + "";
    }

    public void deleteCache() {
        if (inputUriFile != null && inputUriFile.exists()) {
            boolean delete = inputUriFile.delete();
            Log.d(TAG, "deleteCache inputUriFile delete success: " + delete);
        }
        if (internalTempSaveFile != null && internalTempSaveFile.exists()) {
            boolean delete = internalTempSaveFile.delete();
            Log.d(TAG, "deleteCache internalTempSaveFile delete success: " + delete);
        }
    }

    public File getCacheDir(Context context) {
        if (context == null) {
            Log.e(TAG, "getCacheDir Context is null.");
            return null;
        }
        String cacheDirPath = context.getFilesDir().getAbsolutePath() + File.separator + DIR_DATA_CONNECT_CACHE + File.separator;
        File cacheDir = new File(cacheDirPath);
        if (!cacheDir.exists()) {
            boolean mkdirs = cacheDir.mkdirs();
            if (!mkdirs) {
                Log.e(TAG, "getCacheDir mkdirs failed");
            }
        }
        return cacheDir;
    }

    protected String getMimeType(String filename) {
        String extension = "";
        try {
            extension = filename.substring(filename.lastIndexOf(".") + 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(extension)) {
            return MimeType.UNKNOWN;
        } else {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
                // aac要单独判断，因为4.4没有aac格式
                if ("aac".equalsIgnoreCase(extension)) {
                    return MimeType.AAC;
                }
            }
            return MimeType.getMimeTypeFromExtension(extension.toLowerCase());
        }
    }

    protected Uri getMediaLocation(String mimeType) {
        if (MimeType.isVideo(mimeType)) {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }
        if (MimeType.isImage(mimeType)) {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }
        if (MimeType.isAudio(mimeType)) {
            return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        }
        return MediaStore.Downloads.EXTERNAL_CONTENT_URI;
    }
}
