package com.androidx;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
    @Deprecated
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
            mimeType = MimeType.getMimeTypeFromExtension(extension);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.e("StorageSaveUtils saveMediaFile() mimeType is unknown.");
            return null;
        }

        ContentValues contentValues = StorageUriUtils.getContentValues(mediaFile, folderPath, fileName, mimeType);
        if (contentValues == null) {
            LogUtils.e("StorageSaveUtils saveMediaFile() ContentValues is null.");
            return null;
        }

        Uri destUri;
        if (MimeType.isImage(mimeType)) {
            destUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else if (MimeType.isVideo(mimeType)) {
            destUri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else if (MimeType.isAudio(mimeType)) {
            destUri = contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues);
        } else {
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

    /**
     * 这个方法稚嫩gcopy图片，其他的就不行了
     * @param context
     * @param src
     * @param filename
     * @param data
     * @param relativePath
     * @return
     */
    @Deprecated
    public static Uri copy(Context context, Uri src, String filename, String data, String relativePath) {
        MediaUriInfo mediaInfo = UriUtils.getMediaInfo(context, src);
        if (mediaInfo == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mediaInfo.getMimeType());
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(UriUtils.DATE_TAKEN, System.currentTimeMillis());
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

    /**
     * 复制uri 到另一个位置
     * @param context
     * @param srcUri
     * @param folderPath
     * @return
     */
    public static Uri imageCopy(Context context, Uri srcUri, String folderPath) {
        if (context == null || srcUri == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        MediaUriInfo imageInfo = UriUtils.getImageInfo(contentResolver, srcUri);
        if (imageInfo == null) {
            return null;
        }
        String displayName = imageInfo.getDisplayName();

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, imageInfo.getMimeType());
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(UriUtils.DATE_TAKEN, System.currentTimeMillis());
        int width = imageInfo.getWidth();
        int height = imageInfo.getHeight();
        if (width * height > 0) {
            contentValues.put(MediaStore.MediaColumns.WIDTH, width);
            contentValues.put(MediaStore.MediaColumns.HEIGHT, height);
        }
        long size = imageInfo.getSize();
        if (size > 0) {
            contentValues.put(MediaStore.MediaColumns.SIZE, size);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, true);
            LogUtils.d("imageCopy folderPath: " + folderPath);
        } else {
            String dataPath = getDataPath(folderPath, displayName);
            contentValues.put(MediaStore.MediaColumns.DATA, dataPath);
            LogUtils.d("imageCopy data path: " + dataPath);
        }
        String mimeType = imageInfo.getMimeType();
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
                inputStream = contentResolver.openInputStream(srcUri);
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

    public static Uri imageCopy(Context context, File imageFile, String folderPath) {
        if (context == null || imageFile == null || !imageFile.exists()) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();

        String displayName = imageFile.getName();
        String mimeType = MimeType.getMimeTypeFromFilename(displayName);

        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        contentValues.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        contentValues.put(UriUtils.DATE_TAKEN, System.currentTimeMillis());
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);

        int width = options.outWidth;
        int height = options.outHeight;
        if (width * height > 0) {
            contentValues.put(MediaStore.MediaColumns.WIDTH, width);
            contentValues.put(MediaStore.MediaColumns.HEIGHT, height);
        }
        long size = imageFile.length();
        if (size > 0) {
            contentValues.put(MediaStore.MediaColumns.SIZE, size);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, true);
            LogUtils.d("imageCopy folderPath: " + folderPath);
        } else {
            String dataPath = getDataPath(folderPath, displayName);
            contentValues.put(MediaStore.MediaColumns.DATA, dataPath);
            LogUtils.d("imageCopy data path: " + dataPath);
        }
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
                inputStream = new FileInputStream(imageFile);
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
