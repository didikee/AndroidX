package com.androidx;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.androidx.media.MediaUriInfo;

import java.util.ArrayList;

/**
 * user author: didikee
 * create time: 2019-12-03 09:04
 * description: 
 */
public final class UriUtils {

    public static MediaUriInfo getMediaInfo(ContentResolver contentResolver, Uri uri) {
        if (contentResolver != null && uri != null) {
            ArrayList<String> projection = new ArrayList<>();
            projection.add(MediaStore.MediaColumns._ID);/*0*/
            projection.add(MediaStore.MediaColumns.DISPLAY_NAME);/*1*/
            projection.add(MediaStore.MediaColumns.MIME_TYPE);/*2*/
            projection.add(MediaStore.MediaColumns.WIDTH);/*3*/
            projection.add(MediaStore.MediaColumns.HEIGHT);/*4*/
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projection.add(MediaStore.MediaColumns.RELATIVE_PATH);/*5*/
            } else {
                projection.add(MediaStore.MediaColumns.DATA);/*5*/
            }
            projection.add(MediaStore.MediaColumns.SIZE);/*6*/
            projection.add(MediaStore.MediaColumns.DATE_ADDED);/*7*/
            projection.add(MediaStore.MediaColumns.DATE_MODIFIED);/*8*/

            Cursor cursor = contentResolver.query(uri, projection.toArray(new String[projection.size()]), null, null, null);
            if (cursor == null) {
                LogUtils.w("queryUriDisplayName get a empty cursor.");
            } else {
                try {
                    if (cursor.moveToFirst()) {
                        MediaUriInfo mediaUriInfo = new MediaUriInfo();
                        mediaUriInfo.setId(cursor.getLong(0));
                        mediaUriInfo.setDisplayName(cursor.getString(1));
                        mediaUriInfo.setMimeType(cursor.getString(2));
                        mediaUriInfo.setWidth(cursor.getInt(3));
                        mediaUriInfo.setHeight(cursor.getInt(3));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setRelativePath(cursor.getString(5));
                        } else {
                            mediaUriInfo.setData(cursor.getString(5));
                        }
                        mediaUriInfo.setSize(cursor.getLong(6));
                        mediaUriInfo.setDateAdded(cursor.getLong(7));
                        mediaUriInfo.setDateModified(cursor.getLong(8));
                        return mediaUriInfo;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    cursor.close();
                }
            }
        }
        return null;
    }

    /**
     * 调用第三方的视频播放器播放视频
     * @param activity
     * @param videoUri
     * @return
     */
    public static boolean playVideo(Activity activity, Uri videoUri) {
        if (activity == null || videoUri == null) {
            return false;
        }
        Intent videoIntent = new Intent(Intent.ACTION_VIEW);
        videoIntent.setDataAndType(videoUri, "video/*");
        videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try {
            activity.startActivity(Intent.createChooser(videoIntent, ""));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

}
