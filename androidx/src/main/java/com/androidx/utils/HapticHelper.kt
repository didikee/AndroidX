package com.androidx.utils

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * user author: didikee
 * create time: 2025/7/22 下午1:59
 * description:
 */
object HapticHelper {
    /**
     * 轻触反馈（如按钮点击、虚拟按键）
     * 30ms，默认强度	轻触
     */
    fun performTap(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
    }

    /**
     * 长按反馈（反馈更强烈，用于拖拽、弹出菜单等场景）
     * 50ms，强度高	重震
     */
    fun performLongPress(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
    }

    /**
     * 输入键盘点击反馈（适用于自定义键盘、输入控件）
     */
    fun performKeyClick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
    }

    /**
     * 滚动节拍反馈（适用于时钟、选择器等滚动节拍场景）
     * 15ms，低强度	极轻
     */
    fun performTick(view: View) {
        view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

    /**
     * 文本光标或句柄移动反馈（适用于文本编辑器类控件）
     */
    fun performTextHandleMove(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.TEXT_HANDLE_MOVE)
        }
    }

    /**
     * 系统确认反馈（适用于关键确认操作，Android 11+ 可用）
     * 2段式震动	反馈更鲜明
     */
    fun performConfirm(view: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
        }
    }

    /**
     * 自定义通用触觉反馈（仅在支持设备上有效）
     */
    fun performCustom(view: View, feedbackConstant: Int) {
        view.performHapticFeedback(feedbackConstant)
    }
}