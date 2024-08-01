package com.androidx.picker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.AndroidUtils;
import com.androidx.LogUtils;
import com.androidx.R;

import java.util.ArrayList;

/**
 * user author: didikee
 * create time: 2019-07-18 13:43
 * description: 获取手机里的视频
 */
@Deprecated
public class ImageLoader extends AbsMediaLoader {
    public static final int IMAGE = 0;
    public static final int IMAGE_WITHOUT_GIF = 1;
    public static final int GIF = 2;
    private int mType;
    private String mMimeType = "";

    public ArrayList<MediaFolder> load(Context context, int type) {
        this.mType = type;
        if (mType < 0 || mType > 2) {
            return null;
        }
        if (mType == GIF) {
            mMimeType = "image/gif";
        }
        return load(context);
    }

    private ArrayList<MediaFolder> load(Context context) {
        if (context == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return null;
        }
        final boolean isAndroid10 = AndroidUtils.isAndroid10();
        final boolean externalStorageLegacy = AndroidUtils.isExternalStorageLegacy();
        Uri externalContentUri = getContentUri();
        String order = getOrder();
        String selection = getSelection();
        String[] selectionArgs = getSelectionArgs();
        // common projections
        ArrayList<String> projections = new ArrayList<>();
        projections.add(MediaStore.MediaColumns._ID);
        projections.add(MediaStore.MediaColumns.DISPLAY_NAME);
        projections.add(MediaStore.MediaColumns.SIZE);
        projections.add(MediaStore.MediaColumns.MIME_TYPE);
        projections.add(MediaStore.MediaColumns.DATE_ADDED);
        projections.add(MediaStore.MediaColumns.DATE_MODIFIED);
        if (isAndroid10) {
            if (externalStorageLegacy) {
                projections.add(MediaStore.MediaColumns.DATA);
            } else {
                // 相对路径   /storage/0000-0000/DCIM/Vacation/IMG1024.JPG} would have a path of {@code DCIM/Vacation/}.
                projections.add(MediaStore.MediaColumns.RELATIVE_PATH);
            }
        } else {
            // 真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            projections.add(MediaStore.MediaColumns.DATA);
        }
        // 添加特定类型的参数
        addProjections(projections);
        // 确定他们的index
        final int index_ID = projections.indexOf(MediaStore.MediaColumns._ID);
        final int index_DISPLAY_NAME = projections.indexOf(MediaStore.MediaColumns.DISPLAY_NAME);
        final int index_SIZE = projections.indexOf(MediaStore.MediaColumns.SIZE);
        final int index_MIME_TYPE = projections.indexOf(MediaStore.MediaColumns.MIME_TYPE);
        final int index_DATE_ADDED = projections.indexOf(MediaStore.MediaColumns.DATE_ADDED);
        final int index_DATE_MODIFIED = projections.indexOf(MediaStore.MediaColumns.DATE_MODIFIED);
        final int index_DATA = projections.indexOf(MediaStore.MediaColumns.DATA);
        final int index_RELATIVE_PATH = projections.indexOf(MediaStore.MediaColumns.RELATIVE_PATH);

        Cursor cursor = contentResolver.query(externalContentUri, projections.toArray(new String[projections.size()]), selection, selectionArgs, order);
        if (cursor == null) {
            return null;
        }

        ArrayList<MediaFolder> mediaFolders = new ArrayList<>();
        ArrayList<MediaItem> allMedias = new ArrayList<>();
        while (cursor.moveToNext()) {
            //查询数据
            String id = cursor.getString(index_ID);
            String displayName = cursor.getString(index_DISPLAY_NAME);
            String mimeType = cursor.getString(index_MIME_TYPE);

            // 现在全部改为uri来实现
            Uri uri = ContentUris.withAppendedId(externalContentUri, Long.parseLong(id));
            if (mType == IMAGE_WITHOUT_GIF && isGif(displayName, mimeType)) {
                continue;
            }
            String data = "";
            String relativePath = "";
            if (isAndroid10) {
                if (externalStorageLegacy) {
                    data = cursor.getString(index_DATA);
                } else {
                    relativePath = cursor.getString(index_RELATIVE_PATH);
                }
            } else {
                data = cursor.getString(index_DATA);
            }
            // 获取父目录的信息,用于文件夹分类
            String parentName = "";
            String parentPath = "";
            if (!TextUtils.isEmpty(data)) {
                String[] parentInfo = getParentInfoFromData(data);
                parentName = parentInfo[0];
                parentPath = parentInfo[1];
            }

            if (!TextUtils.isEmpty(relativePath)) {
                String[] parentInfo = getParentInfoFromRelativePath(relativePath);
                parentName = parentInfo[0];
                parentPath = parentInfo[1];
            }
            MediaItem mediaItem = new MediaItem(uri);
            // 这些是公用的参数
            long size = cursor.getLong(index_SIZE);
            long dateAdded = cursor.getLong(index_DATE_ADDED);
            long dateModified = cursor.getLong(index_DATE_MODIFIED);

            // 设置公共参数
            mediaItem.setDisplayName(displayName);
            mediaItem.setSize(size);
            mediaItem.setMimeType(mimeType);
            mediaItem.setDateAdded(dateAdded);
            mediaItem.setDateModified(dateModified);
            mediaItem.setData(data);
            mediaItem.setRelativePath(relativePath);
            // 添加专有参数
            bindCursorData(cursor, mediaItem);

            allMedias.add(mediaItem);

            //根据父路径分类存放图片
            MediaFolder mediaFolder = new MediaFolder();
            mediaFolder.name = parentName;
            mediaFolder.path = parentPath;

            int indexOf = mediaFolders.indexOf(mediaFolder);
            if (indexOf == -1) {
                ArrayList<MediaItem> images = new ArrayList<>();
                images.add(mediaItem);
                mediaFolder.items = images;
                mediaFolders.add(mediaFolder);
            } else {
                // contain
                mediaFolders.get(indexOf).items.add(mediaItem);
            }
        }
        // 防止没有图片报异常
        if (cursor.getCount() > 0 && allMedias.size() > 0) {
            //构造所有图片的集合
            MediaFolder allImagesFolder = new MediaFolder();
            allImagesFolder.name = context.getString(R.string.recently);
            allImagesFolder.path = "";
            allImagesFolder.items = allMedias;
            mediaFolders.add(0, allImagesFolder);  //确保第一条是所有图片
            LogUtils.d("all items: " + allMedias.size());
        }
        cursor.close();
        return mediaFolders;
    }


