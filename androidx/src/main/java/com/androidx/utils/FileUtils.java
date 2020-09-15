package com.androidx.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;

import com.androidx.LogUtils;
import com.androidx.media.MimeType;

import java.io.File;

/**
 * user author: didikee
 * create time: 9/15/20 2:01 PM
 * description: 
 */
public final class FileUtils {
    private static final MediaScannerConnection.OnScanCompletedListener ON_SCAN_COMPLETED_LISTENER = new MediaScannerConnection.OnScanCompletedListener() {
        @Override
        public void onScanCompleted(String path, Uri uri) {
            LogUtils.d("FileUtils --> scanFile path: " + path + " uri: " + (uri == null ? " " : uri.toString()));
        }
    };

    public static void scanFile(Context context, File file) {
        if (file != null) {
            String mimeTypeFromFilename = MimeType.getMimeTypeFromFilename(file.getName());
            scanFile(context, file, mimeTypeFromFilename, null);
        }
    }

    public static void scanFile(Context context, File file, String mimeType, MediaScannerConnection.OnScanCompletedListener onScanCompletedListener) {
        if (context == null) {
            LogUtils.d("FileUtils --> scanFile context is null.");
            return;
        }
        if (file == null || !file.exists()) {
            return;
        }
        MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, new String[]{mimeType}, onScanCompletedListener == null ? ON_SCAN_COMPLETED_LISTENER : onScanCompletedListener);
    }
}
