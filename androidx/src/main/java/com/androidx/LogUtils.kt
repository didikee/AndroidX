package com.androidx

import android.util.Log

/**
 * user author: didikee
 * create time: 2019-07-18 13:25
 * description:
 */
object LogUtils {
    private const val TAG = "AndroidX"
    private var DEBUG = true

    @JvmStatic
    fun setDebug(debug: Boolean) {
        DEBUG = debug
    }

    @JvmStatic
    fun d(message: String) {
        if (DEBUG) {
            Log.d(TAG, message)
        }
    }

    @JvmStatic
    fun w(message: String) {
        if (DEBUG) {
            Log.w(TAG, message)
        }
    }

    @JvmStatic
    fun e(message: String) {
        if (DEBUG) {
            Log.e(TAG, message)
        }
    }

    fun d(tag: String, message: String) {
        if (DEBUG) {
            Log.d(tag, message)
        }
    }

    fun w(tag: String, message: String) {
        if (DEBUG) {
            Log.w(tag, message)
        }
    }

    fun e(tag: String, message: String) {
        if (DEBUG) {
            Log.e(tag, message)
        }
    }
}
