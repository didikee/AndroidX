package com.androidx.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.androidx.LogUtils;

import java.io.File;

/**
 * user author: didikee
 * create time: 4/23/20 9:27 AM
 * description: 
 */
public class SaveFolderCompact {
    public static String getSaveFolderPathForAudio(Context context, String folderName, String subFolderName) {
        return getSaveFolderPath(context, Environment.DIRECTORY_MUSIC, folderName, subFolderName);
    }

    public static String getImageSaveFolderPath(Context context, String folderName, String subFolderName) {
        return getSaveFolderPath(context, Environment.DIRECTORY_PICTURES, folderName, subFolderName);
    }

    public static String getSaveFolderPath(Context context, String environment, String rootFolderName, String subFolderName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (TextUtils.isEmpty(subFolderName)) {
                return environment + File.separator + rootFolderName;
            } else {
                return environment + File.separator + rootFolderName + File.separator + subFolderName;
            }
        } else {
            String dcim = Environment.getExternalStoragePublicDirectory(environment).getAbsolutePath();
            File dir;
            if (TextUtils.isEmpty(subFolderName)) {
                dir = new File(dcim + File.separator + rootFolderName + File.separator);
            } else {
                dir = new File(dcim + File.separator + rootFolderName + File.separator + subFolderName + File.separator);
            }
            if (!dir.exists()) {
                boolean mkdirs = dir.mkdirs();
                if (!mkdirs) {
                    LogUtils.e("APPHolder getSaveFolderPath() mkdirs failed.");
                    return "";
                }
            }
            return dir.getAbsolutePath();
        }
    }
}
