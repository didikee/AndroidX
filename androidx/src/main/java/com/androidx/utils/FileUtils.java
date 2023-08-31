package com.androidx.utils;

import android.content.Context;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.text.InputFilter;
import android.text.Spanned;

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
    // 屏蔽的非法文件名字符
    public static final String BLOCK_FILE_CHARS = "\\/:*?\"<>|";

    public static InputFilter createFilenameInputFilter() {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && BLOCK_FILE_CHARS.contains(source)) {
                    return "";
                }
                return null;
            }
        };
    }

    public static InputFilter createBlockCharsInputFilter(String blockChars) {
        return new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (source != null && blockChars.contains(source)) {
                    return "";
                }
                return null;
            }
        };
    }


    /**
     * 创建默认时间样式的文件名：yyyyMMdd_HHmmss -> 20230831_173302
     * @param timeMs 毫秒
     * @return 默认样式(yyyyMMdd_HHmmss)的格式化时间字符串
     */
    public static String createFileDisplayName(long timeMs) {
        return TimeUtils.format(TimeUtils.DATE_FORMAT_1, timeMs).toString();
    }

    public static boolean isFilenameValid(String filename) {
        File file = new File(filename);
        try {
            String canonicalPath = file.getCanonicalPath();
            return true;
        } catch (IOException e) {
            LogUtils.d("isFilenameValid exception: " + e.getMessage());
        }
        return false;
    }

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
