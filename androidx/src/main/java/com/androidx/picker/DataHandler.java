package com.androidx.picker;

import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * description:
 */
public interface DataHandler<T> {
    T handle(@NonNull Cursor cursor, @NonNull ArrayList<String> projections, @NonNull Uri uri,
             String displayName, String mimeType,
             long size, long dateAdded, long dateModified,
             String data, String relativePath
    );

    @NonNull
    ArrayList<T> getDataResult();
}
