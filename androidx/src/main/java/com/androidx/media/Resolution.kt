package com.androidx.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * user author: didikee
 * create time: 2024/12/26 下午5:19
 * description: 用于保存多媒体的宽高数据，便于阅读而已
 */
@Parcelize
class Resolution(val width: Int, val height: Int) : Parcelable {

    fun getRealResolution(rotate: Int): Resolution {
        // 获取真实的宽高
        var realWidth = width
        var realHeight = height
        if (rotate == 90 || rotate == 270) {
            realWidth = realWidth xor realHeight
            realHeight = realWidth xor realHeight
            realWidth = realWidth xor realHeight
        }
        return Resolution(realWidth, realHeight)
    }
}