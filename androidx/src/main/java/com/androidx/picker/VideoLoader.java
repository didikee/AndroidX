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
public class VideoLoader {

    public ArrayList<MediaFolder> getVideos(Context context) {
        return getVideos(context, "");
    }

    public ArrayList<MediaFolder> getVideos(Context context, String folderPath) {
        if (context == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return null;
        }

        Uri externalContentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String order = MediaStore.MediaColumns.DATE_ADDED + " DESC";
        String selection = MediaStore.MediaColumns.MIME_TYPE + "=?";
        String[] selectionArgs = new String[]{"video/mp4"};

        // projections
        ArrayList<String> projections = new ArrayList<>();
        projections.add(MediaStore.Video.Media._ID);
        projections.add(MediaStore.Video.Media.DISPLAY_NAME);
        projections.add(MediaStore.Video.Media.SIZE);
        projections.add(MediaStore.Video.Media.WIDTH);
        projections.add(MediaStore.Video.Media.HEIGHT);
        projections.add(MediaStore.Video.Media.MIME_TYPE);
        projections.add(MediaStore.Video.Media.DATE_ADDED);
        projections.add(MediaStore.Video.Media.DATE_MODIFIED);
        projections.add(MediaStore.Video.Media.DURATION);
        /**
         * MediaStore.Video.Media.RELATIVE_PATH
         * 相对路径   /storage/0000-0000/DCIM/Vacation/IMG1024.JPG} would have a path of {@code DCIM/Vacation/}.
         *
         * MediaStore.Video.Media.DATA 在android10上已经过时了
         * 真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
         */
        if (Build.VERSION.SDK_INT >= 29/*android 10*/) {
            projections.add(MediaStore.Video.Media.RELATIVE_PATH);
        } else {
            projections.add(MediaStore.Video.Media.DATA);
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
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME));

            // 现在全部改为uri来实现
            Uri uri = ContentUris.withAppendedId(externalContentUri, Long.parseLong(id));

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

            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE));
            int width = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.WIDTH));
            int height = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.HEIGHT));
            String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.MIME_TYPE));
            long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION));
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
            if (duration != -1) {
                mediaItem.setDuration(duration);
            }

            allMedias.add(mediaItem);

            //根据父路径分类存放图片
            MediaFolder mediaFolder = new MediaFolder();
            mediaFolder.name = parentName;
            mediaFolder.path = parentPath;

            if (mediaFolders.contains(mediaFolder)) {
                mediaFolders.get(mediaFolders.indexOf(mediaFolder)).images.add(mediaItem);
            } else {
                ArrayList<MediaItem> images = new ArrayList<>();
                images.add(mediaItem);
                mediaFolder.images = images;
                mediaFolders.add(mediaFolder);
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
}
