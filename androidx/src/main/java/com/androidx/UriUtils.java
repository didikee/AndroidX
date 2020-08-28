package com.androidx;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.media.MediaUriInfo;
import com.androidx.media.MimeType;

import java.util.ArrayList;

/**
 * user author: didikee
 * create time: 2019-12-03 09:04
 * description: 
 */
public final class UriUtils {
    public static final String DATE_TAKEN = "datetaken";
    public static final Uri EXTERNAL_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final Uri EXTERNAL_VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    @Deprecated
    public static MediaUriInfo getMediaInfo(ContentResolver contentResolver, Uri uri) {
        if (contentResolver == null || uri == null) {
            return null;
        }
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
        return null;
    }

    /**
     * 获取多媒体文件的信息
     * 先根据媒体文件的类型去获取对应的信息
     * @param context
     * @param uri
     * @return
     */
    public static MediaUriInfo getMediaInfo(Context context, Uri uri) {
        return getMediaInfo(context, uri, "");
    }

    public static MediaUriInfo getMediaInfo(Context context, Uri uri, String mimeType) {
        if (context == null || uri == null) {
            LogUtils.e("UriUtils getMediaInfo() params is null");
            return null;
        }
        if (TextUtils.isEmpty(mimeType)) {
            // mimeType = getMimeType(context, uri);
            mimeType = getMimeType(context.getContentResolver(), uri);
            LogUtils.e("getMediaInfo mimeType: " + mimeType);
        }
        if (TextUtils.isEmpty(mimeType)) {
            return null;
        } else {
            if (MimeType.isImage(mimeType)) {
                return getImageInfo(context.getContentResolver(), uri);
            }
            if (MimeType.isVideo(mimeType)) {
                return getVideoInfo(context.getContentResolver(), uri);
            }
            if (MimeType.isAudio(mimeType)) {
                return getAudioInfo(context.getContentResolver(), uri);
            }
            // 当什么都不是的时候去获取基础信息
            return getBaseInfo(context.getContentResolver(), uri);
        }
    }

    private static String getMimeType(Context context, Uri uri) {
        String mimeType = "";
        MediaMetadataRetriever metadataRetriever = null;
        try {
            metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(context, uri);
            mimeType = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (metadataRetriever != null) {
                metadataRetriever.release();
            }
        }
        return mimeType;
    }

    private static String getMimeType(ContentResolver contentResolver, Uri uri) {
        String mimeType = "";
        String[] projections = new String[]{
                MediaStore.MediaColumns.MIME_TYPE,
        };
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projections, null, null, null);
            if (cursor == null) {
                LogUtils.e("getMimeType get a empty cursor: " + uri.toString());
            } else {
                if (cursor.moveToFirst()) {
                    mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return mimeType;
    }


    public static MediaUriInfo getImageInfo(ContentResolver contentResolver, Uri uri) {
        if (contentResolver != null && uri != null) {
            ArrayList<String> projections = getCommonProjects();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN);
            } else {
                projections.add(MediaStore.Images.Media.DATE_TAKEN);
            }
            projections.add(MediaStore.Images.Media.WIDTH);
            projections.add(MediaStore.Images.Media.HEIGHT);
            projections.add(MediaStore.Images.Media.ORIENTATION);
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(uri, projections.toArray(new String[projections.size()]), null, null, null);
                if (cursor == null) {
                    LogUtils.e("getImageInfo get a empty cursor: " + uri.toString());
                } else {
                    if (cursor.moveToFirst()) {
                        MediaUriInfo mediaUriInfo = new MediaUriInfo();
                        mediaUriInfo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                        mediaUriInfo.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)));
                        mediaUriInfo.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setRelativePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)));
                        } else {
                            mediaUriInfo.setData(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
                        }
                        mediaUriInfo.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)));
                        mediaUriInfo.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)));
                        mediaUriInfo.setDateModified(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)));

                        // custom
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(UriUtils.DATE_TAKEN)));
                        } else {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)));
                        }
                        mediaUriInfo.setWidth(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)));
                        mediaUriInfo.setHeight(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)));
                        mediaUriInfo.setRotate(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.ORIENTATION)));
                        return mediaUriInfo;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    public static MediaUriInfo getVideoInfo(ContentResolver contentResolver, Uri uri) {
        if (contentResolver != null && uri != null) {
            ArrayList<String> projections = getCommonProjects();

            projections.add(MediaStore.Video.Media.DURATION);
            projections.add(MediaStore.Video.Media.WIDTH);
            projections.add(MediaStore.Video.Media.HEIGHT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN);
                //在sdk >= 29 后才加入了视频的方向api,所以先不取这个字段
                // projections.add(MediaStore.Video.Media.ORIENTATION);
            } else {
                projections.add(MediaStore.Video.Media.DATE_TAKEN);
            }
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(uri, projections.toArray(new String[projections.size()]), null, null, null);
                if (cursor == null) {
                    LogUtils.e("getVideoInfo get a empty cursor: " + uri.toString());
                } else {

                    if (cursor.moveToFirst()) {
                        MediaUriInfo mediaUriInfo = new MediaUriInfo();
                        mediaUriInfo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                        mediaUriInfo.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)));
                        mediaUriInfo.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setRelativePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)));
                        } else {
                            mediaUriInfo.setData(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
                        }
                        mediaUriInfo.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)));
                        mediaUriInfo.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)));
                        mediaUriInfo.setDateModified(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)));

                        // custom
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(UriUtils.DATE_TAKEN)));
                        } else {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)));
                        }
                        mediaUriInfo.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
                        mediaUriInfo.setWidth(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)));
                        mediaUriInfo.setHeight(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)));
