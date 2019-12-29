package com.androidx;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.media.MediaUriInfo;
import com.androidx.media.MimeType;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * user author: didikee
 * create time: 2019-12-02 16:31
 * description: 
 */
public final class StorageSaveUtils {

//    public static Uri saveBitmap(ContentResolver contentResolver, Bitmap bitmap, String fileName, String mimeType) {
//        String path = getPath(Environment.DIRECTORY_DCIM, fileName);
//        return saveBitmap(contentResolver, bitmap, path,fileName,  mimeType);
//    }

    /**
     * 保存bitmap到存储目录,兼容androidx
     * @param contentResolver
     * @param bitmap
     * @param fileName
     * @param mimeType
     * @return
     */
    public static Uri saveBitmap(ContentResolver contentResolver, Bitmap bitmap, String folderPath, String fileName, String mimeType) {
        if (contentResolver == null || bitmap == null) {
            LogUtils.e("StorageSaveUtils saveBitmap() contentResolver or bitmap is null.");
            return null;
        }
        if (TextUtils.isEmpty(fileName)) {
            LogUtils.e("StorageSaveUtils saveBitmap() fileName is empty.");
            return null;
        }
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.e("StorageSaveUtils saveBitmap() mimeType is empty.");
            return null;
        }
        if (TextUtils.isEmpty(folderPath)) {
            LogUtils.e("StorageSaveUtils saveBitmap() folderPath is empty.");
            return null;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(MediaStore.Images.Media.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        contentValues.put(MediaStore.Images.Media.WIDTH, bitmap.getWidth());
        contentValues.put(MediaStore.Images.Media.HEIGHT, bitmap.getHeight());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, true);
        } else {
            contentValues.put(MediaStore.MediaColumns.DATA, getDataPath(folderPath, fileName));
        }
        Uri destUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        OutputStream outputStream = null;
        try {
            if (destUri != null) {
                outputStream = contentResolver.openOutputStream(destUri);
            }
            if (outputStream != null) {
                boolean compress = bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                outputStream.flush();
                if (compress) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
                        contentResolver.update(destUri, contentValues, null, null);
                    }
                    return destUri;
                } else {
                    contentResolver.delete(destUri, null, null);
                    return null;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     * 插入一个文件,支持图片和视频
     * @param contentResolver
     * @param mediaFile
     * @param folderPath
     * @return
     */
    public static Uri saveMediaFile(ContentResolver contentResolver, File mediaFile, String folderPath) {
        if (contentResolver == null) {
            LogUtils.e("StorageSaveUtils saveMediaFile() contentResolver is null.");
            return null;
        }
        if (mediaFile == null || !mediaFile.exists()) {
            LogUtils.e("StorageSaveUtils saveMediaFile() file error.");
            return null;
        }
        if (TextUtils.isEmpty(folderPath)) {
            LogUtils.e("StorageSaveUtils saveMediaFile() folderPath is empty.");
            return null;
        }
        String mimeType = "";

        String fileName = mediaFile.getName();
        try {
            String extension = fileName.substring(fileName.lastIndexOf(".") + 1);
            if ("png".equalsIgnoreCase(extension)) {
                mimeType = MimeType.PNG;
            } else if ("jpg".equalsIgnoreCase(extension) || "jpeg".equalsIgnoreCase(extension)) {
                mimeType = MimeType.JPEG;
            } else if ("gif".equalsIgnoreCase(extension)) {
                mimeType = MimeType.GIF;
            } else if ("mp4".equalsIgnoreCase(extension)) {
                mimeType = MimeType.MP4;
            } else if ("mp3".equalsIgnoreCase(extension)) {
                mimeType = MimeType.MP3;
            } else if ("aac".equalsIgnoreCase(extension)) {
                mimeType = MimeType.AAC;
            } else if ("wav".equalsIgnoreCase(extension)) {
                mimeType = MimeType.WAV;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.e("StorageSaveUtils saveMediaFile() mimeType is unknown.");
            return null;
        }

        ContentValues contentValues;
        Uri destUri;
        if (mimeType.startsWith("image")) {
            int width = 0;
            int height = 0;
            try {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(mediaFile.getAbsolutePath(), options);
                width = options.outWidth;
                height = options.outHeight;
            } catch (Exception e) {
                e.printStackTrace();
            }
            contentValues = StorageUriUtils.makeImageValues(folderPath, fileName, mimeType, width, height, 0);
            destUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else if (mimeType.startsWith("video")) {
            contentValues = StorageUriUtils.makeVideoValues(folderPath, fileName, 0);
            destUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else if (mimeType.startsWith("audio")) {
            contentValues = StorageUriUtils.makeMediaValues(folderPath, fileName, mimeType, 0, 0, 0);
            destUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
            contentValues = StorageUriUtils.makeMediaValues(folderPath, fileName, mimeType, 0, 0, 0);
            destUri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
        }
        OutputStream outputStream = null;
        FileInputStream inputStream = null;
        if (destUri != null) {
            try {
                outputStream = contentResolver.openOutputStream(destUri);
                inputStream = new FileInputStream(mediaFile);
                if (outputStream != null) {
                    //获得原文件流
                    byte[] buffer = new byte[2048];
                    //输出流
                    //开始处理流
                    while (inputStream.read(buffer) != -1) {
                        outputStream.write(buffer);
                    }
                    outputStream.flush();

                    // update
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
                        contentResolver.update(destUri, contentValues, null, null);
                    }
                    return destUri;
                }
            } catch (Exception e) {
                e.printStackTrace();
                contentResolver.delete(destUri, null, null);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }


    /**
     * 保存图片文件到uri
     * @param contentResolver
     * @param imageFile
     * @param folderPath
     * @return
     */
    @Deprecated
    public static Uri saveImageFile(ContentResolver contentResolver, File imageFile, String folderPath) {
        return saveMediaFile(contentResolver, imageFile, folderPath);
    }

    /**
     * 适用于
     * @param folderPath
     * @param filename
     * @return
     */
    public static String getDataPath(String folderPath, String filename) {
        String data;
        if (folderPath.endsWith(File.separator)) {
            data = folderPath + filename;
        } else {
            data = folderPath + File.separator + filename;
        }
        return data;
    }

    /**
     * 删除uri
     * @param contentResolver
     * @param uri
     * @return
     */
    public static boolean delete(ContentResolver contentResolver, Uri uri) {
        if (contentResolver == null) {
            return false;
        }
        if (uri == null || TextUtils.isEmpty(uri.toString())) {
            return false;
        }
        int delete = contentResolver.delete(uri, null, null);
        return delete != -1;
    }

    public static Uri copy(ContentResolver contentResolver, Uri src, String filename, String data, String relativePath) {
        MediaUriInfo mediaInfo = UriUtils.getMediaInfo(contentResolver, src);
        if (mediaInfo == null) {
            return null;
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mediaInfo.getMimeType());
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        int width = mediaInfo.getWidth();
        int height = mediaInfo.getHeight();
        if (width * height > 0) {
            contentValues.put(MediaStore.MediaColumns.WIDTH, width);
            contentValues.put(MediaStore.MediaColumns.HEIGHT, height);
        }
        long size = mediaInfo.getSize();
        if (size > 0) {
            contentValues.put(MediaStore.MediaColumns.SIZE, size);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, true);
            LogUtils.d("StorageSaveUtils copy() relativePath: " + relativePath);
        } else {
            contentValues.put(MediaStore.MediaColumns.DATA, data);
            LogUtils.d("StorageSaveUtils copy() data: " + data);
        }
        String mimeType = mediaInfo.getMimeType();
        Uri mediaStoreUri;
        if (mimeType.startsWith("video")) {
            mediaStoreUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("image")) {
            mediaStoreUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (mimeType.startsWith("audio")) {
            mediaStoreUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            mediaStoreUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
        }
        Uri destUri = contentResolver.insert(mediaStoreUri, contentValues);
        if (destUri == null) {
            LogUtils.e("StorageSaveUtils copy() insert failed.");
            return null;
        }
        OutputStream outputStream = null;
        InputStream inputStream = null;
        try {
            outputStream = contentResolver.openOutputStream(destUri);
            if (outputStream != null) {
                inputStream = contentResolver.openInputStream(src);
                if (inputStream != null) {
                    //获得原文件流
                    byte[] buffer = new byte[2048];
                    //输出流
                    //开始处理流
                    while (inputStream.read(buffer) != -1) {
                        outputStream.write(buffer);
                    }
                    outputStream.flush();

                    // update
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
                        contentResolver.update(destUri, contentValues, null, null);

                    }
                    return destUri;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }
}
