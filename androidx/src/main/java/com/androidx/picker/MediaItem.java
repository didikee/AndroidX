package com.androidx.picker;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.androidx.media.MediaUriInfo;

/**
 * user author: didikee
 * create time: 2019-12-03 09:58
 * description: 
 */
public class MediaItem extends MediaUriInfo implements Parcelable {
    private Uri uri;
    private long duration;

    public MediaItem(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public MediaItem(Parcel in) {
        super(in);
        uri = in.readParcelable(Uri.class.getClassLoader());
        duration = in.readLong();
    }

    public static final Creator<MediaItem> CREATOR = new Creator<MediaItem>() {
        @Override
        public MediaItem createFromParcel(Parcel in) {
            return new MediaItem(in);
        }

        @Override
        public MediaItem[] newArray(int size) {
            return new MediaItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(uri, flags);
        dest.writeLong(duration);
    }
}
