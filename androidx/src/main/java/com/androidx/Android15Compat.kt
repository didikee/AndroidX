package com.androidx

import android.app.Activity
import android.view.View
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.google.android.material.appbar.AppBarLayout

/**
 * user author: didikee
 * create time: 2025/10/17 08:32
 * description:
 */
object Android15Compat {
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

    fun edgeWithRoot(
        layoutRoot: View,
    ) {
        ViewCompat.setOnApplyWindowInsetsListener(
            layoutRoot,
            OnApplyWindowInsetsListener { v, insets ->
                val systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
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
}
