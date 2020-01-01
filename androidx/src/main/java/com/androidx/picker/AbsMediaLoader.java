package com.androidx.picker;

import android.text.TextUtils;

import java.io.File;

/**
 * user author: didikee
 * create time: 2020-01-01 20:01
 * description: 
 */
public abstract class AbsMediaLoader {

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
        if (!TextUtils.isEmpty(relativePath)) {
            // 根据相对路径来判断
            // DCIM/MY FOLDER/SUB/demo.png
            // android 10 DCIM/MY FOLDER/SUB
            String parentName = "";
            String parentPath = "";
            if (!TextUtils.isEmpty(relativePath) && relativePath.contains(File.separator)) {
                int lastIndexOf = relativePath.lastIndexOf(File.separator);
                if (lastIndexOf != -1) {
                    parentName = relativePath.substring(lastIndexOf + 1);
                    parentPath = relativePath.substring(0, lastIndexOf);
                }
            } else {
                parentName = relativePath;
                parentPath = relativePath;
            }
            return new String[]{parentName, parentPath};
        }
        return new String[]{"", ""};
    }
}
