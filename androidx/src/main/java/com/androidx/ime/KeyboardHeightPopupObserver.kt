package com.androidx.ime

import android.app.Activity
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.PopupWindow
import com.androidx.LogUtils

/**
 * user author: didikee
 * create time: 2025/7/18 下午5:22
 * description:
 * 参考1：https://juejin.cn/post/7150453629021847566#heading-2
 * 参考2：https://github.com/siebeprojects/samples-keyboardheight/blob/develop/app/src/main/java/com/siebeprojects/samples/keyboardheight/KeyboardHeightProvider.java
 */
class KeyboardHeightPopupObserver(
    private val activity: Activity,
    private val keyboardHeightListener: ((imeHeight: Int, orientation: Int) -> Unit)? = null
) : PopupWindow(activity) {

    private var offset = 0
    private var popupView: View
    private var parentView: View
    private var globalLayoutListener: ViewTreeObserver.OnGlobalLayoutListener? = null

    /** The cached landscape height of the keyboard  */
    private var keyboardLandscapeHeight = 0

    /** The cached portrait height of the keyboard  */
    private var keyboardPortraitHeight = 0

    init {
        popupView = LinearLayout(
            activity
        ).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            background = ColorDrawable(Color.TRANSPARENT)
        }
        contentView = popupView

        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = INPUT_METHOD_NEEDED

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT

        parentView = activity.findViewById(android.R.id.content)

        globalLayoutListener = ViewTreeObserver.OnGlobalLayoutListener {
            if (popupView.isAttachedToWindow) {
                handleOnGlobalLayout()
            }
        }
        popupView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
    }

    fun start() {
        if (isShowing) {
            return
        }
        if (parentView.isAttachedToWindow) {
            startInternal()
        } else {
            parentView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    startInternal()
                }

                override fun onViewDetachedFromWindow(v: View) {
                    // no-op
                }
            })
        }
    }

    fun stop() {
        globalLayoutListener?.let {
            popupView.viewTreeObserver.removeOnGlobalLayoutListener(it)
        }
        globalLayoutListener = null
        dismiss()
    }

    private fun startInternal() {
        if (!isShowing && parentView.windowToken != null) {
            setBackgroundDrawable(ColorDrawable(0))
            showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
        }
    }

    private fun handleOnGlobalLayout() {
        val screenSize = Point()
        activity.windowManager.defaultDisplay.getSize(screenSize)

        val rect = Rect()
        popupView.getWindowVisibleDisplayFrame(rect)


        // REMIND, you may like to change this using the fullscreen size of the phone
        // and also using the status bar and navigation bar heights of the phone to calculate
        // the keyboard height. But this worked fine on a Nexus.
        val orientation: Int = getScreenOrientation()
        val height = screenSize.y - rect.bottom
        LogUtils.d("KeyboardHeightPopupObserver screenSize.y - rect.bottom --> ${height}")
        val keyboardHeight = if (height < 0) {
            offset = Math.abs(height)
            0
        } else {
            height + offset
        }

        if (keyboardHeight == 0) {
            notifyKeyboardHeightChanged(0, orientation)
        } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            this.keyboardPortraitHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardPortraitHeight, orientation)
        } else {
            this.keyboardLandscapeHeight = keyboardHeight
            notifyKeyboardHeightChanged(keyboardLandscapeHeight, orientation)
        }
    }

    private fun getScreenOrientation(): Int {
        return activity.resources.configuration.orientation
    }

    private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
        keyboardHeightListener?.invoke(height, orientation)
    }
}