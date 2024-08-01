package com.androidx;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/**
 * description:
 */
public final class AndroidUtils {
    private static final String TAG = "AndroidUtils";

    public enum ExpectMediaType {
        IMAGE,
        VIDEO,
        IMAGE_AND_VIDEO
    }

    public static Intent getAppSettingsIntent(String packageName) {
        Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        settingsIntent.setData(Uri.fromParts("package", packageName, null));
        return settingsIntent;
    }

    public static boolean isAndroid14() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
    }

    public static boolean isAndroid13() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
    }

    public static boolean isAndroid10() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    }

    public static boolean hasPermissions(@NonNull Context context, @NonNull String... permissions) {
        if (permissions.length == 0) {
            LogUtils.w("hasPermissions check 0 permissions.");
            return true;
        }
        // Always return true for SDK < M, let the system deal with the permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            Log.w(TAG, "hasPermissions: API version < M, returning true by default");

            // DANGER ZONE!!! Changing this will break the library.
            return true;
        }

        // Null context may be passed if we have detected Low API (less than M) so getting
        // to this point with a null context should not be possible.
        if (context == null) {
            throw new IllegalArgumentException("Can't check permissions for null context");
        }
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(context, perm)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    public static boolean shouldShowRequestPermissionRationale(@NonNull Activity activity,
                                                               @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        for (String permission : permissions) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否需要显示申请权限的原因
     *
     * @param fragment
     * @param permissions
     * @return
     */
    public static boolean shouldShowRequestPermissionRationale(@NonNull Fragment fragment, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return false;
        }
        for (String permission : permissions) {
            if (fragment.shouldShowRequestPermissionRationale(permission)) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] getExpectPermissions(ExpectMediaType mediaType) {
        if (mediaType == ExpectMediaType.IMAGE) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
            };
        } else if (mediaType == ExpectMediaType.VIDEO) {
            return new String[]{
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {
            return new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        }
    }

    /**
     * 用户是否选择了只显示部分图片/视频
     *
     * @param context
     * @return
     */
    public static boolean shouldShowVisualTips(@NonNull Context context, ExpectMediaType mediaType) {
        if (isAndroid14()) {
            final String[] mediaPermissions = getExpectPermissions(mediaType);
            boolean mediaPermissionsGranted = hasPermissions(context, mediaPermissions);
            boolean visualPermissionGranted = hasPermissions(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
            LogUtils.d("shouldShowVisualTips mediaPermissionsGranted: " + mediaPermissionsGranted);
            LogUtils.d("shouldShowVisualTips visualPermissionGranted: " + visualPermissionGranted);
            return !mediaPermissionsGranted && visualPermissionGranted;
        } else {
            return false;
        }
    }


    /**
     * 是否已经有基础权限，这会根据版本进行判断，最终会检查不同版本的最基本权限即可
     *
     * @param context
     * @param permissions
     * @return
     */
    public static boolean hasBasicPermissionsForImageAndVideo(@NonNull Context context, @NonNull String[] permissions) {
        if (isAndroid14()) {
            List<String> permissionList = new ArrayList<>(Arrays.asList(permissions));
            // 检查是否包含 READ_MEDIA_VISUAL_USER_SELECTED 权限
            if (permissionList.contains(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)) {
                boolean visualGranted = hasPermissions(context, Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
                if (visualGranted) {
                    // 移除 READ_MEDIA_VIDEO 和 READ_MEDIA_IMAGES 权限
                    permissionList.remove(Manifest.permission.READ_MEDIA_VIDEO);
                    permissionList.remove(Manifest.permission.READ_MEDIA_IMAGES);
                    String[] leftPermissions = permissionList.toArray(new String[0]);
                    if (leftPermissions.length > 0) {
                        return hasPermissions(context, leftPermissions);
                    } else {
                        return true;
                    }
                } else {
                    // 移除 READ_MEDIA_VISUAL_USER_SELECTED 权限，看看其他权限是否是同意的
                    permissionList.remove(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED);
                    String[] leftPermissions = permissionList.toArray(new String[0]);
                    return hasPermissions(context, leftPermissions);
                }
            } else {
                return hasPermissions(context, permissions);
            }
        } else {
            // 低于安卓14的版本
            return hasPermissions(context, permissions);
        }
    }


}
