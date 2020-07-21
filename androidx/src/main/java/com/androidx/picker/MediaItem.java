package com.androidx.picker;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.androidx.media.MediaUriInfo;

/**
 * user author: didikee
 * create time: 2019-12-03 09:58
 * description:
 * Parcelable 的继承问题:
 * https://stackoverflow.com/questions/17725821/how-to-extend-android-class-which-implements-parcelable-interface
 */
public class MediaItem extends MediaUriInfo implements Parcelable {
    private Uri uri;

    public MediaItem(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public MediaItem(Parcel in) {
        super(in);
        uri = in.readParcelable(Uri.class.getClassLoader());
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
    }
}
