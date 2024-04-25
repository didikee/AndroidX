package com.androidx.media

import android.text.TextUtils
import com.androidx.media.ExifUtils.convertDMSFractionToDecimal
import com.androidx.tools.ImageUtils

/**
 * user author: didikee
 * create time: 4/28/21 5:31 PM
 * description:
 */
class ImageExif {
    var orientation = 0 // 旋转角度，整形表示，在ExifInterface中有常量对应表示
    var dateTime: String = "" //拍摄时间，取决于设备设置的时间
    var make: String = "" //设备品牌
    var model: String = "" //设备型号，整形表示，在ExifInterface中有常量对应表示
    var flash: String = "" //闪光灯
    var flashEnergy: String = "" //闪光灯进光量
    var imageLength = 0 //图片高度
    var imageWidth = 0 //图片宽度
    var gps: GPS = GPS()
    var exposureTime: String = "" //曝光时间
    var aperture: String = "" //光圈值
    var isoSpeed: String = "" //ISO 速度
    var isoSpeedRatings: String = "" //ISO感光度
    var dateTimeDigitized: String = "" //数字化时间

    /**
     * 一些数字相机每秒能拍摄 2~30 张照片, 但是DateTime/DateTimeOriginal/DateTimeDigitized 标签只能记录到秒单位的时间.
     * SubsecTime 标签就是用来记录秒后面的数据(微秒).
     * 例如, DateTimeOriginal = "1996:09:01 09:15:30", SubSecTimeOriginal = "130",
     * 合并起来的原始的拍摄时间就是 "1996:09:01 09:15:30.130"
     */
    var subSecTime: String = "" // 时间，微秒。拼接在 dateTimeDigitized 时间后面得到更精确的时间
    var subSecTimeOrig: String = ""
    var subSecTimeDig: String = ""
    var whiteBalance: String = "" //白平衡
    var focalLength: String = "" //焦距
    var processingMethod: String = "" //用于定位查找的全球定位系统处理方法。
    val isFlashOn: Boolean
        get() {
            val flashIndexValue = flashIndexValue()
            return if (flashIndexValue.length > 0) {
                flashIndexValue[0] == '1'
            } else false
        }

    private fun flashIndexValue(): String {
        if (!TextUtils.isEmpty(flash)) {
            try {
                val f = flash.toInt()
                val binaryString = Integer.toBinaryString(f)
                return StringBuilder(binaryString).reverse().toString()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return ""
    }

    class GPS {
        var latitude: String = "" //纬度
        var longitude: String = "" //经度
        var latitudeRef: String = "" //纬度名（N or S）
        var longitudeRef: String = "" //经度名（E or W）
        var altitude: String = "" //海拔高度
        var altitudeRef: String = "" //海拔高度
        var gpsTimeStamp: String = "" //时间戳
        var gpsDateStamp: String = "" //日期戳
        val isGPSAvailable: Boolean
            //判断gps的经纬度是否正常
            get() = !TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude)
        val lat: Double
            get() = convertDMSFractionToDecimal(latitude)
        val lng: Double
            get() = convertDMSFractionToDecimal(longitude)
        val latDisplay: String
            get() = ImageUtils.formatLongitudeAndLatitude(latitude)
        val lngDisplay: String
            get() = ImageUtils.formatLongitudeAndLatitude(longitude)
    }
}