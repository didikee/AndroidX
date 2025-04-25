package com.androidx.picker;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.AndroidStorage;
import com.androidx.AndroidUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * user author: didikee
 * create time: 5/26/21 4:17 下午
 * description:
 */
public class MediaLoader {
    // 排序，根据添加日期排序
    public static final String ORDER_DATE_ADDED_DESC = MediaStore.MediaColumns.DATE_ADDED + " DESC";
    // 排序，根据最后修改的日期排序（从新到旧）
    public static final String ORDER_DATE_MODIFIED_DESC = MediaStore.MediaColumns.DATE_MODIFIED + " DESC";
    private final @NonNull
    ContentResolver contentResolver;
    @Nullable
    private Uri externalContentUri;
    @Nullable
    private final String order;
    @Nullable
    private final String selection;
    @Nullable
    private final String[] selectionArgs;
    @Nullable
    private final String targetFolderPath;
    @Nullable
    private final String[] targetMimeTypes;
    @Nullable
    private final String[] extraProjections;
    @Nullable
    private final String[] blockMimeTypes;


    private MediaLoader(Builder builder) {
        contentResolver = builder.resolver;
        externalContentUri = builder.contentUri;
        order = builder.order;
        selection = builder.selection;
        selectionArgs = builder.selectionArgs;
        targetMimeTypes = builder.targetMimeTypes;
        blockMimeTypes = builder.blockMimeTypes;
        targetFolderPath = builder.targetFolderPath;
        extraProjections = builder.extraProjections;
    }

    protected void addExtraProjections(ArrayList<String> projections, ArrayList<String> extraProjections) {
        for (String projection : extraProjections) {
            if (!projections.contains(projection)) {
                projections.add(projection);
            }
        }
    }

