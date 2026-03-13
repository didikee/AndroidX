package com.androidx

import android.app.Activity
import android.view.View
import android.view.Window
import androidx.appcompat.widget.Toolbar
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.androidx.compat.EdgeInsetsConfig
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.card.MaterialCardView

/**
 * user author: didikee
 * create time: 2025/10/17 08:32
 * description:
 */
object Android15Compat {

    /**
     * 不管顶部和底部的view是什么，只有是支持修改padding的即可
     * 注意：如果view的高度是固定的，那么修改padding是无法改变大小的，所以请注意，需要wrap_content的view
     */
    fun edge(
        rootView: View,
        topView: View? = null,
        bottomView: View? = null,
        config: EdgeInsetsConfig = EdgeInsetsConfig()
    ) {
        // 记录初始 padding，避免重复叠加
        val topInitialPadding = topView?.paddingTop ?: 0
        val bottomInitialPadding = bottomView?.paddingBottom ?: 0

        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            if (config.applyTop) {
                val targetTop = topView ?: rootView
                targetTop.setPadding(
                    targetTop.paddingLeft,
                    topInitialPadding + systemBars.top,
                    targetTop.paddingRight,
                    targetTop.paddingBottom
                )
            }

            if (config.applyBottom) {
                val targetBottom = bottomView ?: rootView
                targetBottom.setPadding(
                    targetBottom.paddingLeft,
                    targetBottom.paddingTop,
                    targetBottom.paddingRight,
                    bottomInitialPadding + systemBars.bottom
                )
            }

            if (config.consumeInsets) {
                WindowInsetsCompat.CONSUMED
            } else {
                insets
            }
        }

//        // 触发一次
//        ViewCompat.requestApplyInsets(rootView)
    }

    // 利用appbar实现md2风格的状态栏 + Toolbar
    fun edgeWithAppBar(
        layoutRoot: View,
        appBar: AppBarLayout,
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(
            layoutRoot,
            OnApplyWindowInsetsListener { v, insets ->
                val systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                appBar.setPadding(0, systemBars.top, 0, 0)
                insets
            })
    }

    fun edgeWithToolbar(
        layoutRoot: View,
        toolbar: Toolbar,
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(
            layoutRoot,
            OnApplyWindowInsetsListener { v, insets ->
                val systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
                toolbar.updatePadding(top = systemBars.top)
                insets
            })
    }

    fun edgeWithRoot(
        layoutRoot: View,
        bottomPadding: Int = -1
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(
            layoutRoot,
            OnApplyWindowInsetsListener { v, insets ->
                val systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars())
                val bp = if (bottomPadding >= 0) bottomPadding else systemBars.bottom
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, bp)
                insets
            })
    }


    // 顶部的padding给root，底部的给底部view。这要求底部的布局不能写死，最好是wrap_content的
    fun edgeWithBottom(
        layoutRoot: View,
        bottomView: View
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(
            layoutRoot,
            OnApplyWindowInsetsListener { v, insets ->
                val systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
                if (bottomView is MaterialCardView) {
                    bottomView.setContentPadding(
                        bottomView.contentPaddingLeft,
                        bottomView.contentPaddingTop,
                        bottomView.paddingRight,
                        systemBars.bottom
                    )
                } else {
                    bottomView.updatePadding(bottom = systemBars.bottom)
                }
                insets
            })
    }

    /**
     * lightStatusBars: false 白色状态栏
     * lightStatusBars: true  黑色状态栏
     */
    fun tintStatusBars(activity: Activity?, lightStatusBars: Boolean) {
        activity?.let {
            val window = it.window
            window.decorView.post {
                val controller =
                    WindowInsetsControllerCompat(window, window.decorView)
                // lightStatusBars = false -> 状态栏白色文字
                // lightStatusBars = true  -> 状态栏黑色文字
                controller.isAppearanceLightStatusBars = lightStatusBars
            }
        }
    }

    // 启用Edge to Edge 特性
    fun enableEdgeToEdge(window: Window?) {
        window?.let { WindowCompat.setDecorFitsSystemWindows(it, false) }
    }
}
