package com.androidx;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;

import java.io.File;
import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.WorkerThread;

/**
 * description: 关于android:name="android.permission.MANAGE_EXTERNAL_STORAGE" 权限下的文件管理
 */
public final class AndroidExternalStorage {

    /**
     * 判断当前是否具有 MANAGE_EXTERNAL_STORAGE 权限
     *
     * @return true: 代表已经拥有 MANAGE_EXTERNAL_STORAGE 权限
     * false: 代表没有相应的权限，此时应该去申请权限
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static boolean isSupported() {
        return Environment.isExternalStorageManager();
    }

    /**
     * @param pkgName 应用的 BuildConfig.APPLICATION_ID
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Intent getManageExternalStoragePermissionIntent(@NonNull String pkgName) {
        return new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.parse("package:" + pkgName));
    }

    /**
     * 申请权限
     *
     * @param activity
     * @param pkgName
     * @param requestCode
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static void requestExternalStoragePermission(@NonNull Activity activity,
                                                        @NonNull String pkgName,
                                                        int requestCode) {
        if (isSupported()) {
            return;
        }
        try {
            Intent intent = getManageExternalStoragePermissionIntent(pkgName);
            activity.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            e.printStackTrace();
            Intent vagueIntent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            activity.startActivityForResult(vagueIntent, requestCode);
        }
    }


    /**
     * 从一个文件夹中列出所有符合拓展名要求的文件列表
     *
     * @param directory  需要查找的文件夹
     * @param extensions 拓展名，可以是一个或者多个，需要加点（虽然不是强制的，但是这样匹配可能会更准确一些）
     * @return 文件列表
     */
    @NonNull
    @WorkerThread
    public static ArrayList<File> listFiles(@NonNull File directory, @NonNull String[] extensions) {
        ArrayList<File> resultList = new ArrayList<>();
        if (directory.isDirectory() && extensions.length > 0) {
            findTargetFiles(directory, extensions, resultList);
        }
        return resultList;
    }

    private static void findTargetFiles(@NonNull File directory, String[] extensions, ArrayList<File> resultList) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    findTargetFiles(file, extensions, resultList);
                } else {
                    for (String extension : extensions) {
                        if (file.getName().toLowerCase().endsWith(extension)) {
                            resultList.add(file);
                            break;
                        }
                    }
                }
            }
        }
    }


}
