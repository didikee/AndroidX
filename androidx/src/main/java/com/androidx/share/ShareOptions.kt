package com.androidx.share

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * user author: didikee
 * create time: 2026/1/14 16:16
 * description:
 */
@Parcelize
data class ShareOptions(
    val title: String? = null,
    val packageName: String? = null,
    val className: String? = null,
) : Parcelable {

    companion object {
        @JvmStatic
        fun create(): ShareOptions = ShareOptions()
    }
}
