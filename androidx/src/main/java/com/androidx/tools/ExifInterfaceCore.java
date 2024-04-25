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
@Deprecated
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
        exif.setOrientation(parseInt(exifInterface.getAttribute(ExifInterface.TAG_ORIENTATION), 0));
        exif.setDateTime(exifInterface.getAttribute(ExifInterface.TAG_DATETIME));
        exif.setMake(exifInterface.getAttribute(ExifInterface.TAG_MAKE));
        exif.setModel(exifInterface.getAttribute(ExifInterface.TAG_MODEL));
        exif.setFlash(exifInterface.getAttribute(ExifInterface.TAG_FLASH));
        exif.setImageLength(parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH), 0));
        exif.setImageWidth(parseInt(exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH), 0));

        exif.setExposureTime(exifInterface.getAttribute(ExifInterface.TAG_EXPOSURE_TIME));
        exif.setAperture(exifInterface.getAttribute(ExifInterface.TAG_APERTURE));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            exif.setIsoSpeedRatings(exifInterface.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS));
        } else {
            exif.setIsoSpeedRatings(exifInterface.getAttribute(ExifInterface.TAG_ISO));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            exif.setDateTimeDigitized(exifInterface.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED));
            exif.setSubSecTime(exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME));
            exif.setSubSecTimeOrig(exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_ORIG));
            exif.setSubSecTimeDig(exifInterface.getAttribute(ExifInterface.TAG_SUBSEC_TIME_DIG));
        }

        exif.setWhiteBalance(exifInterface.getAttribute(ExifInterface.TAG_WHITE_BALANCE));
        exif.setFocalLength(exifInterface.getAttribute(ExifInterface.TAG_FOCAL_LENGTH));
        exif.setProcessingMethod(exifInterface.getAttribute(ExifInterface.TAG_GPS_PROCESSING_METHOD));

        ImageExif.GPS gps =new ImageExif.GPS();
        gps.setLatitude(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE));
        gps.setLongitude(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));
        gps.setLatitudeRef(exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF));
        gps.setLongitudeRef(exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF));
        gps.setAltitude(exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE));
        gps.setAltitudeRef(exifInterface.getAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF));
        gps.setGpsTimeStamp(exifInterface.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP));
        gps.setGpsDateStamp(exifInterface.getAttribute(ExifInterface.TAG_GPS_DATESTAMP));
        exif.setGps(gps);
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
