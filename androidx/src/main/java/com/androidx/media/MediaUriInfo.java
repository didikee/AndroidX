package com.androidx.media;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import androidx.annotation.NonNull;

/**
 * user author: didikee
 * create time: 2019-12-03 09:15
 * description:
 */
public class MediaUriInfo implements Parcelable {
    private long id;
    private String displayName;
    private String mimeType;
    @Deprecated
    private String data;
    private String relativePath;
    private long size;
    private long dateAdded;
    private long dateModified;
    private long dateTaken;
    // image & video
    private int width;
    private int height;
    private int rotate;
    // video & audio
    private long duration;
    private String xmp;

    public MediaUriInfo() {
    }

    public MediaUriInfo(String mimeType, int width, int height) {
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
    }

    protected MediaUriInfo(Parcel in) {
        id = in.readLong();
        displayName = in.readString();
        mimeType = in.readString();
        data = in.readString();
        relativePath = in.readString();
        size = in.readLong();
        dateAdded = in.readLong();
        dateModified = in.readLong();
        dateTaken = in.readLong();
        width = in.readInt();
        height = in.readInt();
        rotate = in.readInt();
        duration = in.readLong();
        xmp = in.readString();
    }

    public static final Creator<MediaUriInfo> CREATOR = new Creator<MediaUriInfo>() {
        @Override
        public MediaUriInfo createFromParcel(Parcel in) {
            return new MediaUriInfo(in);
        }

        @Override
        public MediaUriInfo[] newArray(int size) {
            return new MediaUriInfo[size];
        }
    };

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
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

    public Resolution getRealSize() {
        // 获取真实的宽高
        int realWidth = width;
        int realHeight = height;
        if (rotate == 90 || rotate == 270) {
            realWidth = realWidth ^ realHeight;
            realHeight = realWidth ^ realHeight;
            realWidth = realWidth ^ realHeight;
        }
        return new Resolution(realWidth, realHeight);
    }

    @NonNull
    public String getData() {
        return TextUtils.isEmpty(data) ? "" : data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @NonNull
    public String getRelativePath() {
        return TextUtils.isEmpty(relativePath) ? "" : relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath = relativePath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
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

    public long getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(long dateTaken) {
        this.dateTaken = dateTaken;
    }

    public int getRotate() {
        return rotate;
    }

    public void setRotate(int rotate) {
        this.rotate = rotate;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getXmp() {
        return xmp;
    }

    public void setXmp(String xmp) {
        this.xmp = xmp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(displayName);
        dest.writeString(mimeType);
        dest.writeString(data);
        dest.writeString(relativePath);
        dest.writeLong(size);
        dest.writeLong(dateAdded);
        dest.writeLong(dateModified);
        dest.writeLong(dateTaken);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(rotate);
        dest.writeLong(duration);
        dest.writeString(xmp);
    }
}
