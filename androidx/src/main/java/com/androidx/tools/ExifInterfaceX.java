package com.androidx.tools;

import android.content.ContentResolver;
import android.net.Uri;

import com.androidx.media.ImageExif;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.exifinterface.media.ExifInterface;

/**
 * user author: didikee
 * create time: 4/28/21 5:34 PM
 * description: https://blog.csdn.net/u011002668/article/details/51490712
 */
public class ExifInterfaceX implements IExifInterface {

    @Override
    public ImageExif decodeExif(@NonNull InputStream inputStream) {
        try {
            ExifInterface exifInterface = new ExifInterface(inputStream);
            return decodeExif(exifInterface);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ImageExif decodeExif(@NonNull ContentResolver resolver, @NonNull Uri uri) {
        try {
            return decodeExif(resolver.openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ImageExif decodeExif(@NonNull String path) {
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            return decodeExif(exifInterface);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public ImageExif decodeExif(@NonNull FileDescriptor fileDescriptor) {
        try {
            ExifInterface exifInterface = new ExifInterface(fileDescriptor);
            return decodeExif(exifInterface);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public ImageExif decodeExif(@NonNull ExifInterface exifInterface) {
        ImageExif exif = new ImageExif();
        ImageExif.GPS gps =new ImageExif.GPS();
        exif.orientation = parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION), 0);
        exif.dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        exif.make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
        exif.model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        exif.flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
        exif.flashEnergy = exifInterface.getAttribute(ExifInterface.TAG_FLASH_ENERGY);
        exif.imageLength = parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH), 0);
        exif.imageWidth = parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH), 0);
        //gps 从androidQ开始需要声明权限才能获取到
        // 参考：https://github.com/expo/expo/issues/17399
        gps.latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        gps.longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        gps.latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        gps.longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        gps.altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        gps.altitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
        gps.gpsTimeStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
        gps.gpsDateStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);

        exif.exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        exif.aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE_VALUE);
        exif.isoSpeed = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED);
        exif.isoSpeedRatings = exifInterface.getAttribute(ExifInterface.TAG_PHOTOGRAPHIC_SENSITIVITY);
        exif.dateTimeDigitized = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
        exif.subSecTime = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME);
        exif.subSecTimeOrig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIGINAL);
        exif.subSecTimeDig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIGITIZED);

        exif.whiteBalance = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
        exif.focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        exif.processingMethod = exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);

        exif.gps = gps;
        return exif;
    }


    private int parseInt(String text, int defaultValue) {
        try {
            return Integer.parseInt(text);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return defaultValue;
    }
}
