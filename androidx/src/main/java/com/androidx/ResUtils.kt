package com.androidx

import android.R
import android.content.Context
import android.util.TypedValue

/**
 * user author: didikee
 * create time: 2025/11/28 10:14
 * description:
 */
object ResUtils {
    fun getSelectableItemBackgroundResId(context: Context): Int {
        return getResourceId(context, R.attr.selectableItemBackground)
    }

    /**
     * 获取一些内置主题的资源对应的id
     *
     * @param context
     * @param resid
     * @return
     */
    fun getResourceId(context: Context, resid: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(resid, outValue, true)
        return outValue.resourceId
    }
}