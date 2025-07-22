package com.androidx.ime

import android.content.Context
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.ViewConfiguration

/**
 * user author: didikee
 * create time: 2025/7/19 下午4:46
 * description:
 */
object NavigationBarChecker {
    fun getNavigationBarHeight(context: Context): Int {
        val res = context.resources
        val resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android")
        return if (resourceId > 0 && hasNavigationBar(context)) {
            res.getDimensionPixelSize(resourceId)
        } else 0
    }

    fun hasNavigationBar(context: Context): Boolean {
        val id = context.resources.getIdentifier("config_showNavigationBar", "bool", "android")
        return if (id > 0) {
            context.resources.getBoolean(id)
        } else {
            // 备用判断：检查是否有物理返回键
            val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
            val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
            !(hasMenuKey || hasBackKey)
        }
    }

}