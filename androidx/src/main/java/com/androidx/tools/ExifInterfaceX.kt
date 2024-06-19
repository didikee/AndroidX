package com.androidx.tools

import android.content.ContentResolver
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import com.androidx.media.ImageExif
import com.androidx.media.ImageExif.GPS
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.InputStream

/**
 * user author: didikee
 * create time: 4/28/21 5:34 PM
 * description: https://blog.csdn.net/u011002668/article/details/51490712
 */
class ExifInterfaceX : IExifInterface {
    override fun decodeExif(inputStream: InputStream): ImageExif? {
        try {
            val exifInterface = ExifInterface(inputStream)
            return decodeExif(exifInterface)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    override fun decodeExif(resolver: ContentResolver, uri: Uri): ImageExif? {
        try {
            val requestUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Get location data using the Exifinterface library.
                // Exception occurs if ACCESS_MEDIA_LOCATION permission isn't granted.
                // 来自：https://developer.android.com/training/data-storage/shared/media?hl=zh-cn#media-location-permission
                MediaStore.setRequireOriginal(uri)
            } else {
                uri
            }
            return decodeExif(resolver.openInputStream(requestUri)!!)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return null
    }

    override fun decodeExif(path: String): ImageExif? {
        try {
            val exifInterface = ExifInterface(path)
            return decodeExif(exifInterface)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    override fun decodeExif(fileDescriptor: FileDescriptor): ImageExif? {
        try {
            val exifInterface = ExifInterface(fileDescriptor)
            return decodeExif(exifInterface)
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return null
    }

    private fun decodeExif(exifInterface: ExifInterface): ImageExif {
        val exif = ImageExif()
        val gps = GPS()
        exif.orientation = parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION), 0)
        exif.dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME) ?: ""
        exif.make = exifInterface.getAttribute(ExifInterface.TAG_MAKE) ?: ""
        exif.model = exifInterface.getAttribute(ExifInterface.TAG_MODEL) ?: ""
        exif.flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH) ?: ""
        exif.flashEnergy = exifInterface.getAttribute(ExifInterface.TAG_FLASH_ENERGY) ?: ""
        exif.imageLength = parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH), 0)
        exif.imageWidth = parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH), 0)
        //gps 从androidQ开始需要声明权限才能获取到
        // 参考：https://github.com/expo/expo/issues/17399
        gps.latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE) ?: ""
        gps.longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) ?: ""
        gps.latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) ?: ""
        gps.longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF) ?: ""
        gps.altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE) ?: ""
        gps.altitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF) ?: ""
        gps.gpsTimeStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP) ?: ""
        gps.gpsDateStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP) ?: ""
        exif.exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) ?: ""
        exif.aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE) ?: ""
        exif.isoSpeed = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED) ?: ""
        exif.isoSpeedRatings =
            exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY) ?: ""
        exif.dateTimeDigitized =
            exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED) ?: ""
        exif.subSecTime = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME) ?: ""
        exif.subSecTimeOrig =
            exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL) ?: ""
        exif.subSecTimeDig =
            exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED) ?: ""
        exif.whiteBalance = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE) ?: ""
        exif.focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH) ?: ""
        exif.processingMethod =
            exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD) ?: ""
        exif.gps = gps
        return exif
    }

    private fun parseInt(text: String?, defaultValue: Int): Int {
        try {
            return text!!.toInt()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return defaultValue
    }
}