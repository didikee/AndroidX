package com.androidx.picker;

import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import androidx.annotation.NonNull;

import com.androidx.AndroidUtils;

/**
 * description:
 */
public class MediaItemDataHandler implements DataHandler<MediaItem> {
    final ArrayList<MediaItem> result = new ArrayList<>();

    @Override
    public MediaItem handle(@NonNull Cursor cursor, @NonNull ArrayList<String> projections,
                            @NonNull Uri uri, String displayName,
                            String mimeType, long size, long dateAdded,
                            long dateModified, String data, String relativePath) {
        int width = 0;
        int height = 0;
        long duration = 0;
        String xmp = "";
        int osVersion = AndroidUtils.getOSVersion();
        for (String projection : projections) {
            switch (projection) {
                case MediaStore.MediaColumns.WIDTH:
                    width = cursor.getInt(cursor.getColumnIndexOrThrow(projection));
                    break;
                case MediaStore.MediaColumns.HEIGHT:
                    height = cursor.getInt(cursor.getColumnIndexOrThrow(projection));
                    break;
                case MediaStore.MediaColumns.DURATION:
                    if (osVersion >= Build.VERSION_CODES.Q) {
                        duration = cursor.getLong(cursor.getColumnIndexOrThrow(projection));
                    } else {
                        // 低于android q无法直接查询到时长
                    }
                case "xmp": {
                    if (osVersion >= 30) {
                        byte[] xmpData = cursor.getBlob(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.XMP));
                        if (xmpData != null && xmpData.length > 0) {
                            // 将字节数组转换为字符串
                            xmp = new String(xmpData, StandardCharsets.UTF_8);
                        }
                    }
                }
                break;
            }
        }
        MediaItem mediaItem = new MediaItem(uri);
        // 设置公共参数
        mediaItem.setDisplayName(displayName);
        mediaItem.setSize(size);
        mediaItem.setMimeType(mimeType);
        mediaItem.setDateAdded(dateAdded);
        mediaItem.setDateModified(dateModified);
        mediaItem.setData(data);
        mediaItem.setRelativePath(relativePath);
        //封装实体
        mediaItem.setWidth(width);
        mediaItem.setHeight(height);
        mediaItem.setDuration(duration);
        mediaItem.setXmp(xmp);
        result.add(mediaItem);
        return mediaItem;
    }

    @Override
    public ArrayList<MediaItem> getResult() {
        return result;
    }
}
