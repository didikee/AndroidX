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

    public static String getOrientationDescription(int orientation) {
        String desc;
        switch (orientation) {
            case 1:
                desc = "Flip horizontally";
                break;
            case 2:
                desc = "Flip vertically";
                break;
            case 3:
                desc = "Rotate 180 degrees";
                break;
            case 4:
                desc = "Rotate 180 degrees and flip horizontally";
                break;
            case 5:
                desc = "Rotate 270 degrees and flip horizontally";
                break;
            case 6:
                desc = "Rotate 90 degrees";
                break;
            case 7:
                desc = "Rotate 90 degrees and flip horizontally";
                break;
            case 8:
                desc = "Rotate 270 degrees";
                break;
            default:
                desc = "Normal";
        }
        return desc;
    }




}