//                        mediaUriInfo.setRotate(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ORIENTATION)));
                        return mediaUriInfo;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    public static MediaUriInfo getAudioInfo(ContentResolver contentResolver, Uri uri) {
        if (contentResolver != null && uri != null) {
            ArrayList<String> projections = getCommonProjects();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN);
            } else {
                // android 10 开始才有
                // do nothing
            }
            projections.add(MediaStore.Audio.Media.DURATION);
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(uri, projections.toArray(new String[projections.size()]), null, null, null);
                if (cursor == null) {
                    LogUtils.w("getAudioInfo get a empty cursor: " + uri.toString());
                } else {

                    if (cursor.moveToFirst()) {
                        MediaUriInfo mediaUriInfo = new MediaUriInfo();
                        mediaUriInfo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                        mediaUriInfo.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)));
                        mediaUriInfo.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setRelativePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)));
                        } else {
                            mediaUriInfo.setData(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
                        }
                        mediaUriInfo.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)));
                        mediaUriInfo.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)));
                        mediaUriInfo.setDateModified(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)));

                        // custom
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(UriUtils.DATE_TAKEN)));
                        } else {
                            mediaUriInfo.setDateTaken(0/*低于android10的没有这个字段*/);
                        }
                        mediaUriInfo.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
                        return mediaUriInfo;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    public static MediaUriInfo getBaseInfo(ContentResolver contentResolver, Uri uri) {
        if (contentResolver != null && uri != null) {
            ArrayList<String> projections = getCommonProjects();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN);
            } else {
                projections.add(DATE_TAKEN);
            }
            Cursor cursor = null;
            try {
                cursor = contentResolver.query(uri, projections.toArray(new String[projections.size()]), null, null, null);
                if (cursor == null) {
                    LogUtils.e("getBaseInfo get a empty cursor: " + uri.toString());
                } else {

                    if (cursor.moveToFirst()) {
                        MediaUriInfo mediaUriInfo = new MediaUriInfo();
                        mediaUriInfo.setId(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)));
                        mediaUriInfo.setDisplayName(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)));
                        mediaUriInfo.setMimeType(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)));
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setRelativePath(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH)));
                        } else {
                            mediaUriInfo.setData(cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)));
                        }
                        mediaUriInfo.setSize(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)));
                        mediaUriInfo.setDateAdded(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED)));
                        mediaUriInfo.setDateModified(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)));

                        // custom
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(UriUtils.DATE_TAKEN)));
                        } else {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(DATE_TAKEN)));
                        }
                        return mediaUriInfo;
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }

    /**
     * 获取基础的数据
     * @return
     */
    private static ArrayList<String> getCommonProjects() {
        ArrayList<String> projection = new ArrayList<>();
        projection.add(MediaStore.MediaColumns._ID);
        projection.add(MediaStore.MediaColumns.DISPLAY_NAME);
        projection.add(MediaStore.MediaColumns.MIME_TYPE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            projection.add(MediaStore.MediaColumns.RELATIVE_PATH);
        } else {
            projection.add(MediaStore.MediaColumns.DATA);
        }
        projection.add(MediaStore.MediaColumns.SIZE);
        projection.add(MediaStore.MediaColumns.DATE_ADDED);
        projection.add(MediaStore.MediaColumns.DATE_MODIFIED);
        return projection;
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
