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
    fun d(message: String?) {
        if (DEBUG) {
            message?.let {
                Log.d(TAG, it)
            }

        }
    }

    @JvmStatic
    fun w(message: String?) {
        if (DEBUG) {
            message?.let {
                Log.w(TAG, it)
            }
        }
    }

    @JvmStatic
    fun e(message: String?) {
        if (DEBUG) {
            message?.let {
                Log.e(TAG, it)
            }
        }
    }

    fun d(tag: String?, message: String?) {
        if (DEBUG) {
            message?.let {
                Log.d(tag ?: TAG, it)
            }
        }
    }

    fun w(tag: String?, message: String?) {
        if (DEBUG) {
            message?.let {
                Log.w(tag ?: TAG, it)
            }
        }
    }

    fun e(tag: String?, message: String?) {
        if (DEBUG) {
            message?.let {
                Log.e(tag ?: TAG, it)
            }
        }
    }
}
