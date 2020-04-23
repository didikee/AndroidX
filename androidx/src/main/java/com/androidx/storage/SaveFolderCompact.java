package com.androidx.storage;

import android.content.Context;
import android.os.Build;
import android.os.Environment;

import com.androidx.LogUtils;

import java.io.File;

/**
 * user author: didikee
 * create time: 4/23/20 9:27 AM
 * description: 
 */
public class SaveFolderCompact {
    public static String getSaveFolderPathForAudio(Context context, String folderName) {
        return getSaveFolderPath(context, Environment.DIRECTORY_MUSIC, folderName);
    }

    public static String getImageSaveFolderPath(Context context, String folderName) {
        return getSaveFolderPath(context, Environment.DIRECTORY_PICTURES, folderName);
    }

    public static String getSaveFolderPath(Context context, String environment, String saveFolderName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return environment + File.separator + saveFolderName;
        } else {
            String dcim = Environment.getExternalStoragePublicDirectory(environment).getAbsolutePath();
            File dir = new File(dcim + File.separator + saveFolderName + File.separator);
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
