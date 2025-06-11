package com.androidx.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.RectF
import android.os.Build
import android.util.DisplayMetrics
import android.util.Pair
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.annotation.DimenRes
import androidx.appcompat.app.AppCompatActivity
import com.androidx.media.Resolution
import kotlin.math.max

/**
 * user author: didikee
 * create time: 12/25/18 1:06 PM
 * description:
 */
object UiUtil {
    /**
     * 将px值转换为dip或dp值，保证尺寸大小不变
     *
     * @param pxValue
     * @param context context
     * （DisplayMetrics类中属性density）
     * @return float cast to int
     */
    fun px2dip(context: Context, pxValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()
    }

    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @param context context
     * （DisplayMetrics类中属性density）
     * @return float cast to int
     */
    fun dp2px(context: Context, dipValue: Float): Int {
        val scale = context.resources.displayMetrics.density
        return (dipValue * scale + 0.5f).toInt()
    }

    /**
     * 将px单位转换为sp
     *
     * @param pxValue need px
     * @param context context
     * DisplayMetrics类中属性scaledDensity
     * @return float cast to int
     */
    fun px2sp(context: Context, pxValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (pxValue / fontScale + 0.5f).toInt()
    }

    /**
     * 将sp值转换为px值，保证文字大小不变
     *
     * @param spValue need sp
     * @param context context
     * DisplayMetrics类中属性scaledDensity
     * @return float cast to int
     */
    fun sp2px(context: Context, spValue: Float): Int {
        val fontScale = context.resources.displayMetrics.scaledDensity
        return (spValue * fontScale + 0.5f).toInt()
    }

    /**
     * 获取系统状态栏高度
     * @param context context
     * @return statusBar height
     */
    fun getSystemStatusBarHeight(context: Context): Int {
        val resourceId = context.resources.getIdentifier(
            "status_bar_height", "dimen",
            "android"
        )
        return context.resources.getDimensionPixelSize(resourceId)
    }

    /**
     * 获取屏幕的宽高
     * @param context
     * @return
     */
    fun getWindowPixels(context: Context): Resolution {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val displayMetrics = DisplayMetrics()
        wm.defaultDisplay.getMetrics(displayMetrics)
        return Resolution(displayMetrics.widthPixels, displayMetrics.heightPixels)
    }

    /**
     * 获取dimen定义的尺寸，定义的是dp，此函数最终返回的是px
     *
     * @param res
     * @param dimenResId
     * @return
     */
    fun getDip(res: Resources, @DimenRes dimenResId: Int): Int {
        return res.getDimensionPixelSize(dimenResId)
    }

    // 计算grid布局的每行item个数
    fun calculateSpanCount(screenWidthPx: Int, itemWidthPx: Int): Int {
        return (screenWidthPx / itemWidthPx).coerceAtLeast(1)
    }

    /**
     * 将一个矩形放到另一个矩形的中，保持缩放和居中。
     * 类型Matrix的一个方法：[Matrix.setRectToRect]
     * @param src
     * @param dst
     * @return
     */
    fun setRectToRectCenterFit(src: RectF?, dst: RectF?): RectF? {
        if (src == null || dst == null) {
            return null
        }
        val matrix = Matrix()
        matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER)

        //        Log.d(TAG, "src: " + src.toString());
//        Log.d(TAG, "dst: " + dst.toString());
//        Log.d(TAG, "matrix: " + matrix.toString());
        val displayRectF = RectF()
        matrix.mapRect(displayRectF, src)

        //        Log.d(TAG, "displayRectF: " + displayRectF.toString());
        return displayRectF
    }

    fun fitToCenter(srcWidth: Int, srcHeight: Int, targetWidth: Int, targetHeight: Int): IntArray? {
        if (srcWidth <= 0 || srcHeight <= 0 || targetWidth <= 0 || targetHeight <= 0) {
            return null
        }
        if (srcWidth == targetWidth && srcHeight == targetHeight) {
            return intArrayOf(
                srcWidth, srcHeight
            )
        }


        val scaleX = srcWidth * 1f / targetWidth
        val scaleY = srcHeight * 1f / targetHeight
        val scale = max(scaleX.toDouble(), scaleY.toDouble()).toFloat()
        val width = (srcWidth / scale).toInt()
        val height = (srcHeight / scale).toInt()

        return intArrayOf(width, height)
    }

    /**
     * 设置全屏
     * 来自：https://www.jianshu.com/p/ce65dc7b0b56
     * @param activity
     * @param isShowStatusBar
     * @param isShowNavigationBar
     */
    fun setFullscreen(
        activity: AppCompatActivity,
        isShowStatusBar: Boolean,
        isShowNavigationBar: Boolean
    ) {
        var uiOptions = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)

        if (!isShowStatusBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_FULLSCREEN
        }
        if (!isShowNavigationBar) {
            uiOptions = uiOptions or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        }
        val window = activity.window
        window.decorView.systemUiVisibility = uiOptions

        //隐藏标题栏
        val supportActionBar = activity.supportActionBar
        supportActionBar?.hide()
        //专门设置一下状态栏导航栏背景颜色为透明，凸显效果。
        if (!isShowNavigationBar) {
            setNavigationStatusColor(window, Color.TRANSPARENT)
        }
    }

    fun setNavigationStatusColor(window: Window, color: Int) {
        //VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        if (Build.VERSION.SDK_INT >= 21) {
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.navigationBarColor = color
            window.statusBarColor = color
        }
    }
}
