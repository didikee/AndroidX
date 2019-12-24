package com.androidx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

/**
 * user author: didikee
 * create time: 2019-12-02 14:23
 * description: 
 */
public final class ShareUtil {
    private static final String TAG = "ShareUtil";

    /**
     * 分享媒体uri:可以时图片,视频,gif等.
     * @param activity activity
     * @param uri uri,注意Android N 前后版本uri的差异,请先做好兼容处理
     * @param mimeType uri的媒体类型
     * @param packageName 指定app分享,可以为空.但是不为空是如果指定的包名对应的app未安装则会分享失败
     * @param shareTitle 分享标题,可以为空
     * @return 是否分享成功
     */
    public static boolean share(Activity activity, Uri uri, String mimeType, String packageName, String shareTitle) {
        if (activity == null) {
            Log.e(TAG, "ShareUtil Activity is null");
            return false;
        }
        if (uri == null) {
            Log.e(TAG, "ShareUtil uri is null");
            return false;
        }
        if (TextUtils.isEmpty(mimeType)) {
            Log.e(TAG, "ShareUtil mimeType is empty");
            return false;
        }
        if (TextUtils.isEmpty(shareTitle)) {
            shareTitle = "";
        }
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        if (!TextUtils.isEmpty(packageName)) {
            if (checkAndGetLaunchIntent(activity, packageName) == null) {
                // App not installed
                Toast.makeText(activity, "App not installed", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                shareIntent.setPackage(packageName);
            }
        }
        try {
            shareIntent.setType(mimeType);
            shareIntent.setDataAndType(uri, mimeType);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            activity.startActivity(Intent.createChooser(shareIntent, shareTitle));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Intent checkAndGetLaunchIntent(Context context, String packageName) {
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
