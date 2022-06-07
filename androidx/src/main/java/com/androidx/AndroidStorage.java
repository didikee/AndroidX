package com.androidx;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;

import com.androidx.media.MimeType;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * user author: didikee
 * create time: 8/19/20 11:28 AM
 * description: 汇集安卓存储相关的开放接口，只需要这一个类即可完成所有的储存操作
 *
 */
public final class AndroidStorage {

    public enum ContentType {
        IMAGE, VIDEO, FILE
    }

    public static class Builder {
        // 必要参数
        @NonNull
        private final ContentResolver resolver;/*资源管理类*/
        @NonNull
        private ContentType contentType;/*文件类型*/
        @NonNull
        private String folderName;/*文件夹名称，为了规范存储，所以必须要设置一级目录名称*/
        //--------------------------------------
        // 以下都是可选参数
        @Nullable
        private byte[] data;
        private String filename;
        private int width, height, rotate;
        private String mimeType;
        private long fileLength;

        private String subFolderName;

        public Builder(@NonNull ContentResolver contentResolver,
                       @NonNull ContentType contentType,
                       @NonNull String folderName
        ) {
            this.resolver = contentResolver;
            this.contentType = contentType;
            this.folderName = folderName;
        }

        public Builder filename(@NonNull String filename) {
            this.filename = filename;
            return this;
        }

        public Builder customFolder(@Nullable String subFolderName) {
            this.subFolderName = subFolderName;
            return this;
        }

        public Builder customFolder(@NonNull String folderName, @Nullable String subFolderName) {
            this.folderName = folderName;
            this.subFolderName = subFolderName;
            return this;
        }

        public Builder image(int width, int height, int rotate) {
            this.contentType = ContentType.IMAGE;
            this.width = width;
            this.height = height;
            this.rotate = rotate;
            return this;
        }

        public Builder mimeType(String mimeType) {
            this.mimeType = mimeType;
            return this;
        }

        public Builder fileLength(long fileLength) {
            this.fileLength = fileLength;
            return this;
        }

        private String checkFilename() {
            if (TextUtils.isEmpty(filename)) {
                switch (contentType) {
                    case IMAGE:
                        if (TextUtils.isEmpty(mimeType)) {
                            mimeType = MimeType.JPEG;
                        }
                        break;
                    case VIDEO:
                        if (TextUtils.isEmpty(mimeType)) {
                            mimeType = MimeType.MP4;
                        }
                        break;
                    case FILE:
                        if (TextUtils.isEmpty(mimeType)) {
                            mimeType = "";
                        }
                        break;
                }
                if (TextUtils.isEmpty(mimeType)) {
                    return "Unknown_" + System.currentTimeMillis();
                } else {
                    String extensionFromMimeType = MimeType.getExtensionFromMimeType(mimeType);
                    if (TextUtils.isEmpty(extensionFromMimeType)) {
                        return "Unknown_" + System.currentTimeMillis();
                    }
                    return System.currentTimeMillis() + "." + extensionFromMimeType;
                }
            }
            return filename;
        }

        private String checkMimeType() {
            if (TextUtils.isEmpty(mimeType)) {
                switch (contentType) {
                    case IMAGE:
                        mimeType = MimeType.JPEG;
                        break;
                    case VIDEO:
                        mimeType = MimeType.MP4;
                        break;
                    case FILE:
                        mimeType = MimeType.ALL;
                        break;
                }
            }
            return mimeType;
        }

        @Nullable
        public Uri save(@NonNull byte[] data) {
            if (contentType == ContentType.IMAGE) {
                // TODO 第一层文件夹名称应该也是可以为空,这样就直接存在根目录下,但是一般不推荐这样做
                String imageFolderPath = getImageFolderPath(folderName, subFolderName);
                if (TextUtils.isEmpty(imageFolderPath)) {
                    return null;
                }
                /**
                 * 需要先检查mimetype,之后的filename检测需要用到
                 */
                checkMimeType();
                ContentValues contentValues = StorageUriUtils.makeImageValues(
                        imageFolderPath,
                        checkFilename(),
                        checkMimeType(),
                        width,
                        height,
                        rotate,
                        fileLength);
                return saveImage(resolver, contentValues, data);
            } else if (contentType == ContentType.VIDEO) {

            } else if (contentType == ContentType.FILE) {

            }
            return null;
        }


    }


    public static Uri saveImage(ContentResolver contentResolver, ContentValues contentValues, byte[] bytes) {
        Uri externalContentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Uri uri = contentResolver.insert(externalContentUri, contentValues);
        if (uri == null) {
            return null;
        }
        OutputStream outputStream = null;
        try {
            outputStream = contentResolver.openOutputStream(uri);
            if (outputStream != null) {
                outputStream.write(bytes);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, false);
                    contentResolver.update(uri, contentValues, null, null);
                }
                return uri;
            }
        } catch (Exception e) {
            e.printStackTrace();
            contentResolver.delete(uri, null, null);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }


    public static String getAudioFolderPath(@NonNull String folderName, String subFolderName) {
        return getFolderPath(Environment.DIRECTORY_MUSIC, folderName, subFolderName);
    }

    public static String getImageFolderPath(@NonNull String folderName, String subFolderName) {
        return getFolderPath(Environment.DIRECTORY_PICTURES, folderName, subFolderName);
    }

    public static String getFolderPath(@NonNull String environment,
                                       @NonNull String rootFolderName,
                                       @Nullable String subFolderName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (TextUtils.isEmpty(subFolderName)) {
                return environment + File.separator + rootFolderName;
            } else {
                return environment + File.separator + rootFolderName + File.separator + subFolderName;
            }
        } else {
            String dcim = Environment.getExternalStoragePublicDirectory(environment).getAbsolutePath();
            File dir;
            if (TextUtils.isEmpty(subFolderName)) {
                dir = new File(dcim + File.separator + rootFolderName + File.separator);
            } else {
                dir = new File(dcim + File.separator + rootFolderName + File.separator + subFolderName + File.separator);
            }
            if (!dir.exists()) {
                boolean mkdirs = dir.mkdirs();
                if (!mkdirs) {
                    LogUtils.e("getFolderPath() mkdirs failed.");
                    return "";
                }
            }
            return dir.getAbsolutePath();
        }
    }
}

