package com.androidx.picker;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by didikee on 2017/12/7.
 */

public class MediaFolder implements Serializable, Parcelable {

    public String name;  //当前文件夹的名字
    public String path;  //当前文件夹的路径
    public ArrayList<MediaItem> images;  //当前文件夹下所有图片的集合

    public boolean check; //当前文件夹是否选中

    public MediaFolder() {
    }

    public MediaItem getCover() {
        if (images != null && images.size() > 0) {
            return images.get(0);
        }
        return null;
    }


    /** 只要文件夹的路径和名字相同，就认为是相同的文件夹 */
    @Override
    public boolean equals(Object o) {
        try {
            MediaFolder other = (MediaFolder) o;
            return this.path.equalsIgnoreCase(other.path) && this.name.equalsIgnoreCase(other.name);
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        return super.equals(o);
    }

    protected MediaFolder(Parcel in) {
        name = in.readString();
        path = in.readString();
        images = in.createTypedArrayList(MediaItem.CREATOR);
        check = in.readByte() != 0;
    }

    public static final Creator<MediaFolder> CREATOR = new Creator<MediaFolder>() {
        @Override
        public MediaFolder createFromParcel(Parcel in) {
            return new MediaFolder(in);
        }

        @Override
        public MediaFolder[] newArray(int size) {
            return new MediaFolder[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(path);
        dest.writeTypedList(images);
        dest.writeByte((byte) (check ? 1 : 0));
    }
}
