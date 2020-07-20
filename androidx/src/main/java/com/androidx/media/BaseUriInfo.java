package com.androidx.media;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * user author: didikee
 * create time: 2019-12-03 09:14
 * description: 
 */
public class BaseUriInfo implements Parcelable {

    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public BaseUriInfo() {
    }

    protected BaseUriInfo(Parcel in) {
        id = in.readLong();
    }

    public static final Creator<BaseUriInfo> CREATOR = new Creator<BaseUriInfo>() {
        @Override
        public BaseUriInfo createFromParcel(Parcel in) {
            return new BaseUriInfo(in);
        }

        @Override
        public BaseUriInfo[] newArray(int size) {
            return new BaseUriInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
    }
}
