package com.androidx.ime

import android.app.Activity
import android.content.res.Resources
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.annotation.RequiresApi
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.androidx.LogUtils

/**
 * user author: didikee
 * create time: 2025/7/18 下午5:13
 * description: 来自：https://juejin.cn/post/7150453629021847566#heading-1 的方案1：
 * 关于 键盘的几种形式：https://juejin.cn/post/7152316798669422622
 */
class KeyboardHeightObserverLegcy(
    private val activity: Activity,
    private val listener: (imeHeight: Int, navBarHeight: Int) -> Unit
) {

    private var lastKeyboardHeight = 0
    private var navBarHeight = 0
    private var decorDelta = 0
    private var isListening = false
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null
    private val contentView: ViewGroup = activity.findViewById(android.R.id.content)

    /**
     * 开始监听键盘高度
     */
    fun start() {
        if (isListening) return
        isListening = true

        getNavigationBarHeight(activity) { navHeight, _ ->
            LogUtils.d("getNavigationBarHeight: navHeight = ${navHeight}")
            navBarHeight = navHeight
            lastKeyboardHeight = getKeyboardHeight()

            globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
                val currentHeight = getKeyboardHeight()
                LogUtils.d("KeyboardHeightObserverLegcy currentHeight: ${currentHeight}")
                if (currentHeight != lastKeyboardHeight) {
                    listener.invoke(currentHeight, navHeight)
                    lastKeyboardHeight = currentHeight
                }
            }

            contentView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
        }
    }

    /**
     * 停止监听，释放资源
     */
    fun stop() {
        if (!isListening) return
        globalLayoutListener?.let {
            contentView.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }
        globalLayoutListener = null
        isListening = false
    }

    /**
     * 获取键盘高度（基于 decorView 的不可见区域）
     */
    private fun getKeyboardHeight(): Int {
        val decorView = activity.window.decorView ?: return lastKeyboardHeight
        val rect = Rect()
        decorView.getWindowVisibleDisplayFrame(rect)
        val delta = kotlin.math.abs(decorView.bottom - rect.bottom)

        return if (delta <= navBarHeight) {
            decorDelta = delta
            0
        } else {
            delta - decorDelta
        }
    }


    companion object {

        /**
         * 获取导航栏高度（异步）
         */
        fun getNavigationBarHeight(
            activity: Activity,
            callback: (height: Int, hasNavBar: Boolean) -> Unit
        ) {
            val decorView = activity.window.decorView
            if (ViewCompat.isAttachedToWindow(decorView)) {
                val insets = ViewCompat.getRootWindowInsets(decorView)
                val navInsets =
                    insets?.getInsets(WindowInsetsCompat.Type.navigationBars()) ?: Insets.NONE
                val hasNavBar =
                    insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true &&
                            navInsets.bottom > 0
                val height = if (navInsets.bottom > 0) navInsets.bottom else getSystemNavBarHeight()
                callback(height, hasNavBar)
            } else {
                decorView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        decorView.removeOnAttachStateChangeListener(this)
                        val insets = ViewCompat.getRootWindowInsets(decorView)
                        val navInsets =
                            insets?.getInsets(WindowInsetsCompat.Type.navigationBars())
                                ?: Insets.NONE
                        val hasNavBar =
                            insets?.isVisible(WindowInsetsCompat.Type.navigationBars()) == true &&
                                    navInsets.bottom > 0
                        val height =
                            if (navInsets.bottom > 0) navInsets.bottom else getSystemNavBarHeight()
                        callback(height, hasNavBar)
                    }

                    override fun onViewDetachedFromWindow(v: View) = Unit
                })
            }
        }

        /**
         * 通过资源 ID 获取系统导航栏高度
         */
         fun getSystemNavBarHeight(): Int {
            val res = Resources.getSystem()
            val id = res.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (id > 0) res.getDimensionPixelSize(id) else 0
        }
    }
}