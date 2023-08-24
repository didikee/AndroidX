package com.androidx.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;

import com.androidx.LogUtils;
import com.androidx.media.MimeType;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import androidx.annotation.NonNull;

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

    public static long copy(@NonNull InputStream in, @NonNull OutputStream out) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return android.os.FileUtils.copy(in, out);
        } else {
            final int BUFFER_SIZE = 8192;
            long nread = 0L;
            byte[] buf = new byte[BUFFER_SIZE];
            int n;
            while ((n = in.read(buf)) > 0) {
                out.write(buf, 0, n);
                nread += n;
            }
            return nread;
        }
    }

    public static void close(Closeable closeObj) {
        if (closeObj != null) {
            try {
                closeObj.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
