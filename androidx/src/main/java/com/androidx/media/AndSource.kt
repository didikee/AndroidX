package com.androidx.media

import android.net.Uri
import android.os.Parcel
import android.os.Parcelable
import com.androidx.utils.UriUtils
import kotlinx.parcelize.Parcelize
import java.io.File

/**
 * user author: didikee
 * create time: 2026/1/15 16:32
 * description:
 */

sealed class AndSource : Parcelable {
    abstract fun readUri(): Uri

    @Parcelize
    data class FromUri(val uri: Uri) : AndSource() {
        override fun readUri(): Uri {
            return uri
        }
    }

    @Parcelize
    data class FromFile(val path: String) : AndSource() {
        fun toFile(): File = File(path)
        override fun readUri(): Uri {
            return Uri.fromFile(toFile())
        }
    }

    @Parcelize
    data class FromAssets(val assetsRelativePath: String) : AndSource() {

        override fun readUri(): Uri {
            return UriUtils.createUri(assetsRelativePath)
        }

    }
}