    @NonNull
    private <T> ArrayList<T> execute(DataHandler<T> dataHandler) {
        final boolean isAndroid10 = AndroidUtils.isAndroid10();
        final boolean externalStorageLegacy = AndroidUtils.isExternalStorageLegacy();
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
        if (externalContentUri == null) {
            externalContentUri = AndroidStorage.EXTERNAL_IMAGE_URI;
        }
        final ArrayList<String> allExtraProjections = new ArrayList<>();
        // 根据特定的类型添加特定类型的参数
        if (AndroidStorage.EXTERNAL_IMAGE_URI.toString().equals(externalContentUri.toString())) {
            allExtraProjections.add(MediaStore.MediaColumns.WIDTH);
            allExtraProjections.add(MediaStore.MediaColumns.HEIGHT);
            if (AndroidUtils.getOSVersion() >= 30) {
                allExtraProjections.add(MediaStore.Images.Media.XMP);
            }
        } else if (AndroidStorage.EXTERNAL_VIDEO_URI.toString().equals(externalContentUri.toString())) {
            allExtraProjections.add(MediaStore.MediaColumns.WIDTH);
            allExtraProjections.add(MediaStore.MediaColumns.HEIGHT);
            if (isAndroid10) {
                allExtraProjections.add(MediaStore.Video.Media.DURATION);
            }
        } else if (AndroidStorage.EXTERNAL_AUDIO_URI.toString().equals(externalContentUri.toString())) {
            if (isAndroid10) {
                allExtraProjections.add(MediaStore.Audio.Media.DURATION);
            }
        } else {
            // empty
        }
        //可选，增加额外的类型参数
        if (extraProjections != null) {
            allExtraProjections.addAll(Arrays.asList(extraProjections));
        }
        addExtraProjections(projections, allExtraProjections);

        Cursor cursor = contentResolver.query(externalContentUri,
                projections.toArray(new String[projections.size()]),
                selection, selectionArgs, order);
        if (cursor == null) {
            return new ArrayList<>();
        }

        while (cursor.moveToNext()) {
            // 这些是公用的参数
            String id = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID));
            String mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE));
            // 根据mimetype来过滤文件
            if (contains(blockMimeTypes, mimeType)) {
                continue;
            }
            if (targetMimeTypes != null && targetMimeTypes.length > 0 && !contains(targetMimeTypes, mimeType)) {
                continue;
            }
            String data = "";
            String relativePath = "";
            if (isAndroid10) {
                if (externalStorageLegacy) {
                    data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
                } else {
                    relativePath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.RELATIVE_PATH));
                }
            } else {
                data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA));
            }
            // 文件夹过滤
            if (!isTargetFolder(data, relativePath, targetFolderPath)) {
                continue;
            }

            // 现在全部改为uri来实现
            Uri uri = ContentUris.withAppendedId(externalContentUri, Long.parseLong(id));
            // 延迟解析的公共参数
            String displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME));
            long size = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE));
            long dateAdded = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_ADDED));
            long dateModified = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED));

            dataHandler.handle(cursor, allExtraProjections, uri, displayName, mimeType, size, dateAdded, dateModified, data, relativePath);
        }
        return dataHandler.getDataResult();
    }

    private boolean contains(@Nullable String[] array, @Nullable String content) {
        if (content == null || content.length() == 0) {
            return false;
        }
        if (array != null && array.length > 0) {
            for (String s : array) {
                if (content.equalsIgnoreCase(s)) {
                    return true;
                }
            }
        }
        return false;
    }

    protected boolean isTargetFolder(@Nullable String data, @Nullable String relativePath, @Nullable String folderPath) {
        if (TextUtils.isEmpty(folderPath)) {
            return true;
        }
        // 文件夹过滤
        // android9:手机存储/DCIM/VideoMusicEditor/
        if (data != null && data.length() > 0 && data.startsWith(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + folderPath)) {
            return true;
        }
        // android10:DCIM/VideoMusicEditor/adb.mp4
        if (relativePath != null && relativePath.length() > 0 && relativePath.startsWith(folderPath)) {
            return true;
        }
        return false;
    }

    public static class Builder {
        ContentResolver resolver;
        Uri contentUri;
        String order = ORDER_DATE_MODIFIED_DESC; // 默认按照时间排序，最近的媒体在最前面，符合90%的使用场景
        String selection;
        String[] selectionArgs;
        String[] extraProjections;
        // 过滤
        String targetFolderPath;
        // 目标类型
        String[] targetMimeTypes;
        // 屏蔽的类型
        String[] blockMimeTypes;

        public Builder(ContentResolver resolver) {
            this.resolver = resolver;
        }

        public Builder setContentUri(Uri contentUri) {
            this.contentUri = contentUri;
            return this;
        }

        public Builder setOrder(String order) {
            this.order = order;
            return this;
        }

        public Builder setSelection(String selection, String[] selectionArgs) {
            this.selection = selection;
            this.selectionArgs = selectionArgs;
            return this;
        }

        public Builder setExtraProjections(String[] extraProjections) {
            this.extraProjections = extraProjections;
            return this;
        }

        public Builder setTargetFolder(String targetFolderPath) {
            this.targetFolderPath = targetFolderPath;
            return this;
        }

        public Builder ofImage() {
            this.contentUri = AndroidStorage.EXTERNAL_IMAGE_URI;
            return this;
        }

        public Builder ofVideo() {
            this.contentUri = AndroidStorage.EXTERNAL_VIDEO_URI;
            return this;
        }

        public Builder ofAudio() {
            this.contentUri = AndroidStorage.EXTERNAL_AUDIO_URI;
            return this;
        }

        @RequiresApi(api = Build.VERSION_CODES.Q)
        public Builder ofDownload() {
            this.contentUri = AndroidStorage.EXTERNAL_DOWNLOAD_URI;
            return this;
        }

        public Builder setTargetMimeTypes(String[] targetMimeTypes) {
            this.targetMimeTypes = targetMimeTypes;
            return this;
        }

        public Builder setBlockMimeTypes(String[] blockMimeTypes) {
            this.blockMimeTypes = blockMimeTypes;
            return this;
        }

        public ArrayList<MediaItem> get() {
            return new MediaLoader(this).execute(new MediaItemDataHandler());
        }

        /**
         * 以文件夹的形式获得所有的媒体文件
         *
         * @return
         */
        public ArrayList<MediaFolder> getFolders(@Nullable String defaultFolderName) {
            FolderDataHandler folderDataHandler = new FolderDataHandler();
            ArrayList<MediaFolder> mediaFolders = new MediaLoader(this).execute(folderDataHandler);
            ArrayList<MediaItem> allItems = folderDataHandler.getAllItems();
            if (allItems.size() > 0) {
                //构造所有媒体文件的集合
                MediaFolder allImagesFolder = new MediaFolder();
                allImagesFolder.name = TextUtils.isEmpty(defaultFolderName) ? "Recently" : defaultFolderName;
                allImagesFolder.path = "";
                allImagesFolder.items = allItems;
                mediaFolders.add(0, allImagesFolder);  //确保第一条是所有图片
            }
            return mediaFolders;
        }

        /**
         * 获取所有的媒体文件，以uri的形式
         *
         * @return
         */
        public ArrayList<Uri> getUris() {
            return new MediaLoader(this).execute(new UriDataHandler());
        }

        public <T> ArrayList<T> get(DataHandler<T> dataHandler) {
            return new MediaLoader(this).execute(dataHandler);
        }
    }


}
