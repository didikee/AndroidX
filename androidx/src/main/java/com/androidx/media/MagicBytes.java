package com.androidx.media;

import com.androidx.LogUtils;
import com.androidx.utils.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * user author: didikee
 * create time: 4/27/21 10:03 AM
 * description: magic num来自 https://www.garykessler.net/library/file_sigs.html
 */
public final class MagicBytes {
    private static final String WEB_SYMBOL = "x";
    private static final String REGEX_SYMBOL = ".";
    private static final String START_OFFSET_SYMBOL = "#";

    private static final Map<String, String> magicBytesMap = new HashMap<>();

    static {
        // image
        add("89 50 4E 47 0D 0A 1A 0A", "PNG");
        add("FF D8", "JPEG");
        add("52 49 46 46 xx xx xx xx 57 45 42 50", "WEBP");
        add("47 49 46 38 37 61", "GIF");
        add("47 49 46 38 39 61", "GIF");
        add("00 00 00 00 14 00 00 00", "TBI");
        add("00 00 00 0C 6A 50 20 20 0D 0A", "JP2");
        add("00 01 00 08 00 01 00 01 01", "IMG");
        add("38 42 50 53", "PSD");
        add("42 4D", "BMP");
        add("42 50 47 FB", "BPG");
        add("42 5A 68", "DMG");
        add("63 64 73 61 65 6E 63 72", "DMG");
        add("65 6E 63 72 63 64 73 61", "DMG");
        // video
        add("#4 66 74 79 70 4D 53 4E 56", "MP4");
        add("#4 66 74 79 70 69 73 6F 6D", "MP4");
        add("#4 66 74 79 70 71 74 20 20", "MOV");
        add("#4 66 74 79 70 4D 34 56 20", "M4V");
        add("#4 66 74 79 70 6D 70 34 32", "M4V");
        add("52 49 46 46 xx xx xx xx 41 56 49 20 4C 49 53 54", "AVI");
        add("1A 45 DF A3", "MKV");
        add("30 26 B2 75 8E 66 CF 11 A6 D9 00 AA 00 62 CE 6C", "WMV");
        add("46 4C 56 01", "FLV");
        add("45 4E 54 52 59 56 43 44 02 00 00 01 02 00 18 58", "VCD");
        // audio
        add("FF Ex", "MP3");
        add("FF Fx", "MP3");
        add("49 44 33", "MP3");
        add("FF F1", "AAC");
        add("FF F9", "AAC");
        add("#4 66 74 79 70 4D 34 41 20", "M4A");
        add("80 00", "ADX");
        add("52 49 46 46 xx xx xx xx 57 41 56 45 66 6D 74 20", "WAV");
        add("23 21 41 4D 52", "AMR");
        add("23 21 53 49 4C 4B 0A", "SIL");
        add("66 4C 61 43 00 00 00 22", "FLAC");
        // 其他常见的格式
        add("50 4B 03 04","ZIP");
        add("52 61 72 21 1A 07 00","RAR");
        add("52 61 72 21 1A 07 01 00","RAR");
        add("5F 27 A8 89","JAR");
        add("41 43","DWG");
        add("3C 3F 78 6D 6C 20 76 65 72 73 69 6F 6E 3D","XML");
        // 文档
        add("EC A5 C1 00","DOC");
        add("D0 CF 11 E0 A1 B1 1A E1","DOC");
        add("CF 11 E0 A1 B1 1A E1 00","DOC");
        add("25 50 44 46","PDF");
        add("7B 5C 72 74 66","RTF");

        add("46 72 6F 6D 20 20 20","EML");
        add("46 72 6F 6D 20 3F 3F 3F","EML");
        add("46 72 6F 6D 3A 20","EML");
        add("52 65 74 75 72 6E 2D 50 61 74 68 3A 20","EML");
        add("58 2D","EML");

    }


    private static void add(String magicBytesHex, String type) {
        magicBytesMap.put(magicBytesHex, type);
    }


    private static int getOffset(String text) {
        if (text.startsWith(START_OFFSET_SYMBOL) && text.length() > 1) {
            try {
                return Integer.parseInt(text.substring(1)) * 2;
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    public static String getType(byte[] bytes) {
        final String hexString = toHexString(bytes);
        LogUtils.d("hex: " + hexString.substring(0, Math.min(32, hexString.length())));
        String magicNum = "";
        int startOffset = 0;
        for (String key : magicBytesMap.keySet()) {
            magicNum = "";
            startOffset = 0;
            if (key.contains(WEB_SYMBOL)) {
                magicNum = key.replace(WEB_SYMBOL, REGEX_SYMBOL);
            } else {
                magicNum = key;
            }
            String[] split = magicNum.split(" ");
            startOffset = getOffset(split[0]);
            StringBuilder sbMagicNum = new StringBuilder();
            for (int i = startOffset > 0 ? 1 : 0; i < split.length; i++) {
                sbMagicNum.append(split[i]);
            }
            if (magicNum.contains(REGEX_SYMBOL) || magicNum.startsWith(START_OFFSET_SYMBOL)) {
                // 正则 ^[0-9A-F]{16}00..0000
                magicNum = sbMagicNum.toString();
                String regex;
                if (startOffset > 0) {
                    regex = "^[0-9A-F]{" + startOffset + "}" + magicNum;
                } else {
                    regex = "^" + magicNum;
                }
                LogUtils.d("regex: " + regex);
                LogUtils.d("magicNum: " + magicNum);
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(hexString);
                boolean find = matcher.find();
                if (find) {
                    return magicBytesMap.get(key);
                }
            } else {
                magicNum = sbMagicNum.toString();
                LogUtils.d("magicNum: " + magicNum);
                if (hexString.startsWith(magicNum)) {
                    return magicBytesMap.get(key);
                }
            }
        }
        return "";
    }

    public static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte b1 : b) {
            sb.append(toHexString(b1));
        }
        return sb.toString();
    }

    public static String toHexString(byte b) {
        int v = b & 0xFF;
        String hex = Integer.toHexString(v);
        if (hex.length() < 2) {
            hex = "0" + hex;
        }
        return hex.toUpperCase();
    }

    public static byte[] extract(InputStream is, int length) {
        try {
            byte[] buffer = new byte[length];
            int read = is.read(buffer, 0, length);
            return buffer;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            IOUtils.close(is);
        }
        return null;
    }

}
