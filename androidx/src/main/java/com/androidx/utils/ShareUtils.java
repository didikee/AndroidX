package com.androidx.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.androidx.LogUtils;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * user author: didikee
 * create time: 2019-12-02 14:23
 * description:
 */
public final class ShareUtils {
    private static final String TAG = "ShareUtil";

    private ShareUtils() {
    }

    /**
     * 分享媒体uri:可以时图片,视频,gif等.
     *
     * @param context    activity
     * @param uri         uri,注意Android N 前后版本uri的差异,请先做好兼容处理
     * @param mimeType    uri的媒体类型
     * @param packageName 指定app分享,可以为空.但是不为空是如果指定的包名对应的app未安装则会分享失败
     * @param title  分享标题,可以为空
     * @return 是否分享成功
     */
    public static void share(Context context, Uri uri, String mimeType, String packageName, String title) throws Exception {
        share(context, uri, mimeType, packageName, "", title);
    }

    public static void share(Context context, Uri uri, String mimeType,
                             String packageName, String className, String title) {
        shareUri(context, uri, mimeType, packageName, className, title);
    }

    /**
     * @param context
     * @param uri
     * @param mimeType
     * @param packageName
     * @param className
     * @param title
     * @throws Exception
     */
    public static void shareUri(Context context, Uri uri, String mimeType,
                                String packageName, String className, String title) {
        if (context == null) {
            LogUtils.e("context is null");
            return;
        }
        if (uri == null) {
            LogUtils.e("uri is null");
            return;
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = UriUtils.getMimeType(context, uri);
        }
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.e("mimeType is empty");
            return;
        }
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        if (!TextUtils.isEmpty(packageName)
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
                && checkAndGetLaunchIntent(context, packageName) == null) {
            // app not installed
            LogUtils.w("App not installed");
        } else {
            Intent shareIntent = getShareIntent(uri, mimeType, packageName, className);
            context.startActivity(Intent.createChooser(shareIntent, title));
        }
    }

    public static void shareMultipleUris(Context context, ArrayList<Uri> uris, String mimeType,
                                         String packageName, String className, String title) {
        if (context == null) {
            LogUtils.e("context is null");
            return;
        }
        if (uris == null || uris.size() <= 0) {
            LogUtils.e("uris is empty");
            return;
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = UriUtils.getMimeType(context, uris.get(0));
        }
        if (TextUtils.isEmpty(mimeType)) {
            LogUtils.e("mimeType is empty");
            return;
        }
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        if (!TextUtils.isEmpty(packageName)
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
                && checkAndGetLaunchIntent(context, packageName) == null) {
            // app not installed
            LogUtils.w("App not installed");
        } else {
            Intent shareIntent = getShareIntent( mimeType, packageName, className,uris);
            context.startActivity(Intent.createChooser(shareIntent, title));
        }
    }

    public static Intent getShareIntent(@NonNull String mimeType,
                                        @Nullable String packageName,
                                        @Nullable String className,
                                        @NonNull ArrayList<Uri> uris) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(packageName)) {
            if (TextUtils.isEmpty(className)) {
                shareIntent.setPackage(packageName);
            } else {
                shareIntent.setComponent(new ComponentName(packageName, className));
            }
        }
        shareIntent.setType(mimeType);
        shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return shareIntent;
    }

    public static Intent getShareIntent(@NonNull String mimeType,
                                        @Nullable String packageName,
                                        @Nullable String className,
                                        @NonNull Uri uri) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(packageName)) {
            if (TextUtils.isEmpty(className)) {
                shareIntent.setPackage(packageName);
            } else {
                shareIntent.setComponent(new ComponentName(packageName, className));
            }
        }
        shareIntent.setType(mimeType);
        shareIntent.setDataAndType(uri, mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return shareIntent;
    }

    public static Intent getShareIntent(Uri uri, String mimeType,
                                        String packageName, String className) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(packageName)) {
            if (TextUtils.isEmpty(className)) {
                shareIntent.setPackage(packageName);
            } else {
                shareIntent.setComponent(new ComponentName(packageName, className));
            }
        }
        shareIntent.setType(mimeType);
        shareIntent.setDataAndType(uri, mimeType);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        return shareIntent;
    }

    public static void share(@NonNull Context context,
                             @NonNull Uri uri,
                             @Nullable String packageName,
                             @Nullable String title) throws Exception {
        String mimeType = UriUtils.getMimeType(context, uri);
        share(context, uri, mimeType, packageName, title);
    }


    @Deprecated
    public static boolean share(Activity activity, Uri uri, String mimeType, String packageName, String shareTitle) {
        try {
            share((Context) activity, uri, mimeType, packageName, shareTitle);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 该api已经无法在android R上工作了，如果必须使用请申请 query all package 权限
     * 参考文档：https://developer.android.google.cn/training/basics/intents/package-visibility
     *
     * @param context
     * @param packageName
     * @return
     */
    @Deprecated
    public static Intent checkAndGetLaunchIntent(Context context, String packageName) {
        if (context == null || TextUtils.isEmpty(packageName)) {
            return null;
        }
        try {
            return context.getPackageManager().getLaunchIntentForPackage(packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
