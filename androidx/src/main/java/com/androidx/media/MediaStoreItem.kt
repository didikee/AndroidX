package com.androidx.media

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * user author: didikee
 * create time: 2025/12/7 13:30
 * description: 从MediaStore中查询出来的媒体
 */
@Parcelize
data class MediaStoreItem(
    val uri: Uri,
    val info: MediaStoreInfo,
) : Parcelable
