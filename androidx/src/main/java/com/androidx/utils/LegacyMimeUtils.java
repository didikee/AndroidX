package com.androidx.utils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * user author: didikee
 * create time: 8/19/20 11:49 AM
 * description: 来自安卓9.0源码：http://androidxref.com/9.0.0_r3/xref/libcore/luni/src/main/java/libcore/net/MimeUtils.java
 */
public class MimeUtils {
    private static final Map<String, String> mimeTypeToExtensionMap = new HashMap<String, String>();
29
        30    private static final Map<String, String> extensionToMimeTypeMap = new HashMap<String, String>();
31
        32    static {
        33        // The following table is based on /etc/mime.types data minus
        34        // chemical/* MIME types and MIME types that don't map to any
        35        // file extensions. We also exclude top-level domain names to
        36        // deal with cases like:
        37        //
        38        // mail.google.com/a/google.com
        39        //
        40        // and "active" MIME types (due to potential security issues).
        41
        42        // Note that this list is _not_ in alphabetical order and must not be sorted.
        43        // The "most popular" extension must come first, so that it's the one returned
        44        // by guessExtensionFromMimeType.
        45
        46        add("application/andrew-inset", "ez");
        47        add("application/dsptype", "tsp");
        48        add("application/epub+zip", "epub");
        49        add("application/hta", "hta");
        50        add("application/mac-binhex40", "hqx");
        51        add("application/mathematica", "nb");
        52        add("application/msaccess", "mdb");
        53        add("application/oda", "oda");
        54        add("application/ogg", "ogx");
        55        add("application/pdf", "pdf");
        56        add("application/pgp-keys", "key");
        57        add("application/pgp-signature", "pgp");
        58        add("application/pics-rules", "prf");
        59        add("application/pkix-cert", "cer");
        60        add("application/rar", "rar");
        61        add("application/rdf+xml", "rdf");
        62        add("application/rss+xml", "rss");
        63        add("application/zip", "zip");
        64        add("application/vnd.android.package-archive", "apk");
        65        add("application/vnd.cinderella", "cdy");
        66        add("application/vnd.ms-pki.stl", "stl");
        67        add("application/vnd.oasis.opendocument.database", "odb");
        68        add("application/vnd.oasis.opendocument.formula", "odf");
        69        add("application/vnd.oasis.opendocument.graphics", "odg");
        70        add("application/vnd.oasis.opendocument.graphics-template", "otg");
        71        add("application/vnd.oasis.opendocument.image", "odi");
        72        add("application/vnd.oasis.opendocument.presentation", "odp");
        73        add("application/vnd.oasis.opendocument.presentation-template", "otp");
        74        add("application/vnd.oasis.opendocument.spreadsheet", "ods");
        75        add("application/vnd.oasis.opendocument.spreadsheet-template", "ots");
        76        add("application/vnd.oasis.opendocument.text", "odt");
        77        add("application/vnd.oasis.opendocument.text-master", "odm");
        78        add("application/vnd.oasis.opendocument.text-template", "ott");
        79        add("application/vnd.oasis.opendocument.text-web", "oth");
        80        add("application/vnd.google-earth.kml+xml", "kml");
        81        add("application/vnd.google-earth.kmz", "kmz");
        82        add("application/msword", "doc");
        83        add("application/msword", "dot");
        84        add("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx");
        85        add("application/vnd.openxmlformats-officedocument.wordprocessingml.template", "dotx");
        86        add("application/vnd.ms-excel", "xls");
        87        add("application/vnd.ms-excel", "xlt");
        88        add("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "xlsx");
        89        add("application/vnd.openxmlformats-officedocument.spreadsheetml.template", "xltx");
        90        add("application/vnd.ms-powerpoint", "ppt");
        91        add("application/vnd.ms-powerpoint", "pot");
        92        add("application/vnd.ms-powerpoint", "pps");
        93        add("application/vnd.openxmlformats-officedocument.presentationml.presentation", "pptx");
        94        add("application/vnd.openxmlformats-officedocument.presentationml.template", "potx");
        95        add("application/vnd.openxmlformats-officedocument.presentationml.slideshow", "ppsx");
        96        add("application/vnd.rim.cod", "cod");
        97        add("application/vnd.smaf", "mmf");
        98        add("application/vnd.stardivision.calc", "sdc");
        99        add("application/vnd.stardivision.draw", "sda");
        100        add("application/vnd.stardivision.impress", "sdd");
        101        add("application/vnd.stardivision.impress", "sdp");
        102        add("application/vnd.stardivision.math", "smf");
        103        add("application/vnd.stardivision.writer", "sdw");
        104        add("application/vnd.stardivision.writer", "vor");
        105        add("application/vnd.stardivision.writer-global", "sgl");
        106        add("application/vnd.sun.xml.calc", "sxc");
        107        add("application/vnd.sun.xml.calc.template", "stc");
        108        add("application/vnd.sun.xml.draw", "sxd");
        109        add("application/vnd.sun.xml.draw.template", "std");
        110        add("application/vnd.sun.xml.impress", "sxi");
        111        add("application/vnd.sun.xml.impress.template", "sti");
        112        add("application/vnd.sun.xml.math", "sxm");
        113        add("application/vnd.sun.xml.writer", "sxw");
        114        add("application/vnd.sun.xml.writer.global", "sxg");
        115        add("application/vnd.sun.xml.writer.template", "stw");
        116        add("application/vnd.visio", "vsd");
        117        add("application/vnd.youtube.yt", "yt");
        118        add("application/x-abiword", "abw");
        119        add("application/x-apple-diskimage", "dmg");
        120        add("application/x-bcpio", "bcpio");
        121        add("application/x-bittorrent", "torrent");
        122        add("application/x-cdf", "cdf");
        123        add("application/x-cdlink", "vcd");
        124        add("application/x-chess-pgn", "pgn");
        125        add("application/x-cpio", "cpio");
        126        add("application/x-debian-package", "deb");
        127        add("application/x-debian-package", "udeb");
        128        add("application/x-director", "dcr");
        129        add("application/x-director", "dir");
        130        add("application/x-director", "dxr");
        131        add("application/x-dms", "dms");
        132        add("application/x-doom", "wad");
        133        add("application/x-dvi", "dvi");
        134        add("application/x-font", "pfa");
        135        add("application/x-font", "pfb");
        136        add("application/x-font", "gsf");
        137        add("application/x-font", "pcf");
        138        add("application/x-font", "pcf.Z");
        139        add("application/x-freemind", "mm");
        140        // application/futuresplash isn't IANA, so application/x-futuresplash should come first.
        141        add("application/x-futuresplash", "spl");
        142        add("application/futuresplash", "spl");
        143        add("application/x-gnumeric", "gnumeric");
        144        add("application/x-go-sgf", "sgf");
        145        add("application/x-graphing-calculator", "gcf");
        146        add("application/x-gtar", "tgz");
        147        add("application/x-gtar", "gtar");
        148        add("application/x-gtar", "taz");
        149        add("application/x-hdf", "hdf");
        150        add("application/x-hwp", "hwp"); // http://b/18788282.
        151        add("application/x-ica", "ica");
        152        add("application/x-internet-signup", "ins");
        153        add("application/x-internet-signup", "isp");
        154        add("application/x-iphone", "iii");
        155        add("application/x-iso9660-image", "iso");
        156        add("application/x-jmol", "jmz");
        157        add("application/x-kchart", "chrt");
        158        add("application/x-killustrator", "kil");
        159        add("application/x-koan", "skp");
        160        add("application/x-koan", "skd");
        161        add("application/x-koan", "skt");
        162        add("application/x-koan", "skm");
        163        add("application/x-kpresenter", "kpr");
        164        add("application/x-kpresenter", "kpt");
        165        add("application/x-kspread", "ksp");
        166        add("application/x-kword", "kwd");
        167        add("application/x-kword", "kwt");
        168        add("application/x-latex", "latex");
        169        add("application/x-lha", "lha");
        170        add("application/x-lzh", "lzh");
        171        add("application/x-lzx", "lzx");
        172        add("application/x-maker", "frm");
        173        add("application/x-maker", "maker");
        174        add("application/x-maker", "frame");
        175        add("application/x-maker", "fb");
        176        add("application/x-maker", "book");
        177        add("application/x-maker", "fbdoc");
        178        add("application/x-mif", "mif");
        179        add("application/x-ms-wmd", "wmd");
        180        add("application/x-ms-wmz", "wmz");
        181        add("application/x-msi", "msi");
        182        add("application/x-ns-proxy-autoconfig", "pac");
        183        add("application/x-nwc", "nwc");
        184        add("application/x-object", "o");
        185        add("application/x-oz-application", "oza");
        186        add("application/x-pem-file", "pem");
        187        add("application/x-pkcs12", "p12");
        188        add("application/x-pkcs12", "pfx");
        189        add("application/x-pkcs7-certreqresp", "p7r");
        190        add("application/x-pkcs7-crl", "crl");
        191        add("application/x-quicktimeplayer", "qtl");
        192        add("application/x-shar", "shar");
        193        add("application/x-shockwave-flash", "swf");
        194        add("application/x-stuffit", "sit");
        195        add("application/x-sv4cpio", "sv4cpio");
        196        add("application/x-sv4crc", "sv4crc");
        197        add("application/x-tar", "tar");
        198        add("application/x-texinfo", "texinfo");
        199        add("application/x-texinfo", "texi");
        200        add("application/x-troff", "t");
        201        add("application/x-troff", "roff");
        202        add("application/x-troff-man", "man");
        203        add("application/x-ustar", "ustar");
        204        add("application/x-wais-source", "src");
        205        add("application/x-wingz", "wz");
        206        add("application/x-webarchive", "webarchive");
        207        add("application/x-webarchive-xml", "webarchivexml");
        208        add("application/x-x509-ca-cert", "crt");
        209        add("application/x-x509-user-cert", "crt");
        210        add("application/x-x509-server-cert", "crt");
        211        add("application/x-xcf", "xcf");
        212        add("application/x-xfig", "fig");
        213        add("application/xhtml+xml", "xhtml");
        214        // Video mime types for 3GPP first so they'll be default for guessMimeTypeFromExtension
        215        // See RFC 3839 for 3GPP and RFC 4393 for 3GPP2
        216        add("video/3gpp", "3gpp");
        217        add("video/3gpp", "3gp");
        218        add("video/3gpp2", "3gpp2");
        219        add("video/3gpp2", "3g2");
        220        add("audio/3gpp", "3gpp");
        221        add("audio/aac", "aac");
        222        add("audio/aac-adts", "aac");
        223        add("audio/amr", "amr");
        224        add("audio/amr-wb", "awb");
        225        add("audio/basic", "snd");
        226        add("audio/flac", "flac");
        227        add("application/x-flac", "flac");
        228        add("audio/imelody", "imy");
        229        add("audio/midi", "mid");
        230        add("audio/midi", "midi");
        231        add("audio/midi", "ota");
        232        add("audio/midi", "kar");
        233        add("audio/midi", "rtttl");
        234        add("audio/midi", "xmf");
        235        add("audio/mobile-xmf", "mxmf");
        236        // add ".mp3" first so it will be the default for guessExtensionFromMimeType
        237        add("audio/mpeg", "mp3");
        238        add("audio/mpeg", "mpga");
        239        add("audio/mpeg", "mpega");
        240        add("audio/mpeg", "mp2");
        241        add("audio/mpeg", "m4a");
        242        add("audio/mpegurl", "m3u");
        243        add("audio/ogg", "oga");
        244        add("audio/ogg", "ogg");
        245        add("audio/ogg", "spx");
        246        add("audio/prs.sid", "sid");
        247        add("audio/x-aiff", "aif");
        248        add("audio/x-aiff", "aiff");
        249        add("audio/x-aiff", "aifc");
        250        add("audio/x-gsm", "gsm");
        251        add("audio/x-matroska", "mka");
        252        add("audio/x-mpegurl", "m3u");
        253        add("audio/x-ms-wma", "wma");
        254        add("audio/x-ms-wax", "wax");
        255        add("audio/x-pn-realaudio", "ra");
        256        add("audio/x-pn-realaudio", "rm");
        257        add("audio/x-pn-realaudio", "ram");
        258        add("audio/x-realaudio", "ra");
        259        add("audio/x-scpls", "pls");
        260        add("audio/x-sd2", "sd2");
        261        add("audio/x-wav", "wav");
        262        // image/bmp isn't IANA, so image/x-ms-bmp should come first.
        263        add("image/x-ms-bmp", "bmp");
        264        add("image/bmp", "bmp");
        265        add("image/gif", "gif");
        266        // image/ico isn't IANA, so image/x-icon should come first.
        267        add("image/x-icon", "ico");
        268        add("image/ico", "cur");
        269        add("image/ico", "ico");
        270        add("image/ief", "ief");
        271        // add ".jpg" first so it will be the default for guessExtensionFromMimeType
        272        add("image/jpeg", "jpg");
        273        add("image/jpeg", "jpeg");
        274        add("image/jpeg", "jpe");
        275        add("image/pcx", "pcx");
        276        add("image/png", "png");
        277        add("image/svg+xml", "svg");
        278        add("image/svg+xml", "svgz");
        279        add("image/tiff", "tiff");
        280        add("image/tiff", "tif");
        281        add("image/vnd.djvu", "djvu");
        282        add("image/vnd.djvu", "djv");
        283        add("image/vnd.wap.wbmp", "wbmp");
        284        add("image/webp", "webp");
        285        add("image/x-adobe-dng", "dng");
        286        add("image/x-canon-cr2", "cr2");
        287        add("image/x-cmu-raster", "ras");
        288        add("image/x-coreldraw", "cdr");
        289        add("image/x-coreldrawpattern", "pat");
        290        add("image/x-coreldrawtemplate", "cdt");
        291        add("image/x-corelphotopaint", "cpt");
        292        add("image/x-fuji-raf", "raf");
        293        add("image/x-jg", "art");
        294        add("image/x-jng", "jng");
        295        add("image/x-nikon-nef", "nef");
        296        add("image/x-nikon-nrw", "nrw");
        297        add("image/x-olympus-orf", "orf");
        298        add("image/x-panasonic-rw2", "rw2");
        299        add("image/x-pentax-pef", "pef");
        300        add("image/x-photoshop", "psd");
        301        add("image/x-portable-anymap", "pnm");
        302        add("image/x-portable-bitmap", "pbm");
        303        add("image/x-portable-graymap", "pgm");
        304        add("image/x-portable-pixmap", "ppm");
        305        add("image/x-samsung-srw", "srw");
        306        add("image/x-sony-arw", "arw");
        307        add("image/x-rgb", "rgb");
        308        add("image/x-xbitmap", "xbm");
        309        add("image/x-xpixmap", "xpm");
        310        add("image/x-xwindowdump", "xwd");
        311        add("model/iges", "igs");
        312        add("model/iges", "iges");
        313        add("model/mesh", "msh");
        314        add("model/mesh", "mesh");
        315        add("model/mesh", "silo");
        316        add("text/calendar", "ics");
        317        add("text/calendar", "icz");
        318        add("text/comma-separated-values", "csv");
        319        add("text/css", "css");
        320        add("text/html", "htm");
        321        add("text/html", "html");
        322        add("text/h323", "323");
        323        add("text/iuls", "uls");
        324        add("text/mathml", "mml");
        325        // add ".txt" first so it will be the default for guessExtensionFromMimeType
        326        add("text/plain", "txt");
        327        add("text/plain", "asc");
        328        add("text/plain", "text");
        329        add("text/plain", "diff");
        330        add("text/plain", "po");     // reserve "pot" for vnd.ms-powerpoint
        331        add("text/richtext", "rtx");
        332        add("text/rtf", "rtf");
        333        add("text/text", "phps");
        334        add("text/tab-separated-values", "tsv");
        335        add("text/xml", "xml");
        336        add("text/x-bibtex", "bib");
        337        add("text/x-boo", "boo");
        338        add("text/x-c++hdr", "hpp");
        339        add("text/x-c++hdr", "h++");
        340        add("text/x-c++hdr", "hxx");
        341        add("text/x-c++hdr", "hh");
        342        add("text/x-c++src", "cpp");
        343        add("text/x-c++src", "c++");
        344        add("text/x-c++src", "cc");
        345        add("text/x-c++src", "cxx");
        346        add("text/x-chdr", "h");
        347        add("text/x-component", "htc");
        348        add("text/x-csh", "csh");
        349        add("text/x-csrc", "c");
        350        add("text/x-dsrc", "d");
        351        add("text/x-haskell", "hs");
        352        add("text/x-java", "java");
        353        add("text/x-literate-haskell", "lhs");
        354        add("text/x-moc", "moc");
        355        add("text/x-pascal", "p");
        356        add("text/x-pascal", "pas");
        357        add("text/x-pcs-gcd", "gcd");
        358        add("text/x-setext", "etx");
        359        add("text/x-tcl", "tcl");
        360        add("text/x-tex", "tex");
        361        add("text/x-tex", "ltx");
        362        add("text/x-tex", "sty");
        363        add("text/x-tex", "cls");
        364        add("text/x-vcalendar", "vcs");
        365        add("text/x-vcard", "vcf");
        366        add("video/avi", "avi");
        367        add("video/dl", "dl");
        368        add("video/dv", "dif");
        369        add("video/dv", "dv");
        370        add("video/fli", "fli");
        371        add("video/m4v", "m4v");
        372        add("video/mp2ts", "ts");
        373        add("video/mpeg", "mpeg");
        374        add("video/mpeg", "mpg");
        375        add("video/mpeg", "mpe");
        376        add("video/mp4", "mp4");
        377        add("video/mpeg", "VOB");
        378        add("video/ogg", "ogv");
        379        add("video/quicktime", "qt");
        380        add("video/quicktime", "mov");
        381        add("video/vnd.mpegurl", "mxu");
        382        add("video/webm", "webm");
        383        add("video/x-la-asf", "lsf");
        384        add("video/x-la-asf", "lsx");
        385        add("video/x-matroska", "mkv");
        386        add("video/x-mng", "mng");
        387        add("video/x-ms-asf", "asf");
        388        add("video/x-ms-asf", "asx");
        389        add("video/x-ms-wm", "wm");
        390        add("video/x-ms-wmv", "wmv");
        391        add("video/x-ms-wmx", "wmx");
        392        add("video/x-ms-wvx", "wvx");
        393        add("video/x-sgi-movie", "movie");
        394        add("video/x-webex", "wrf");
        395        add("x-conference/x-cooltalk", "ice");
        396        add("x-epoc/x-sisx-app", "sisx");
        397    }
398
        399    private static void add(String mimeType, String extension) {
        400        // If we have an existing x -> y mapping, we do not want to
        401        // override it with another mapping x -> y2.
        402        // If a mime type maps to several extensions
        403        // the first extension added is considered the most popular
        404        // so we do not want to overwrite it later.
        405        if (!mimeTypeToExtensionMap.containsKey(mimeType)) {
            406            mimeTypeToExtensionMap.put(mimeType, extension);
            407        }
        408        if (!extensionToMimeTypeMap.containsKey(extension)) {
            409            extensionToMimeTypeMap.put(extension, mimeType);
            410        }
        411    }
412
        413    private MimeUtils() {
        414    }
415
        416    /**
 417     * Returns true if the given case insensitive MIME type has an entry in the map.
 418     * @param mimeType A MIME type (i.e. text/plain)
 419     * @return True if a extension has been registered for
 420     * the given case insensitive MIME type.
 421     */
        422    public static boolean hasMimeType(String mimeType) {
        423        return (guessExtensionFromMimeType(mimeType) != null);
        424    }
425
        426    /**
 427     * Returns the MIME type for the given case insensitive file extension.
 428     * @param extension A file extension without the leading '.'
 429     * @return The MIME type has been registered for
 430     * the given case insensitive file extension or null if there is none.
 431     */
        432    public static String guessMimeTypeFromExtension(String extension) {
        433        if (extension == null || extension.isEmpty()) {
            434            return null;
            435        }
        436        extension = extension.toLowerCase(Locale.US);
        437        return extensionToMimeTypeMap.get(extension);
        438    }
439
        440    /**
 441     * Returns true if the given case insensitive extension has a registered MIME type.
 442     * @param extension A file extension without the leading '.'
 443     * @return True if a MIME type has been registered for
 444     * the given case insensitive file extension.
 445     */
        446    public static boolean hasExtension(String extension) {
        447        return (guessMimeTypeFromExtension(extension) != null);
        448    }
449
        450    /**
 451     * Returns the registered extension for the given case insensitive MIME type. Note that some
 452     * MIME types map to multiple extensions. This call will return the most
 453     * common extension for the given MIME type.
 454     * @param mimeType A MIME type (i.e. text/plain)
 455     * @return The extension has been registered for
 456     * the given case insensitive MIME type or null if there is none.
 457     */
        458    public static String guessExtensionFromMimeType(String mimeType) {
        459        if (mimeType == null || mimeType.isEmpty()) {
            460            return null;
            461        }
        462        mimeType = mimeType.toLowerCase(Locale.US);
        463        return mimeTypeToExtensionMap.get(mimeType);
        464    }
}
