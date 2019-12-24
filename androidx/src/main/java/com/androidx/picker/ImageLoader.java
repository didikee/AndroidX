package com.androidx.picker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.R;

import java.io.File;
import java.util.ArrayList;

/**
 * user author: didikee
 * create time: 2019-07-18 13:43
 * description: 获取手机里的视频
 */
public class ImageLoader {
    public static final int IMAGE = 0;
    public static final int IMAGE_WITHOUT_GIF = 1;
    public static final int GIF = 2;
    private int mType;

    public ArrayList<MediaFolder> load(Context context, int type) {
        this.mType = type;
        if (mType < 0 || mType > 2) {
            return null;
        }
        String mimeType;
        if (mType == GIF) {
            mimeType = "image/gif";
        } else {
            mimeType = "";
        }
        return load(context, mimeType);
    }

    private ArrayList<MediaFolder> load(Context context, String mimeType) {
        if (context == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return null;
        }
        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        String order = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        String selection;
        String[] selectionArgs;
        if (TextUtils.isEmpty(mimeType)) {
            // 空表示选择全部
            selectionArgs = null;
            selection = null;
        } else {
            selectionArgs = new String[]{mimeType};
            selection = MediaStore.MediaColumns.MIME_TYPE + "=?";
        }

        // projections
        ArrayList<String> projections = new ArrayList<>();
        projections.add(MediaStore.MediaColumns._ID);
        projections.add(MediaStore.MediaColumns.DISPLAY_NAME);
        projections.add(MediaStore.MediaColumns.SIZE);
        projections.add(MediaStore.MediaColumns.WIDTH);
        projections.add(MediaStore.MediaColumns.HEIGHT);
        projections.add(MediaStore.MediaColumns.MIME_TYPE);
        projections.add(MediaStore.MediaColumns.DATE_ADDED);
        projections.add(MediaStore.MediaColumns.DATE_MODIFIED);
        if (Build.VERSION.SDK_INT >= 29/*android 10*/) {
            // 相对路径   /storage/0000-0000/DCIM/Vacation/IMG1024.JPG} would have a path of {@code DCIM/Vacation/}.
            projections.add(MediaStore.MediaColumns.RELATIVE_PATH);
        } else {
            // 真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
            projections.add(MediaStore.MediaColumns.DATA);
        }

        Cursor cursor = contentResolver.query(externalContentUri, projections.toArray(new String[projections.size()]), selection, selectionArgs, order);
        if (cursor == null) {
            return null;
        }

        ArrayList<MediaFolder> mediaFolders = new ArrayList<>();
        ArrayList<MediaItem> allMedias = new ArrayList<>();
        while (cursor.moveToNext()) {
            //查询数据
            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            String mimeTypeName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));

            // 现在全部改为uri来实现
            Uri uri = ContentUris.withAppendedId(externalContentUri, Long.parseLong(id));

            if (mType == IMAGE_WITHOUT_GIF && isGif(displayName, mimeTypeName)) {
                continue;
            }

            String data = "";
            String relativePath = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH));
            } else {
                data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
            // 获取父目录的信息,用于文件夹分类
            String parentName = "";
            String parentPath = "";
            if (!TextUtils.isEmpty(data)) {
                // 根据java系统来判断
                File file = new File(data);
                if (!file.exists() || file.length() <= 0) {
                    continue;
                }
                File imageParentFile = file.getParentFile();
                if (imageParentFile == null) {
                    continue;
                }
                parentName = imageParentFile.getName();
                parentPath = imageParentFile.getAbsolutePath();
            }
            if (!TextUtils.isEmpty(relativePath)) {
                // 根据相对路径来判断
                // DCIM/MY FOLDER/SUB/demo.png
                // android 10 DCIM/MY FOLDER/SUB
                if (!TextUtils.isEmpty(relativePath) && relativePath.contains(File.separator)) {
                    int lastIndexOf = relativePath.lastIndexOf(File.separator);
                    if (lastIndexOf != -1) {
                        parentName = relativePath.substring(lastIndexOf + 1);
                        parentPath = relativePath.substring(0, lastIndexOf);
                    }
//                    if (split.length > 0) {
//                        parentName = split[split.length - 1];
//                    }
                } else {
                    parentName = relativePath;
                    parentPath = relativePath;
                }
            }

            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
            int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.HEIGHT));
            long dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
            long dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

            //封装实体
            MediaItem mediaItem = new MediaItem(uri);
            mediaItem.setDisplayName(displayName);
            mediaItem.setSize(size);
            mediaItem.setWidth(width);
            mediaItem.setHeight(height);
            mediaItem.setMimeType(mimeType);
            mediaItem.setDateAdded(dateAdded);
            mediaItem.setDateModified(dateModified);
            mediaItem.setData(data);
            mediaItem.setRelativePath(relativePath);


            allMedias.add(mediaItem);

            //根据父路径分类存放图片
            MediaFolder mediaFolder = new MediaFolder();
            mediaFolder.name = parentName;
            mediaFolder.path = parentPath;

            int indexOf = mediaFolders.indexOf(mediaFolder);
            if (indexOf == -1) {
                ArrayList<MediaItem> images = new ArrayList<>();
                images.add(mediaItem);
                mediaFolder.images = images;
                mediaFolders.add(mediaFolder);
            } else {
                // contain
                mediaFolders.get(indexOf).images.add(mediaItem);
            }
        }
        // 防止没有图片报异常
        if (cursor.getCount() > 0 && allMedias.size() > 0) {
            //构造所有图片的集合
            MediaFolder allImagesFolder = new MediaFolder();
            allImagesFolder.name = context.getString(R.string.recently);
            allImagesFolder.path = "";
            allImagesFolder.images = allMedias;
            mediaFolders.add(0, allImagesFolder);  //确保第一条是所有图片
        }
        cursor.close();
        return mediaFolders;
    }

    /**
     * 判断是否为gif
     * @param displayName
     * @param mimeType
     * @return
     */
    private boolean isGif(String displayName, String mimeType) {
        if (!TextUtils.isEmpty(displayName) && displayName.toLowerCase().endsWith(".gif")) {
            return true;
        }
        return false;
    }
}
