package com.androidx;

import android.content.ContentValues;
import android.media.MediaMetadataRetriever;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.media.MimeType;

import java.io.File;

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
     * 获取音频的参数
     * @return
     */
    public static ContentValues makeAudioValues(String folderPath, String filename, String mimeType, long duration, long fileLength) {
        ContentValues values = makeBaseValues(folderPath, filename, mimeType, fileLength);
        values.put(MediaStore.MediaColumns.DURATION, duration);
        return values;
    }


    /**
     * 获取图片的参数
     * @return
     */
    public static ContentValues makeImageValues(String folderPath, String filename, String mimeType, int width, int height, int rotate, long fileLength) {
        ContentValues values = makeBaseValues(folderPath, filename, mimeType, fileLength);
        values.put(MediaStore.MediaColumns.WIDTH, width);
        values.put(MediaStore.MediaColumns.HEIGHT, height);
        values.put(MediaStore.MediaColumns.ORIENTATION, rotate);
        return values;
    }

    /**
     * 获取视频的信息
     * @return
     */
    public static ContentValues makeVideoValues(String folderPath, String filename, String mimeType, int width, int height, long duration, int rotate, long fileLength) {
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "video/mp4";
        }
        ContentValues values = makeBaseValues(folderPath, filename, mimeType, fileLength);
        values.put(MediaStore.MediaColumns.WIDTH, width);
        values.put(MediaStore.MediaColumns.HEIGHT, height);
        values.put(MediaStore.MediaColumns.ORIENTATION, rotate);
        values.put(MediaStore.MediaColumns.DURATION, duration);
        return values;
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
    @Deprecated
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
            values.put(MediaStore.Images.Media.RELATIVE_PATH, folderPath);
            values.put(MediaStore.MediaColumns.IS_PENDING, true);
        } else {
            String data = StorageSaveUtils.getDataPath(folderPath, filename);
            values.put(MediaStore.MediaColumns.DATA, data);
        }
        return values;
    }


    /**
     * 获取最基础的媒体信息
     * @param folderPath
     * @param filename
     * @param mimeType
     * @param fileLength
     * @return
     */
    public static ContentValues makeBaseValues(String folderPath, String filename, String mimeType, long fileLength) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.TITLE, filename);
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);
        values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
        values.put(MediaStore.MediaColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
        if (fileLength > 0) {
            values.put(MediaStore.MediaColumns.SIZE, fileLength);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, folderPath);
            values.put(MediaStore.MediaColumns.IS_PENDING, true);
        } else {
            String data = StorageSaveUtils.getDataPath(folderPath, filename);
            values.put(MediaStore.MediaColumns.DATA, data);
        }
        return values;
    }

    public static ContentValues getContentValues(File file, String folderPath, String filename, String mimeType) {
        if (file == null || TextUtils.isEmpty(folderPath)) {
            return null;
        }
        if (TextUtils.isEmpty(filename)) {
            filename = file.getName();
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = MimeType.getMimeTypeFromFilename(filename);
        }
        if (MimeType.isVideo(mimeType)) {
            long duration = 0;
            int width = 0;
            int height = 0;
            int rotate = 0;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(file.getAbsolutePath());
                duration = parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION), 0);
                width = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH), 0);
                height = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT), 0);
                rotate = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return makeVideoValues(folderPath, filename, mimeType, width, height, duration, rotate, file.length());
        }
        if (MimeType.isAudio(mimeType)) {
            long duration = 0;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(file.getAbsolutePath());
                duration = Long.parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return makeAudioValues(folderPath, filename, mimeType, duration, file.length());
        }
        if (MimeType.isImage(mimeType)) {
            int width = 0;
            int height = 0;
            int rotate = 0;
            try {
                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                retriever.setDataSource(file.getAbsolutePath());
                width = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_WIDTH), 0);
                height = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_HEIGHT), 0);
                rotate = parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_IMAGE_ROTATION), 0);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return makeImageValues(folderPath, filename, mimeType, width, height, rotate, file.length());
        }
        return makeBaseValues(folderPath, filename, mimeType, file.length());
    }

    private static int parseInt(String num, int defaultValue) {
        try {
            return Integer.parseInt(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }

    private static long parseLong(String num, long defaultValue) {
        try {
            return Long.parseLong(num);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}
