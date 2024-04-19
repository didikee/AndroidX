package com.androidx.media;

import android.text.TextUtils;

import com.androidx.tools.ImageUtils;

/**
 * user author: didikee
 * create time: 4/28/21 5:31 PM
 * description: 
 */
public class ImageExif {
    public int orientation;         // 旋转角度，整形表示，在ExifInterface中有常量对应表示
    public String dateTime;         //拍摄时间，取决于设备设置的时间
    public String make;             //设备品牌
    public String model;            //设备型号，整形表示，在ExifInterface中有常量对应表示
    public String flash;            //闪光灯
    public String flashEnergy;      //闪光灯进光量
    public int imageLength;         //图片高度
    public int imageWidth;          //图片宽度
    public GPS gps;
    public String exposureTime;     //曝光时间
    public String aperture;         //光圈值
    public String isoSpeed;         //ISO 速度
    public String isoSpeedRatings;  //ISO感光度
    public String dateTimeDigitized;//数字化时间
    /**
     * 一些数字相机每秒能拍摄 2~30 张照片, 但是DateTime/DateTimeOriginal/DateTimeDigitized 标签只能记录到秒单位的时间.
     * SubsecTime 标签就是用来记录秒后面的数据(微秒).
     * 例如, DateTimeOriginal = "1996:09:01 09:15:30", SubSecTimeOriginal = "130",
     * 合并起来的原始的拍摄时间就是 "1996:09:01 09:15:30.130"
     */
    public String subSecTime;       // 时间，微秒。拼接在 dateTimeDigitized 时间后面得到更精确的时间
    public String subSecTimeOrig;
    public String subSecTimeDig;

    public String whiteBalance;     //白平衡
    public String focalLength;      //焦距
    public String processingMethod; //用于定位查找的全球定位系统处理方法。

    public final static class GPS{
        public String latitude;         //纬度
        public String longitude;        //经度
        public String latitudeRef;      //纬度名（N or S）
        public String longitudeRef;     //经度名（E or W）
        public String altitude;         //海拔高度
        public String altitudeRef;      //海拔高度
        public String gpsTimeStamp;     //时间戳
        public String gpsDateStamp;     //日期戳

        //判断gps的经纬度是否正常
        public boolean isGPSAvailable(){
            return !TextUtils.isEmpty(latitude) && !TextUtils.isEmpty(longitude);
        }
        public double getLat(){
            return ExifUtils.INSTANCE.convertDMSFractionToDecimal(latitude);
        }

        public double getLng(){
            return ExifUtils.INSTANCE.convertDMSFractionToDecimal(longitude);
        }

        public String getLatDisplay(){
            return ImageUtils.formatLongitudeAndLatitude(latitude);
        }
        public String getLngDisplay(){
            return ImageUtils.formatLongitudeAndLatitude(longitude);
        }
    }
}
