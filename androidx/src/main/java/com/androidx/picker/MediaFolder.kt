package com.androidx.picker

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.io.Serializable

/**
 * Created by didikee on 2017/12/7.
 */
@Parcelize
open class MediaFolder(
    @JvmField var name: String = "",  // 当前文件夹的名字
    @JvmField var path: String = "",  // 当前文件夹的路径
    @JvmField var items: ArrayList<MediaItem> = ArrayList(),  // 当前文件夹下所有图片的集合
    @JvmField var check: Boolean = false  // 当前文件夹是否选中
) : Parcelable {
    val id: String
        get() = "${path}/${name}"

    // 用于缓存计算的文件夹总大小，避免每次访问都进行计算
    @IgnoredOnParcel
    @Transient
    private var cachedTotalFileSize: Long = 0

    // 获取封面图片
    fun getCover(): MediaItem? {
        return if (items.isNotEmpty()) items[0] else null
    }

    // 获取文件夹大小
    fun getSize(): Int {
        return items.size
    }

    // 计算文件夹的总文件大小，并缓存
    fun getTotalFileSize(): Long {
        // 如果缓存的大小有效，直接返回缓存的值
        if (cachedTotalFileSize != 0L) {
            return cachedTotalFileSize
        }

        // 计算所有图片文件的大小
        cachedTotalFileSize = items.sumOf { it.size }
        return cachedTotalFileSize
    }


    // 只要文件夹的路径和名字相同，就认为是相同的文件夹
    override fun equals(other: Any?): Boolean {
        return if (other is MediaFolder) {
            this.path.equals(other.path, ignoreCase = true) && this.name.equals(
                other.name,
                ignoreCase = true
            )
        } else {
            super.equals(other)
        }
    }

    // 重写hashCode方法，保证与equals一致
    override fun hashCode(): Int {
        return 31 * name.hashCode() + path.hashCode()
    }
}
