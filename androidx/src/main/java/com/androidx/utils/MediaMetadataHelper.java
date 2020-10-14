package com.androidx.utils;

import android.content.Context;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.androidx.media.VideoMetaData;

import androidx.annotation.Nullable;

/**
 * user author: didikee
 * create time: 12/27/18 1:54 PM
 * description:
 *  ---------------------
 *  数据说明
 * （1）METADATA_KEY_ALBUM ：检索数据源的专辑标题信息的元数据键。
 * （2）METADATA_KEY_ALBUMARTIST： 检索与数据源相关的表演者或艺术家的信息的元数据键。
 * （3）METADATA_KEY_ARTIST：检索有关数据源的艺术家的信息的元数据键。
 * （4）METADATA_KEY_AUTHOR：检索有关数据源作者的信息的元数据键
 * （5）METADATA_KEY_BITRATE：此键检索平均比特率（以比特/秒），如果可用的话。
 * （6）METADATA_KEY_CD_TRACK_NUMBER：元数据关键字，用于检索描述原始数据记录中音频数据源的顺序的数字字符串。
 * （7）METADATA_KEY_COMPILATION：检索音乐专辑编辑状态的元数据键
 * （8）METADATA_KEY_COMPOSER：检索有关数据源的作曲家的信息的元数据键
 * （9）METADATA_KEY_DATE：检索或创建数据源时的日期的元数据键
 * （10）METADATA_KEY_DISC_NUMBER：用于检索描述音频数据源的集合的哪一部分的数字字符串的元数据键
 * （11）METADATA_KEY_DURATION：检索数据源回放持续时间的元数据键
 *  (12) METADATA_KEY_GENRE:检索数据源的内容类型或类型的元数据键
 * (13) METADATA_KEY_HAS_AUDIO:如果存在此密钥，则媒体包含音频内容
 * (14)METADATA_KEY_HAS_VIDEO:如果存在此密钥，则媒体包含视频内容
 * (15)METADATA_KEY_LOCATION:此键检索位置信息，如果可用的话。该位置应根据ISO-6709标准，在MP4/3GP框“@ XYZ”下指定。例如，经度为90度和纬度为180度的位置将被检索为“-90＋180”。
 * (16)METADATA_KEY_MIMETYPE:检索数据源MIME类型的元数据键。一些示例MIME类型包括："video/mp4", "audio/mp4", "audio/amr-wb"
 * (17)METADATA_KEY_NUM_TRACKS:元数据键，用于检索数据源（如MP4或3GPP文件）中的音轨的数目，如音频、视频、文本。
 * (18)METADATA_KEY_TITLE:检索数据源标题的元数据键
 * (19) METADATA_KEY_VIDEO_HEIGHT:如果媒体包含视频，则该键检索其高度
 * (20)METADATA_KEY_VIDEO_ROTATION:此键检索视频旋转角度的程度，如果可用的话。视频旋转角度可以是0, 90, 180度，也可以是270度
 * (21)METADATA_KEY_VIDEO_WIDTH:如果媒体包含视频，则该密钥检索其宽度
 * (22)METADATA_KEY_WRITER:检索数据源的作者（如歌词作者）信息的元数据键
 * (23) METADATA_KEY_YEAR:检索创建或修改数据源的一年的元数据密钥
 * (24) OPTION_CLOSEST:此选项与GETFrimeTimeTime（long，int）一起使用，以检索与最接近或给定时间的数据源相关联的帧（不一定是关键帧）
 * (25)OPTION_CLOSEST_SYNC:（时间）或给定时间的数据源相关联的同步（或密钥）帧。
 * (26)OPTION_NEXT_SYNC:此选项与GETFrimeTimeTime（long，int）一起使用，以检索与数据源相关联的同步（或密钥）帧，该数据源位于或在给定的时间之后。
 * (27)OPTION_PREVIOUS_SYNC:此选项与GETFrimeTimeTime（long，int）一起使用，以检索与数据源相关联的同步（或密钥）帧，该数据源正好位于给定时间之前或给定时间
 * ---------------------
 */
class MediaMetadataHelper {
    private static final String TAG = "MediaMetadataHelper";

    @Nullable
    public static VideoMetaData getVideoMetaData(Context context, Uri videoUri) {
        if (videoUri == null) {
            return null;
        }
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            //use one of overloaded setDataSource() functions to set your data source
            retriever.setDataSource(context, videoUri);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "" + e.getMessage());
        }
        if (retriever == null) {
            return null;
        }
        VideoMetaData metaData = new VideoMetaData();

        // retriever not null
        metaData.setDuration(parseLong(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION), 0));
        metaData.setRotation(parseInteger(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION), 0));
        metaData.setWidth(parseInteger(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH), 0));
        metaData.setHeight(parseInteger(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT), 0));
        metaData.setBitRate(parseInteger(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE), 0));

        MediaExtractor extractor = new MediaExtractor();
        try {
            //Adjust data source as per the requirement if file, URI, etc.
            extractor.setDataSource(context, videoUri, null);
            int numTracks = extractor.getTrackCount();
            for (int i = 0; i < numTracks; ++i) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime.startsWith("video/")) {
                    if (metaData.getFrameRate() <= 0 && format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                        int frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE);
                        if (frameRate > 0) {
                            metaData.setFrameRate(frameRate);
                        }
                    }
                    if (metaData.getDuration() <= 0 && format.containsKey(MediaFormat.KEY_DURATION)) {
                        long duration = format.getLong(MediaFormat.KEY_DURATION);
                        if (duration > 0) {
                            metaData.setDuration(duration);
                        }
                    }

                    if (metaData.getWidth() <= 0 && format.containsKey(MediaFormat.KEY_WIDTH)) {
                        int width = format.getInteger(MediaFormat.KEY_WIDTH);
                        if (width > 0) {
                            metaData.setWidth(width);
                        }
                    }
                    if (metaData.getHeight() <= 0 && format.containsKey(MediaFormat.KEY_HEIGHT)) {
                        int height = format.getInteger(MediaFormat.KEY_HEIGHT);
                        if (height > 0) {
                            metaData.setHeight(height);
                        }
                    }
                    if (metaData.getiFrameRate() <= 0 && format.containsKey(MediaFormat.KEY_I_FRAME_INTERVAL)) {
                        float iKeyRate = format.getFloat(MediaFormat.KEY_I_FRAME_INTERVAL);
                        if (iKeyRate > 0) {
                            metaData.setiFrameRate(iKeyRate);
                        }
                    }
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
//                        if (metaData.rotation <= 0 && format.containsKey(MediaFormat.KEY_ROTATION)) {
//                            int rotation = format.getInteger(MediaFormat.KEY_ROTATION);
//                            if (rotation )
//                            L.d("rotation: " + rotation);
//                            addInfoView(linearLayout, "旋转", String.valueOf(rotation));
//                        }
//                    }

                    if (metaData.getBitRate() <= 0 && format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                        int bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE);
                        if (bitRate > 0) {
                            metaData.setBitRate(bitRate);
                        }
                    }

//                    if (metaData.colorFormat <= 0 && format.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
//                        int colorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT);
//                        L.d("colorFormat: " + colorFormat);
//                        addInfoView(linearLayout, "颜色格式", String.valueOf(colorFormat));
//                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        } finally {
            //Release stuff
            extractor.release();
        }
        retriever.release();
        return metaData;

    }


    public static long parseLong(@Nullable String string, long defaultValue) {
        if (TextUtils.isEmpty(string)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }

    public static int parseInteger(@Nullable String string, int defaultValue) {
        if (TextUtils.isEmpty(string)) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(string);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return defaultValue;
        }
    }
}
