package com.androidx.media;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * user author: didikee
 * create time: 2019-12-03 09:15
 * description: 
 */
public class MediaUriInfo extends BaseUriInfo implements Parcelable{
    private String displayName;
    private String mimeType;
    private int width;
    private int height;
    @Deprecated
    private String data;
    private String relativePath;
    private long size;
    private long dateAdded;
    private long dateModified;

    public MediaUriInfo() {
    }

    public MediaUriInfo(String mimeType, int width, int height) {
        this.mimeType = mimeType;
        this.width = width;
        this.height = height;
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

    public String getPath() {
        if (Build.VERSION.SDK_INT >= 29) {
            return getRelativePath();
        } else {
            return getData();
        }
    }

    @Override
    public String toString() {
        return "MediaUriInfo{" +
                "displayName='" + displayName + '\'' +
                ", mimeType='" + mimeType + '\'' +
                ", width=" + width +
                ", height=" + height +
                ", data='" + data + '\'' +
                ", relativePath='" + relativePath + '\'' +
                ", size=" + size +
                ", dateAdded=" + dateAdded +
                ", dateModified=" + dateModified +
                '}';
    }

    protected MediaUriInfo(Parcel in) {
        displayName = in.readString();
        mimeType = in.readString();
        width = in.readInt();
        height = in.readInt();
        data = in.readString();
        relativePath = in.readString();
        size = in.readLong();
        dateAdded = in.readLong();
        dateModified = in.readLong();
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(displayName);
        dest.writeString(mimeType);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeString(data);
        dest.writeString(relativePath);
        dest.writeLong(size);
        dest.writeLong(dateAdded);
        dest.writeLong(dateModified);
    }
}
