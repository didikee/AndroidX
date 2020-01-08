package com.androidx.picker;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;

/**
 * user author: didikee
 * create time: 2020-01-01 20:01
 * description: 
 */
public abstract class AbsMediaLoader {
    public static final String DATE_TAKEN = "datetaken";

    public abstract ArrayList<MediaFolder> get(Context context, String folderPath);

    public ArrayList<MediaFolder> get(Context context) {
        return get(context, "");
    }

    /**
     * 从哪个媒体库中查询数据
     * @return
     */
    protected abstract Uri getContentUri();

    /**
     * 查询是按照什么来排序的，一般是时间排序
     * @return
     */
    protected abstract String getOrder();

    /**
     * 筛选,和{@link AbsMediaLoader#getSelectionArgs()}是对应的
     * @return 例如：MediaStore.MediaColumns.MIME_TYPE + "=?"
     */
    protected abstract String getSelection();

    /**
     * 筛选,和{@link AbsMediaLoader#getSelection()}是对应的
     * @return 例如：new String[]{"image/gif"}
     */
    protected abstract String[] getSelectionArgs();

    /**
     * 添加特性类型文件特有的属性，比如图片的宽高（音乐文件却没有）
     * @param projections
     */
    protected abstract void addProjections(ArrayList<String> projections);

    /**
     * 绑定特有的属性到实体类中，参照 addProjections(ArrayList<String> projections);
     * @param cursor
     * @param mediaItem
     */
    protected abstract void bindCursorData(Cursor cursor, MediaItem mediaItem);


    protected String[] getParentInfoFromData(String data) {
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

    protected String[] getParentInfoFromRelativePath(String relativePath) {
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
