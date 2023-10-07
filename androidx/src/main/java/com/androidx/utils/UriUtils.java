package com.androidx.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.CustomFileProvider;
import com.androidx.LogUtils;
import com.androidx.media.MagicBytes;
import com.androidx.media.MediaUriInfo;
import com.androidx.media.MimeType;
import com.androidx.media.VideoMetaData;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * user author: didikee
 * create time: 2019-12-03 09:04
 * description: 
 */
public final class UriUtils {
    public static final String DATE_TAKEN = "datetaken";
    public static final Uri EXTERNAL_IMAGE_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    public static final Uri EXTERNAL_VIDEO_URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private UriUtils() {
    }

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
            mimeType = queryMimeType(context.getContentResolver(), uri);
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

    /**
     * 获取文件的类型
     * @param context
     * @param uri
     * @return
     */
    public static String getMimeType(Context context, Uri uri) {
        String mimeType = parseMimeType(context, uri);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = queryMimeType(context.getContentResolver(), uri);
        }
        return mimeType;
    }

    @WorkerThread
    public static String getMimeTypeFromFileHeader(ContentResolver resolver, Uri uri) {
        try {
            InputStream inputStream = resolver.openInputStream(uri);
            return getMimeTypeFromFileHeader(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    @WorkerThread
    public static String getMimeTypeFromFileHeader(InputStream inputStream) {
        byte[] extract = MagicBytes.extract(inputStream, 512 + 128);
        String type = MagicBytes.getType(extract);
        return MimeType.getMimeTypeFromExtension(type);
    }

    private static String parseMimeType(Context context, Uri uri) {
        String mimeType = "";
        MediaMetadataRetriever metadataRetriever = null;
        try {
            metadataRetriever = new MediaMetadataRetriever();
            metadataRetriever.setDataSource(context, uri);
            mimeType = metadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(metadataRetriever);
        }
        return mimeType;
    }

    private static String queryMimeType(ContentResolver contentResolver, Uri uri) {
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

    /**
     * 旋转方向的字段在api29开始才加入，所以目前还是推荐通过媒体解析得到视频的基本媒体信息，文件信息的话倒是可以使用uri查询的形式
     * @param contentResolver
     * @param uri
     * @return
     */
    public static MediaUriInfo getVideoInfo(ContentResolver contentResolver, Uri uri) {
        if (contentResolver != null && uri != null) {
            ArrayList<String> projections = getCommonProjects();

            projections.add(MediaStore.Video.Media.DURATION);
            projections.add(MediaStore.Video.Media.WIDTH);
            projections.add(MediaStore.Video.Media.HEIGHT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                projections.add(UriUtils.DATE_TAKEN);
                //在sdk >= 29 后才加入了视频的方向api,所以先不取这个字段
                projections.add(MediaStore.Video.Media.ORIENTATION);
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
                            mediaUriInfo.setRotate(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.ORIENTATION)));
                        } else {
                            mediaUriInfo.setDateTaken(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_TAKEN)));
                        }
                        mediaUriInfo.setDuration(cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)));
                        mediaUriInfo.setWidth(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH)));
                        mediaUriInfo.setHeight(cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT)));
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
     * 打开任意类的文件请使用
     * @param activity
     * @param videoUri
     * @return
     */
    @Deprecated
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

    public static void openWith(@NonNull Context context, @NonNull Uri uri) throws Exception {
        openWith(context, uri, null, null);
    }

    /**
     * 试图打开任意的uri
     * @param context
     * @param uri
     * @param mimeType
     * @param title
     * @throws Exception
     */
    public static void openWith(@NonNull Context context, @NonNull Uri uri, @Nullable String mimeType, @Nullable String title) throws Exception {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (TextUtils.isEmpty(mimeType)) {
            MediaUriInfo baseInfo = getBaseInfo(context.getContentResolver(), uri);
            if (baseInfo != null) {
                if (TextUtils.isEmpty(baseInfo.getMimeType())) {
                    if (!TextUtils.isEmpty(baseInfo.getDisplayName())) {
                        mimeType = MimeType.getMimeTypeFromFilename(baseInfo.getDisplayName());
                    }
                } else {
                    mimeType = baseInfo.getMimeType();
                }
            }
        }
        if (TextUtils.isEmpty(mimeType)) {
            String pathFromUri = getPathFromUri(context, uri);
            if (!TextUtils.isEmpty(pathFromUri)) {
                File file = new File(pathFromUri);
                if (file.exists()) {
                    mimeType = MimeType.getMimeTypeFromFilename(file.getName());
                }
            }
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = "*/*";
        }
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(Intent.createChooser(intent, TextUtils.isEmpty(title) ? "" : title));
    }

    /**
     * 获取视频的信息
     * @param context
     * @param videoUri
     * @return
     */
    @NonNull
    public static VideoMetaData getVideoMetaData(Context context, Uri videoUri) {
        return MediaMetadataHelper.getVideoMetaData(context, videoUri);
    }

    @NonNull
    public static VideoMetaData getVideoMetaData(File videoFile) {
        return MediaMetadataHelper.getVideoMetaData(videoFile);
    }


    /**
     * 对旧版api设计的获取java file的绝对路径
     * @param context
     * @param uri
     * @return
     */
    public static String getPathFromUri(Context context, Uri uri) {
        //得到uri，后面就是将uri转化成file的过程
        if (context == null || uri == null) {
            return null;
        }
        String pathFromUri;
        int sdkInt = Build.VERSION.SDK_INT;
        if (sdkInt >= 19) {
            pathFromUri = Uri2Path.getRealPathFromURI_API19(context, uri);
        } else if (sdkInt >= 11 && sdkInt < 19) {
            pathFromUri = Uri2Path.getRealPathFromURI_API11to18(context, uri);
        } else {
            pathFromUri = Uri2Path.getRealPathFromURI_BelowAPI11(context, uri);
        }
        return pathFromUri;
    }

    /**
     * 这个需要测试
     * @param context
     * @param file
     * @return
     */
    public static Uri getUriFrom(Context context, File file) {
        return getUriFrom(context, context.getPackageName() + ".FileProvider", file);
    }

    public static Uri getUriFrom(Context context, @NonNull String authority, File file) {
        Uri uri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            uri = CustomFileProvider.getUriForFile(context, authority, file);
        } else {
            uri = Uri.fromFile(file);
        }
        return uri;
    }

    @Nullable
    public static FileDescriptor getFileDescriptor(ContentResolver resolver, Uri uri, boolean write) {
        try {
            ParcelFileDescriptor parcelFileDescriptor = resolver.openFileDescriptor(uri, write ? "w" : "r");
            if (parcelFileDescriptor != null) {
                return parcelFileDescriptor.getFileDescriptor();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 从asset路径创建uri
     * @param assetPath file:///android_asset/${RELATIVEPATH}
     * @return
     */
    public static Uri createUri(String assetPath) {
        if (TextUtils.isEmpty(assetPath)) {
            assetPath = "";/*avoid exception*/
        }
        return Uri.parse("file:///android_asset/" + assetPath);
    }
}
