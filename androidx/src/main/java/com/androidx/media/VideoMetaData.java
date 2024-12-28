package com.androidx.media;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * user author: didikee
 * create time: 1/14/19 6:25 PM
 * description:
 */
public final class VideoMetaData implements Parcelable {
    //file info
    private String displayName;
    @Deprecated
    private String data;
    private String relativePath;
    private long dateModified;
    private long size;

    //video info
    private int width;
    private int height;
    private long duration;

    // 一下这些参数都可能没有
    private int bitRate;
    private int rotation;

    private int colorFormat;
    private int frameRate;//默认值
    private float iFrameRate;
    private double videoBitrate;
    private double audioBitrate;


    public VideoMetaData() {
    }

    public VideoMetaData(int width, int height, int rotation) {
        this.width = width;
        this.height = height;
        this.rotation = rotation;
    }

    protected VideoMetaData(Parcel in) {
        displayName = in.readString();
        data = in.readString();
        relativePath = in.readString();
        dateModified = in.readLong();
        size = in.readLong();
        width = in.readInt();
        height = in.readInt();
        duration = in.readLong();
        bitRate = in.readInt();
        rotation = in.readInt();
        colorFormat = in.readInt();
        frameRate = in.readInt();
        iFrameRate = in.readFloat();
        videoBitrate = in.readDouble();
        audioBitrate = in.readDouble();
    }

    public static final Creator<VideoMetaData> CREATOR = new Creator<VideoMetaData>() {
        @Override
        public VideoMetaData createFromParcel(Parcel in) {
            return new VideoMetaData(in);
        }

        @Override
        public VideoMetaData[] newArray(int size) {
            return new VideoMetaData[size];
        }
    };

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getBitRate() {
        return bitRate;
    }

    public void setBitRate(int bitRate) {
        this.bitRate = bitRate;
    }

    public int getRotation() {
        return rotation;
    }

    public void setRotation(int rotation) {
        this.rotation = rotation;
    }

    public int getColorFormat() {
        return colorFormat;
    }

    public void setColorFormat(int colorFormat) {
        this.colorFormat = colorFormat;
    }

    public int getFrameRate() {
        return frameRate;
    }

    public void setFrameRate(int frameRate) {
        this.frameRate = frameRate;
    }

    public float getiFrameRate() {
        return iFrameRate;
    }

    public void setiFrameRate(float iFrameRate) {
        this.iFrameRate = iFrameRate;
    }

    public double getVideoBitrate() {
        return videoBitrate;
    }

    public void setVideoBitrate(double videoBitrate) {
        this.videoBitrate = videoBitrate;
    }

    public double getAudioBitrate() {
        return audioBitrate;
    }

    public void setAudioBitrate(double audioBitrate) {
        this.audioBitrate = audioBitrate;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Resolution getRealSize() {
        // 获取真实的宽高
        int realWidth = width;
        int realHeight = height;
        if (rotation == 90 || rotation == 270) {
            realWidth = realWidth ^ realHeight;
            realHeight = realWidth ^ realHeight;
            realWidth = realWidth ^ realHeight;
        }
        return new Resolution(realWidth, realHeight);
    }

    @NonNull
    public String getPath() {
        if (Build.VERSION.SDK_INT >= 29) {
            if (!TextUtils.isEmpty(data)) {
                return getData();
            }
            return getRelativePath();
        } else {
            return getData();
        }
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    /**
     * 当这些信息是有的表示该视频信息合规
     *
     * @return true：视频信息正确
     */
    public boolean isValid() {
        return width > 0 && height > 0 && duration > 0;
    }

    @Override
    public String toString() {
        return "VideoMetaData{" +
                "displayName='" + displayName + '\'' +
                ", data='" + data + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", dateModified=" + dateModified +
                ", size=" + size +
                ", width=" + width +
                ", height=" + height +
                ", duration=" + duration +
                ", bitRate=" + bitRate +
                ", rotation=" + rotation +
                ", colorFormat=" + colorFormat +
                ", frameRate=" + frameRate +
                ", iFrameRate=" + iFrameRate +
                ", videoBitrate=" + videoBitrate +
                ", audioBitrate=" + audioBitrate +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(data);
        dest.writeString(relativePath);
        dest.writeLong(dateModified);
        dest.writeLong(size);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeLong(duration);
        dest.writeInt(bitRate);
        dest.writeInt(rotation);
        dest.writeInt(colorFormat);
        dest.writeInt(frameRate);
        dest.writeFloat(iFrameRate);
        dest.writeDouble(videoBitrate);
        dest.writeDouble(audioBitrate);
    }
}
