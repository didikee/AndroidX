package com.androidx.storage;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.androidx.MediaStoreUtils;

import java.io.IOException;
import java.io.OutputStream;

/**
 * user author: didikee
 * create time: 4/23/20 9:15 AM
 * description: 
 */
public class SaveHelper {
    /**
     * 保存照片
     * @param contentResolver
     * @param contentValues
     * @param bytes
     * @return
     */
    public static Uri saveImage(ContentResolver contentResolver, ContentValues contentValues, byte[] bytes) {
        Uri externalContentUri = MediaStoreUtils.INSTANCE.getEXTERNAL_IMAGE_PRIMARY_URI();
        Uri uri = contentResolver.insert(externalContentUri, contentValues);
        if (uri == null) {
            return null;
        }
        OutputStream outputStream = null;
        try {
            outputStream = contentResolver.openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(bytes);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
                    contentResolver.update(uri, contentValues, null, null);
                }
                return uri;
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri, null, null);
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
}
