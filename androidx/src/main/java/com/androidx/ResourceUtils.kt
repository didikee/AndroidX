package com.androidx

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.TypedValue
import android.view.Menu
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.get
import androidx.core.view.size
import com.google.android.material.color.MaterialColors
import java.util.Locale

/**
 * user author: didikee
 * create time: 2025/11/28 10:14
 * description:
 */
object ResourceUtils {
    /**
     * 获取设备的当前系统语言
     * @param context
     * @return eg: zh-CN
     */
    fun getDeviceLanguage(context: Context): Locale {
        val locale = if (Build.VERSION.SDK_INT >= 24) {
            context.resources.configuration.locales[0]
        } else {
            context.resources.configuration.locale
        }
        return locale
    }

    fun getSelectableItemBackgroundResId(context: Context): Int {
        return getResourceId(context, android.R.attr.selectableItemBackground)
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

    fun getAttrResourceId(context: Context, attrResId: Int): Int {
        val outValue = TypedValue()
        context.theme.resolveAttribute(attrResId, outValue, true)
        return outValue.resourceId
    }

    @JvmStatic
    fun getSelectableItemBackground(context: Context): Int {
        return getAttrResourceId(context, android.R.attr.selectableItemBackground)
    }


    @ColorInt
    fun getMD3Color(
        context: Context,
        @AttrRes colorAttributeResId: Int,
        @ColorInt defaultValue: Int
    ): Int {
        return MaterialColors.getColor(
            context,
            colorAttributeResId,
            defaultValue
        )
    }

    @ColorInt
    fun getMD3ColorOnSurface(
        context: Context,
        @ColorInt defaultValue: Int,
        variant: Boolean = false
    ): Int {
        val id = if (variant) {
            com.google.android.material.R.attr.colorOnSurfaceVariant
        } else {
            com.google.android.material.R.attr.colorOnSurface
        }
        return getMD3Color(context, id, defaultValue)
    }

    @ColorInt
    fun getMD3ColorOutline(
        context: Context,
        @ColorInt defaultValue: Int,
        variant: Boolean = false
    ): Int {
        val id = if (variant) {
            com.google.android.material.R.attr.colorOutlineVariant
        } else {
            com.google.android.material.R.attr.colorOutline
        }
        return getMD3Color(context, id, defaultValue)
    }

    /**
     * 给 Menu 的所有图标统一着色
     *
     * @param menu Menu 对象
     * @param color 直接传颜色值
     */
    fun tintMenuIcons(@ColorInt color: Int, menu: Menu?) {
        menu?.let {
            for (i in 0 until it.size) {
                val menuItem = it[i]
                menuItem.icon?.let { icon ->
                    val wrapped: Drawable = DrawableCompat.wrap(icon).mutate()
                    DrawableCompat.setTint(wrapped, color)
                    menuItem.icon = wrapped
                }
            }
        }

    }

    /**
     * 给 Menu 的所有图标统一着色（传 colorRes 版本）
     */
    fun tintMenuIconsRes(context: Context, @ColorRes colorRes: Int, menu: Menu) {
        val color = ContextCompat.getColor(context, colorRes)
        tintMenuIcons(color, menu)
    }

    fun tintDrawable(drawable: Drawable, @ColorInt color: Int): Drawable {
        return tintDrawable(drawable,ColorStateList.valueOf(color))
    }

    fun tintDrawable(drawable: Drawable, colors: ColorStateList): Drawable {
        val wrappedDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTintList(wrappedDrawable, colors)
        return wrappedDrawable
    }

    fun isDarkTheme(resources: Resources): Boolean {
        val currentNightMode = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return currentNightMode == Configuration.UI_MODE_NIGHT_YES
    }
}