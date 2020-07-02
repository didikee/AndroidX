package com.androidx.media;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * user author: didikee
 * create time: 2019-12-03 09:14
 * description: 
 */
public abstract class BaseUriInfo implements Parcelable {
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

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
    }
}
