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
//            if (TextUtils.isEmpty(selection) && targetMimeTypes != null && targetMimeTypes.length > 0) {
//                StringBuilder sb = new StringBuilder();
//                for (int i = 0; i < targetMimeTypes.length; i++) {
//                    sb.append(MediaStore.MediaColumns.MIME_TYPE + "=?");
//                    if (i != targetMimeTypes.length - 1) {
//                        sb.append(" OR ");
//                    }
//                }
//            }
        } else if (AndroidStorage.EXTERNAL_VIDEO_URI.toString().equals(externalContentUri.toString())) {
            allExtraProjections.add(MediaStore.MediaColumns.WIDTH);
            allExtraProjections.add(MediaStore.MediaColumns.HEIGHT);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                allExtraProjections.add(MediaStore.Video.Media.DURATION);
            }
        } else if (AndroidStorage.EXTERNAL_AUDIO_URI.toString().equals(externalContentUri.toString())) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
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
        return dataHandler.getResult();
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
        String order;
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
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
//                this.contentUri = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL);
//            }else {
//                this.contentUri = AndroidStorage.EXTERNAL_IMAGE_URI;
//            }
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

    public interface DataHandler<T> {
        T handle(@NonNull Cursor cursor, @NonNull ArrayList<String> projections, @NonNull Uri uri,
                 String displayName, String mimeType,
                 long size, long dateAdded, long dateModified,
                 String data, String relativePath
        );

        ArrayList<T> getResult();
    }

    public static class MediaItemDataHandler implements DataHandler<MediaItem> {
        final ArrayList<MediaItem> result = new ArrayList<>();

        @Override
        public MediaItem handle(@NonNull Cursor cursor, @NonNull ArrayList<String> projections,
                                @NonNull Uri uri, String displayName,
                                String mimeType, long size, long dateAdded,
                                long dateModified, String data, String relativePath) {
            int width = 0;
            int height = 0;
            long duration = 0;
            for (String projection : projections) {
                switch (projection) {
                    case MediaStore.MediaColumns.WIDTH:
                        width = cursor.getInt(cursor.getColumnIndexOrThrow(projection));
                        break;
                    case MediaStore.MediaColumns.HEIGHT:
                        height = cursor.getInt(cursor.getColumnIndexOrThrow(projection));
                        break;
                    case MediaStore.MediaColumns.DURATION:
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                            duration = cursor.getLong(cursor.getColumnIndexOrThrow(projection));
                        } else {
                            // 低于android q无法直接查询到时长
                        }
                        break;
                }
            }
            MediaItem mediaItem = new MediaItem(uri);
            // 设置公共参数
            mediaItem.setDisplayName(displayName);
            mediaItem.setSize(size);
            mediaItem.setMimeType(mimeType);
            mediaItem.setDateAdded(dateAdded);
            mediaItem.setDateModified(dateModified);
            mediaItem.setData(data);
            mediaItem.setRelativePath(relativePath);
            //封装实体
            mediaItem.setWidth(width);
            mediaItem.setHeight(height);
            mediaItem.setDuration(duration);
            result.add(mediaItem);
            return mediaItem;
        }

        @Override
        public ArrayList<MediaItem> getResult() {
            return result;
        }
    }

    public static class UriDataHandler implements DataHandler<Uri> {
        final ArrayList<Uri> result = new ArrayList<>();

        @Override
        public Uri handle(@NonNull Cursor cursor, @NonNull ArrayList<String> projections, @NonNull Uri uri, String displayName, String mimeType, long size, long dateAdded, long dateModified, String data, String relativePath) {
            result.add(uri);
            return uri;
        }

        @Override
        public ArrayList<Uri> getResult() {
            return result;
        }
    }

    public static class FolderDataHandler implements DataHandler<MediaFolder> {
        private final MediaItemDataHandler mediaItemDataHandler = new MediaItemDataHandler();
        private final ArrayList<MediaFolder> result = new ArrayList<>();

        @Override
        public MediaFolder handle(@NonNull Cursor cursor, @NonNull ArrayList<String> projections, @NonNull Uri uri,
                                  String displayName, String mimeType, long size, long dateAdded, long dateModified,
                                  String data, String relativePath) {
            // LogUtils.d("uri:" + uri + ", displayName: " + displayName + ", mimeType: " + mimeType + ", size: " + size + ", dateAdded: " + dateAdded + ", dateModified: " + dateModified + ", data: " + data + ", relativePath: " + relativePath);
            MediaItem mediaItem = mediaItemDataHandler.handle(cursor, projections, uri, displayName,
                    mimeType, size, dateAdded, dateModified, data, relativePath);
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
            //根据父路径分类存放图片
            MediaFolder mediaFolder = new MediaFolder();
            mediaFolder.name = parentName;
            mediaFolder.path = parentPath;

            int indexOf = result.indexOf(mediaFolder);
            if (indexOf == -1) {
                ArrayList<MediaItem> images = new ArrayList<>();
                images.add(mediaItem);
                mediaFolder.items = images;
                result.add(mediaFolder);
            } else {
                // contain
                result.get(indexOf).items.add(mediaItem);
            }
            return mediaFolder;
        }

        @Override
        public ArrayList<MediaFolder> getResult() {
            return result;
        }

        public ArrayList<MediaItem> getAllItems() {
            return mediaItemDataHandler.getResult();
        }

        public String[] getParentInfoFromData(String data) {
            if (!TextUtils.isEmpty(data)) {
                // 根据java系统来判断
                File file = new File(data);
                if (file.exists() && file.length() > 0) {
                    File imageParentFile = file.getParentFile();
                    if (imageParentFile != null) {
                        String parentName = imageParentFile.getName();
                        String parentPath = imageParentFile.getAbsolutePath();
                        return new String[]{parentName, parentPath};
                    }
                }
            }
            return new String[]{"", ""};
        }

        public String[] getParentInfoFromRelativePath(String relativePath) {
            String parentName = "";
            String parentPath = "";
            if (!TextUtils.isEmpty(relativePath)) {
                // 根据相对路径来判断
                // DCIM/MY FOLDER/SUB/demo.png
                // android 10 DCIM/MY FOLDER/SUB
                if (!TextUtils.isEmpty(relativePath) && relativePath.contains(File.separator)) {
                    String[] split = relativePath.split(File.separator);
                    int length = split.length;
                    String folderName = "";
                    if (length > 0) {
                        String lastStr = split[length - 1];
                        if (TextUtils.isEmpty(lastStr)) {
                            if (length - 2 >= 0) {
                                String preLastStr = split[length - 2];
                                if (!TextUtils.isEmpty(preLastStr)) {
                                    folderName = preLastStr;
                                }
                            }
                        } else {
                            folderName = lastStr;
                        }
                    }
                    parentName = folderName;
                    parentPath = relativePath;
                } else {
                    parentName = relativePath;
                    parentPath = relativePath;
                }
            }
            return new String[]{parentName, parentPath};
        }
    }


}
