package com.androidx.picker;

import android.database.Cursor;
import android.net.Uri;

import java.util.ArrayList;

import androidx.annotation.NonNull;

/**
 * description:
 */
public class UriDataHandler implements DataHandler<Uri> {
    final ArrayList<Uri> result = new ArrayList<>();

    @Override
    public Uri handle(@NonNull Cursor cursor, @NonNull ArrayList<String> projections, @NonNull Uri uri, String displayName, String mimeType, long size, long dateAdded, long dateModified, String data, String relativePath) {
        result.add(uri);
        return uri;
    }

    @Override
    public ArrayList<Uri> getDataResult() {
        return result;
    }
}
