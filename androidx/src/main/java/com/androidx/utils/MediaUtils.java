package com.androidx.utils;

import android.content.Context;
import android.media.ExifInterface;
import android.net.Uri;

import com.androidx.AndroidStorage;

import java.io.InputStream;

/**
 * user author: didikee
 * create time: 4/21/21 11:04 PM
 * description: 
 */
public final class MediaUtils {
    /**
     * 获取多媒体的文件类型
     * @param inputStream
     * @return
     */
    public static String getMimeType(InputStream inputStream) {
        return "";
    }

    /**
     * 读取图片属性：旋转的角度
     *
     * @param imageUri 图片绝对路径
     * @return degree旋转的角度
     */
    public static int getImageDegree(Context context, Uri imageUri) {
        int degree = 0;
        try {
            ExifInterface exifInterface;
            if (AndroidStorage.isAboveVersionQ()) {
                exifInterface = new ExifInterface(context.getContentResolver().openInputStream(imageUri));
            } else {
                exifInterface = new ExifInterface(UriUtils.getPathFromUri(context, imageUri));
            }
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }

    public static int getImageDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return degree;
    }


}
