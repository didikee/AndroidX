package com.androidx

import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.annotation.ChecksSdkIntAtLeast

/**
 *
 * description:
 */
object MediaStoreUtils {
    // 当读取的时候就读取所有卷的数据，但是储存的时候只能往主要卷存数据
    val EXTERNAL_IMAGE_URI: Uri = if (isAndroidQ()) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val EXTERNAL_VIDEO_URI: Uri = if (isAndroidQ()) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val EXTERNAL_AUDIO_URI: Uri = if (isAndroidQ()) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    // 当存储时只建议往主要卷（第一个卷）存数据
    val EXTERNAL_IMAGE_PRIMARY_URI: Uri = if (isAndroidQ()) MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    else MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val EXTERNAL_VIDEO_PRIMARY_URI: Uri = if (isAndroidQ()) MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    else MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    val EXTERNAL_AUDIO_PRIMARY_URI: Uri = if (isAndroidQ()) MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

    fun isAndroidQ(): Boolean {
        return Build.VERSION.SDK_INT >= 29
    }


}