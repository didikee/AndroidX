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

    fun getRealResolution(rotateDegress: Int) {
        //TODO 获取真实的分辨率（宽高）
    }
}