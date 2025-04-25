package com.androidx.utils

import android.content.ContentResolver
import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.text.TextUtils
import com.androidx.LogUtils
import com.androidx.media.VideoMetaData
import java.io.File
import java.io.IOException

/**
 * user author: didikee
 * create time: 12/27/18 1:54 PM
 * description:
 * ---------------------
 * 数据说明
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
 * (12) METADATA_KEY_GENRE:检索数据源的内容类型或类型的元数据键
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
internal object MediaMetadataHelper {
    private const val TAG = "MediaMetadataHelper"
    private const val SECOND = 1000 * 1000.0

    @JvmStatic
    fun getVideoMetaData(context: Context, videoUri: Uri): VideoMetaData {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val pathFromUri = UriUtils.getPathFromUri(context, videoUri)
            if (!TextUtils.isEmpty(pathFromUri)) {
                val file = File(pathFromUri)
                if (file.exists() && file.length() > 0) {
                    return getVideoMetaData(file)
                }
            }
            return getVideoMetaDataForUri(context, videoUri) // fallback
        } else {
            return getVideoMetaDataForUri(context, videoUri)
        }
    }

    private fun getVideoMetaDataForUri(context: Context, videoUri: Uri): VideoMetaData {
        val metaData = VideoMetaData()
        val baseInfo = UriUtils.getBaseInfo(context.contentResolver, videoUri)
        baseInfo?.let {
            metaData.size = it.size
            metaData.displayName = it.displayName
            metaData.data = it.data
            metaData.dateModified = it.dateModified
            metaData.relativePath = it.relativePath
        }
        try {
            MediaMetadataRetriever().use { retriever ->
                retriever.setDataSource(context, videoUri)
                fillDataFromMediaMetadataRetriever(retriever, metaData)
            }
        } catch (e: Exception) {
            LogUtils.e("MediaMetadataRetriever error: ${e.message}")
        }

        var mediaExtractor: MediaExtractor? = null
        try {
            mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(context, videoUri, null)
            fillDataFromMediaExtractor(mediaExtractor, metaData)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mediaExtractor?.release()
        }
        return metaData
    }


    @JvmStatic
    fun getVideoMetaData(videoFile: File?): VideoMetaData {
        val metaData = VideoMetaData()
        if (videoFile == null || !videoFile.exists()) {
            return metaData
        }
        metaData.size = videoFile.length()
        metaData.displayName = videoFile.name
        metaData.data = videoFile.absolutePath
        metaData.dateModified = videoFile.lastModified()

        var mediaMetadataRetriever: MediaMetadataRetriever? = null
        try {
            mediaMetadataRetriever = MediaMetadataRetriever()
            mediaMetadataRetriever.setDataSource(videoFile.absolutePath)
            fillDataFromMediaMetadataRetriever(mediaMetadataRetriever, metaData)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } finally {
            IOUtils.close(mediaMetadataRetriever)
        }

        var mediaExtractor: MediaExtractor? = null
        try {
            mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(videoFile.absolutePath)
            fillDataFromMediaExtractor(mediaExtractor, metaData)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            mediaExtractor?.release()
        }
        return metaData
    }

    private fun fillDataFromMediaMetadataRetriever(
        retriever: MediaMetadataRetriever?,
        metaData: VideoMetaData
    ) {
        if (retriever == null) {
            return
        }
        // retriever not null
        metaData.duration = parseLong(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION),
            0
        )
        metaData.rotation = parseInteger(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION),
            0
        )
        metaData.width = parseInteger(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH),
            0
        )
        metaData.height = parseInteger(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT),
            0
        )
        metaData.bitRate = parseInteger(
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE),
            0
        )
    }

    private fun fillDataFromMediaExtractor(extractor: MediaExtractor?, metaData: VideoMetaData) {
        if (extractor == null) {
            return
        }
        val numTracks = extractor.trackCount
        var audioSize = 0.0
        var videoSize = 0.0
        var audioBitrate = 0.0
        var videoBitrate = 0.0
        var videoDuration = 0.0
        var audioDuration = 0.0
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
            var bitRate = 0
            var sampleRate = 0
            var channelCount = 0
            val duration: Long = 0
            if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                bitRate = getMediaFormatInt(format, MediaFormat.KEY_BIT_RATE)
            }
            if (format.containsKey(MediaFormat.KEY_SAMPLE_RATE)) {
                sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE)
            }
            if (format.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) {
                channelCount = format.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
            }
            LogUtils.d("Mime: $mime sampleRate: $sampleRate channelCount: $channelCount")

            if (mime.startsWith("audio/")) {
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    audioDuration = format.getLong(MediaFormat.KEY_DURATION) / SECOND
                }
                audioSize = bitRate * audioDuration / (8.0)
                audioBitrate = bitRate.toDouble()
                LogUtils.d("getVideoInfo audio size: " + audioSize + " format size: " + (audioSize * 1.0 / (1024L * 1024)) + "MB")
            }

            if (mime.startsWith("video/")) {
                if (metaData.frameRate <= 0 && format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                    val frameRate = format.getInteger(MediaFormat.KEY_FRAME_RATE)
                    if (frameRate > 0) {
                        metaData.frameRate = frameRate
                    }
                }
                if (format.containsKey(MediaFormat.KEY_DURATION)) {
                    videoDuration = format.getLong(MediaFormat.KEY_DURATION) / SECOND
                }
                if (metaData.width <= 0 && format.containsKey(MediaFormat.KEY_WIDTH)) {
                    val width = format.getInteger(MediaFormat.KEY_WIDTH)
                    if (width > 0) {
                        metaData.width = width
                    }
                }
                if (metaData.height <= 0 && format.containsKey(MediaFormat.KEY_HEIGHT)) {
                    val height = format.getInteger(MediaFormat.KEY_HEIGHT)
                    if (height > 0) {
                        metaData.height = height
                    }
                }
                if (metaData.getiFrameRate() <= 0 && format.containsKey(MediaFormat.KEY_I_FRAME_INTERVAL)) {
                    val iKeyRate = format.getFloat(MediaFormat.KEY_I_FRAME_INTERVAL)
                    if (iKeyRate > 0) {
                        metaData.setiFrameRate(iKeyRate)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (metaData.rotation <= 0 && format.containsKey(MediaFormat.KEY_ROTATION)) {
                        metaData.rotation =
                            format.getInteger(MediaFormat.KEY_ROTATION)
                    }
                }

                if (format.containsKey(MediaFormat.KEY_BIT_RATE)) {
                    bitRate = format.getInteger(MediaFormat.KEY_BIT_RATE)
                    if (metaData.bitRate <= 0 && bitRate > 0) {
                        metaData.bitRate = bitRate
                    }
                }

                if (metaData.colorFormat <= 0 && format.containsKey(MediaFormat.KEY_COLOR_FORMAT)) {
                    val colorFormat = format.getInteger(MediaFormat.KEY_COLOR_FORMAT)
                    if (colorFormat != 0) {
                        metaData.colorFormat = colorFormat
                    }
                }
                videoSize = bitRate * duration / SECOND
                LogUtils.d("getVideoInfo video size: $videoSize")
            }
        } /*end*/
        metaData.audioBitrate = audioBitrate
        val fileSize = metaData.size
        if (fileSize > 0 && videoDuration > 0) {
            val fileBitrate = (fileSize * 8.0 / videoDuration)
            LogUtils.d("getVideoInfo file bitrate: $fileBitrate/bps")
            videoBitrate = (fileSize - audioSize) * 8.0 / videoDuration
            metaData.videoBitrate = videoBitrate
            LogUtils.d("getVideoInfo video bitrate: $videoBitrate/bps")
        }
    }


    fun parseLong(string: String?, defaultValue: Long): Long {
        if (TextUtils.isEmpty(string)) {
            return defaultValue
        }
        try {
            return string!!.toLong()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return defaultValue
        }
    }

    fun parseInteger(string: String?, defaultValue: Int): Int {
        if (TextUtils.isEmpty(string)) {
            return defaultValue
        }
        try {
            return string!!.toInt()
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            return defaultValue
        }
    }

    private fun getMediaFormatInt(
        mediaFormat: MediaFormat?,
        key: String,
        defaultValue: Int = 0
    ): Int {
        try {
            return mediaFormat?.getInteger(key) ?: defaultValue
        } catch (e: Exception) {
            LogUtils.w("getMediaFormatInt error: ${e.message}")
        }
        return defaultValue
    }
}
