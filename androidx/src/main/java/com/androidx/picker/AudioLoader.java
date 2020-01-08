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

import java.util.ArrayList;

/**
 * user author: didikee
 * create time: 2020-01-6 1:24
 * description: 获取手机里的音频
 */
public class AudioLoader extends AbsMediaLoader {

    @Override
    public ArrayList<MediaFolder> get(Context context, String folderPath) {
        if (context == null) {
            return null;
        }
        ContentResolver contentResolver = context.getContentResolver();
        if (contentResolver == null) {
            return null;
        }

        Uri externalContentUri = getContentUri();
        String order = getOrder();
        String selection = getSelection();
        String[] selectionArgs = getSelectionArgs();

        // projections
        ArrayList<String> projections = new ArrayList<>();
        projections.add(MediaStore.Audio.Media._ID);
        projections.add(MediaStore.Audio.Media.DISPLAY_NAME);
        projections.add(MediaStore.Audio.Media.SIZE);
        projections.add(MediaStore.Audio.Media.MIME_TYPE);
        projections.add(MediaStore.Audio.Media.DATE_ADDED);
        projections.add(MediaStore.Audio.Media.DATE_MODIFIED);
        /**
         * MediaStore.Video.Media.RELATIVE_PATH
         * 相对路径   /storage/0000-0000/DCIM/Vacation/IMG1024.JPG} would have a path of {@code DCIM/Vacation/}.
         *
         * MediaStore.Video.Media.DATA 在android10上已经过时了
         * 真实路径  /storage/emulated/0/pp/downloader/wallpaper/aaa.jpg
         */
        if (Build.VERSION.SDK_INT >= 29/*android 10*/) {
            projections.add(MediaStore.Audio.Media.RELATIVE_PATH);
        } else {
            projections.add(MediaStore.Audio.Media.DATA);
        }
        addProjections(projections);

        Cursor cursor = contentResolver.query(externalContentUri, projections.toArray(new String[projections.size()]), selection, selectionArgs, order);
        if (cursor == null) {
            return null;
        }

        ArrayList<MediaFolder> mediaFolders = new ArrayList<>();
        ArrayList<MediaItem> allMedias = new ArrayList<>();
        while (cursor.moveToNext()) {
            //查询数据
            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));

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
                String[] parentInfo = getParentInfoFromData(data);
                parentName = parentInfo[0];
                parentPath = parentInfo[1];
            }

            if (!TextUtils.isEmpty(relativePath)) {
                String[] parentInfo = getParentInfoFromRelativePath(relativePath);
                parentName = parentInfo[0];
                parentPath = parentInfo[1];
            }

            // common
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE));
            String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.MIME_TYPE));
            long dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
            long dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

            //封装实体
            MediaItem mediaItem = new MediaItem(uri);
            mediaItem.setDisplayName(displayName);
            mediaItem.setSize(size);
            mediaItem.setMimeType(mimeType);
            mediaItem.setDateAdded(dateAdded);
            mediaItem.setDateModified(dateModified);
            mediaItem.setData(data);
            mediaItem.setRelativePath(relativePath);

            // 专有参数
            bindCursorData(cursor, mediaItem);


            allMedias.add(mediaItem);

            //根据父路径分类存放图片
            MediaFolder mediaFolder = new MediaFolder();
            mediaFolder.name = parentName;
            mediaFolder.path = parentPath;

            if (mediaFolders.contains(mediaFolder)) {
                mediaFolders.get(mediaFolders.indexOf(mediaFolder)).items.add(mediaItem);
            } else {
                ArrayList<MediaItem> images = new ArrayList<>();
                images.add(mediaItem);
                mediaFolder.items = images;
                mediaFolders.add(mediaFolder);
            }
        }
        // 防止没有图片报异常
        if (cursor.getCount() > 0 && allMedias.size() > 0) {
            //构造所有图片的集合
            MediaFolder allImagesFolder = new MediaFolder();
            allImagesFolder.name = context.getString(R.string.recently);
            allImagesFolder.path = "";
            allImagesFolder.items = allMedias;
            mediaFolders.add(0, allImagesFolder);//确保第一条是所有图片
        }
        cursor.close();
        return mediaFolders;
    }

    @Override
    protected Uri getContentUri() {
        return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
    }

    @Override
    protected String getOrder() {
        return MediaStore.MediaColumns.DATE_ADDED + " DESC";
    }

    @Override
    protected String getSelection() {
        return null;
    }

    @Override
    protected String[] getSelectionArgs() {
        return null;
    }

    @Override
    protected void addProjections(ArrayList<String> projections) {
        // audio
        projections.add(MediaStore.Audio.Media.DURATION);
    }

    @Override
    protected void bindCursorData(Cursor cursor, MediaItem mediaItem) {
        long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
        if (duration != -1) {
            mediaItem.setDuration(duration);
        }
    }

}
