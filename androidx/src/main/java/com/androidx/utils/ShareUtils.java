package com.androidx.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

/**
 * user author: didikee
 * create time: 2019-12-02 14:23
 * description: 
 */
public final class ShareUtils {
    private static final String TAG = "ShareUtil";

    private ShareUtils() {
    }

    public static void share(Context context, Uri uri, String mimeType, String packageName, String title) throws Exception {
        share(context, uri, mimeType, packageName, "", title);
    }

    public static void share(Context context, Uri uri, String mimeType,
                             String packageName, String className, String title) throws Exception {
        if (context == null) {
            throw new Exception("context is null");
        }
        if (uri == null) {
            throw new Exception("uri is null");
        }
        if (TextUtils.isEmpty(mimeType)) {
            throw new Exception("mimeType is empty");
        }
        if (TextUtils.isEmpty(title)) {
            title = "";
        }
        if (!TextUtils.isEmpty(packageName)
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q
                && checkAndGetLaunchIntent(context, packageName) == null) {
            // app not installed
            throw new Exception("App not installed");
        } else {
            Intent shareIntent = getShareIntent(uri, mimeType, packageName, className);
            context.startActivity(Intent.createChooser(shareIntent, title));
        }
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

    public static void share(Context context, Uri uri, String packageName, String title) throws Exception {
        String mimeType = UriUtils.getMimeType(context, uri);
        share(context, uri, mimeType, packageName, title);
    }

    /**
     * 分享媒体uri:可以时图片,视频,gif等.
     * @param activity activity
     * @param uri uri,注意Android N 前后版本uri的差异,请先做好兼容处理
     * @param mimeType uri的媒体类型
     * @param packageName 指定app分享,可以为空.但是不为空是如果指定的包名对应的app未安装则会分享失败
     * @param shareTitle 分享标题,可以为空
     * @return 是否分享成功
     */
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
