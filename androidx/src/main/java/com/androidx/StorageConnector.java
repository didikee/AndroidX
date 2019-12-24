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
    private final File internalTempDir;
    // 用于复制外部的uri
    private File inputUriFile;
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
    private File internalTempSaveFile;
    private final Uri inputUri;
    private final Context context;
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
            // 1. create copy file
            inputUriFile = new File(internalTempDir, getRandomFileName());
            // 2. copy file
            boolean b = copyUri2File(context, inputUri, inputUriFile);
            if (!b) {
                return false;
            }
        }
        // 3. create temp save file for c program
        internalTempSaveFile = new File(internalTempDir, filename);
        return true;
    }

    public File getRealPath(Uri inputUri) {
        // TODO 等待实现
        return null;
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
        return save(internalTempSaveFile);
    }

    public Uri save(File inputFile) {
        if (inputFile == null || !inputFile.exists()) {
            Log.e(TAG, "c program maybe work failed,temp save is not exists");
            return null;
        }
        String fileName = inputFile.getName();
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = getMimeType(fileName);
        }

        ContentValues values = StorageUriUtils.makeMediaValues(
                folderPath,
                fileName,
                mimeType,
                0, 0,
                inputFile.length()
        );
        LogUtils.d("folderPath: " + folderPath + " mimeType: " + mimeType);
        ContentResolver contentResolver = context.getContentResolver();
        Uri mediaLocation = getMediaLocation(mimeType);
        Uri uri = contentResolver.insert(mediaLocation, values);
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
                values.put(MediaStore.MediaColumns.IS_PENDING, false);
                contentResolver.update(uri, values, null, null);
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

    private boolean copyUri2File(Context context, Uri uri, File outputFile) {
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

    private String getRandomFileName() {
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

    private String getMimeType(String filename) {
        String mimeType = MimeType.UNKNOWN;
        if (!TextUtils.isEmpty(filename)) {
            try {
                String extension = filename.substring(filename.lastIndexOf(".") + 1);
                if ("gif".equalsIgnoreCase(extension)) {
                    mimeType = MimeType.GIF;
                } else if ("mp4".equalsIgnoreCase(extension)) {
                    mimeType = MimeType.MP4;
                } else if ("png".equalsIgnoreCase(extension)) {
                    mimeType = MimeType.PNG;
                } else if ("jpeg".equalsIgnoreCase(extension)) {
                    mimeType = MimeType.JPEG;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return mimeType;
    }

    private Uri getMediaLocation(String mimeType) {
        Uri location;
        switch (mimeType) {
            case MimeType.GIF:
            case MimeType.JPEG:
            case MimeType.PNG:
                location = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                break;
            case MimeType.MP4:
                location = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                break;
            default:
                location = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                break;
        }
        return location;
    }
}
