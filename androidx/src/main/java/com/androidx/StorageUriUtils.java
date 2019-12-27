package com.androidx;

import android.content.ContentValues;
import android.os.Build;
import android.provider.MediaStore;

/**
 * user author: didikee
 * create time: 2019-11-29 18:02
 * description: 处理保存uri的低版本和高版本的兼容问题
 */
public final class StorageUriUtils {

    @Deprecated
    public static ContentValues makeImageValues(String folderPath, String filename, String mimeType, int width, int height, long fileLength) {
        return makeMediaValues(folderPath, filename, mimeType, width, height, fileLength);
    }

    @Deprecated
    public static ContentValues makeVideoValues(String folderPath, String filename, long fileLength) {
        return makeMediaValues(folderPath, filename, "video/mp4", 0, 0, fileLength);
    }


    /**
     * 支持图片和视频
     * @param folderPath android(>=10): Environment.DIRECTORY_PICTURES + separator + filename
     *             android(<10):  File pictureDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
     *                            if (!pictureDir.exists()) {
     *                                 pictureDir.mkdirs();
     *                            }
     *                            String absolutePath = pictureDir.getAbsolutePath() + separator + gifFile.getName();
     * @param filename
     * @param mimeType
     * @param fileLength
     */
    public static ContentValues makeMediaValues(String folderPath, String filename, String mimeType, int width, int height, long fileLength) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        if (width > 0 && height > 0) {
            values.put(MediaStore.MediaColumns.WIDTH, width);
            values.put(MediaStore.MediaColumns.HEIGHT, height);
        }
        if (fileLength > 0) {
            values.put(MediaStore.MediaColumns.SIZE, fileLength);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            //        Set<String> externalVolumeNames = MediaStore.getExternalVolumeNames(context);
            values.put(MediaStore.Images.Media.RELATIVE_PATH, folderPath);
            values.put(MediaStore.MediaColumns.IS_PENDING, true);
        } else {
            String data = StorageSaveUtils.getDataPath(folderPath, filename);
            values.put(MediaStore.MediaColumns.DATA, data);
        }
        //Uri uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        return values;
    }


}
