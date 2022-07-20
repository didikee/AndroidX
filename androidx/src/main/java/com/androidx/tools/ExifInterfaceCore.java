package com.androidx.tools;

import android.content.ContentResolver;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;

import com.androidx.media.ImageExif;
import com.androidx.utils.IOUtils;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

/**
 * user author: didikee
 * create time: 4/28/21 5:34 PM
 * description: https://blog.csdn.net/u011002668/article/details/51490712
 */
class ExifInterfaceCore implements IExifInterface {

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public ImageExif decodeExif(@NonNull InputStream inputStream) {
        try {
            ExifInterface exifInterface = new ExifInterface(inputStream);
            return decodeExif(exifInterface);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(inputStream);
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public ImageExif decodeExif(@NonNull FileDescriptor fileDescriptor) {
        try {
            ExifInterface exifInterface = new ExifInterface(fileDescriptor);
            return decodeExif(exifInterface);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    private ImageExif decodeExif(@NonNull ExifInterface exifInterface) {
        ImageExif exif = new ImageExif();
        exif.orientation = parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION), 0);
        exif.dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        exif.make = exifInterface.getAttribute(ExifInterface.TAG_MAKE);
        exif.model = exifInterface.getAttribute(ExifInterface.TAG_MODEL);
        exif.flash = exifInterface.getAttribute(ExifInterface.TAG_FLASH);
        exif.imageLength = parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH), 0);
        exif.imageWidth = parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH), 0);
        exif.latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
        exif.longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
        exif.latitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
        exif.longitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        exif.exposureTime = exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME);
        exif.aperture = exifInterface.getAttribute(ExifInterface.TAG_APERTURE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            exif.isoSpeedRatings = exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS);
        } else {
            exif.isoSpeedRatings = exifInterface.getAttribute(ExifInterface.TAG_ISO);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            exif.dateTimeDigitized = exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED);
            exif.subSecTime = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME);
            exif.subSecTimeOrig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIG);
            exif.subSecTimeDig = exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIG);
        }
        exif.altitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE);
        exif.altitudeRef = exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF);
        exif.gpsTimeStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP);
        exif.gpsDateStamp = exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP);
        exif.whiteBalance = exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE);
        exif.focalLength = exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH);
        exif.processingMethod = exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD);
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
