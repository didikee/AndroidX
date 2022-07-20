package com.androidx.tools;

import android.content.ContentResolver;
import android.net.Uri;

import com.androidx.media.ImageExif;

import java.io.FileDescriptor;
import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 * user author: didikee
 * create time: 4/28/21 5:33 PM
 * description: 
 */
public interface IExifInterface {

    ImageExif decodeExif(@NonNull InputStream inputStream);

    ImageExif decodeExif(@NonNull ContentResolver resolver, @NonNull Uri uri);

    ImageExif decodeExif(@NonNull String path);

    ImageExif decodeExif(@NonNull FileDescriptor fileDescriptor);
}
