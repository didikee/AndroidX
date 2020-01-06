package com.androidx.picker;

import android.content.Context;
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

    protected abstract Uri getContentUri();

    protected abstract String getOrder();

    protected abstract String getSelection();

    protected abstract String[] getSelectionArgs();

    public abstract ArrayList<MediaFolder> get(Context context, String folderPath);

    public ArrayList<MediaFolder> get(Context context) {
        return get(context, "");
    }


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
