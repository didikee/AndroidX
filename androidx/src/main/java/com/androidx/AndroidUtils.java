package com.androidx;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * description:
 */
public final class AndroidUtils {
    private static final String TAG = "AndroidUtils";

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
                                                               @NonNull String permission) {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
    }
}
