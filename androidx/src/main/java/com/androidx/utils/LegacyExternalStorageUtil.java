package com.androidx.utils;

import android.database.Cursor;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.androidx.media.MediaUriInfo;

/**
 * user author: didikee
 * create time: 4/18/20 9:04 PM
 * description: 
 */
public class LegacyExternalStorageUtil {

    public static boolean isJavaStorageEnable() {
        if (Build.VERSION.SDK_INT >= 29) {
            return Environment.isExternalStorageLegacy();
        } else {
            return true;
        }
    }

    public static void handleMediaPath(Cursor cursor, MediaUriInfo mediaUriInfo) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (isJavaStorageEnable()) {
                mediaUriInfo.setData(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
            } else {
                mediaUriInfo.setRelativePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)));
            }
        } else {
            mediaUriInfo.setData(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
        }
    }
}
