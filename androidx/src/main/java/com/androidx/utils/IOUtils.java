package com.androidx.utils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * user author: didikee
 * create time: 4/16/21 4:35 PM
 * description: 
 */
public class IOUtils {
    private IOUtils() {
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

    public static boolean transfer(InputStream in, OutputStream out) throws IOException {
        if (in != null && out != null) {
            int len;
            byte[] buffer = new byte[4 * 1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
            return true;
        }
        return false;
    }

    public static long transfer2(InputStream in, OutputStream out) throws IOException {
        if (in != null && out != null) {
            long total = 0;
            int len;
            byte[] buffer = new byte[4 * 1024];
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                total += len;
            }
            out.flush();
            return total;
        }
        return -1L;
    }
}
