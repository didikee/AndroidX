package com.androidx;

import android.util.Log;



/**
 * user author: didikee
 * create time: 2019-07-18 13:25
 * description: 
 */
public class LogUtils {
    private static final String TAG = "AndroidX";
    private static boolean DEBUG = true;
    
    public static void setDebug(boolean debug){
        DEBUG = debug;
    }

    public static void d(String message) {
        if (DEBUG) {
            Log.d(TAG, message);
        }
    }

    public static void w(String message) {
        if (DEBUG) {
            Log.w(TAG, message);
        }
    }

    public static void e(String message) {
        if (DEBUG) {
            Log.e(TAG, message);
        }
    }
}
