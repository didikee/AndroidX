package com.androidx.media

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * user author: didikee
 * create time: 2024/12/26 下午5:19
 * description: 用于保存多媒体的宽高数据，便于阅读而已
 */
@Parcelize
@Serializable
data class Resolution(val width: Int, val height: Int) : Parcelable {

    // 宽高都要大于0，那么这个媒体资源才是有效的，否则应该某个环节出了问题
    fun isValid(): Boolean {
        return width * height > 0
    }

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

    companion object {
        fun error(): Resolution {
            return Resolution(0, 0)
        }
    }
}