package com.androidx.media;

/**
 * user author: didikee
 * create time: 1/14/19 6:25 PM
 * description: 
 */
public final class VideoMetaData {

    private int width;
    private int height;

    private long duration;
    private long size;
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

    public int[] getRealSize() {
        // 获取真实的宽高
        int realWidth = width;
        int realHeight = height;
        if (rotation == 90 || rotation == 270) {
            realWidth = realWidth ^ realHeight;
            realHeight = realWidth ^ realHeight;
            realWidth = realWidth ^ realHeight;
        }
        return new int[]{realWidth, realHeight};
    }

    /**
     * 当这些信息是有的表示该视频信息合规
     * @return true：视频信息正确
     */
    public boolean isValid() {
        return width > 0 && height > 0 && duration > 0;
    }

    @Override
    public String toString() {
        return "VideoMetaData{" +
                "width=" + width +
                ", height=" + height +
                ", duration=" + duration +
                ", size=" + size +
                ", bitRate=" + bitRate +
                ", rotation=" + rotation +
                ", colorFormat=" + colorFormat +
                ", frameRate=" + frameRate +
                ", iFrameRate=" + iFrameRate +
                ", videoBitrate=" + videoBitrate +
                ", audioBitrate=" + audioBitrate +
                '}';
    }
}
