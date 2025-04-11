package com.androidx.picker;

import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * description:
 */
public class FolderDataHandler implements DataHandler<MediaFolder> {
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

    @NonNull
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
