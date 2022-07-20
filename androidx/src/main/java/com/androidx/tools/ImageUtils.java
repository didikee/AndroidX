package com.androidx.tools;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.androidx.media.ImageExif;
import com.androidx.utils.UriUtils;

import java.io.File;

import androidx.annotation.Nullable;

/**
 * user author: didikee
 * create time: 4/28/21 5:30 PM
 * description: 
 */
public final class ImageUtils {

    @Nullable
    public static ImageExif getExif(String path) {
        return new ExifInterfaceCore().decodeExif(path);
    }

    @Nullable
    public static ImageExif getExif(Context context, Uri uri) {
        if (context == null || uri == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new ExifInterfaceCore().decodeExif(context.getContentResolver(), uri);
        } else {
            String pathFromUri = UriUtils.getPathFromUri(context, uri);
            if (!TextUtils.isEmpty(pathFromUri)) {
                File file = new File(pathFromUri);
                if (file.exists()) {
                    return new ExifInterfaceCore().decodeExif(pathFromUri);
                }
            }
        }
        return null;
    }


}