    /**
     * 判断是否为gif
     *
     * @param displayName
     * @param mimeType
     * @return
     */
    protected boolean isGif(String displayName, String mimeType) {
        if (!TextUtils.isEmpty(displayName) && displayName.toLowerCase().endsWith(".gif")) {
            return true;
        }
        return false;
    }

    @Override
    protected Uri getContentUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String getOrder() {
        return MediaStore.MediaColumns.DATE_ADDED + " DESC";
    }

    @Override
    protected String getSelection() {
        if (TextUtils.isEmpty(mMimeType)) {
            // 空表示选择全部
            return null;
        } else {
            return MediaStore.MediaColumns.MIME_TYPE + "=?";
        }
    }

    @Override
    protected String[] getSelectionArgs() {
        if (TextUtils.isEmpty(mMimeType)) {
            // 空表示选择全部
            return null;
        } else {
            return new String[]{mMimeType};
        }
    }

    @Override
    protected void addProjections(ArrayList<String> projections) {
        projections.add(MediaStore.MediaColumns.WIDTH);
        projections.add(MediaStore.MediaColumns.HEIGHT);
    }

    @Override
    protected void bindCursorData(Cursor cursor, MediaItem mediaItem) {
        // 这些和类型绑定的
        int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH));
        int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT));

        //封装实体
        mediaItem.setWidth(width);
        mediaItem.setHeight(height);
    }

    @Override
    public ArrayList<MediaFolder> get(Context context, String folderPath) {
        return load(context, IMAGE);
    }
}
