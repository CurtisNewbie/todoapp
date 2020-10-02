package com.curtisnewbie.util;

import java.io.UnsupportedEncodingException;

/**
 * <p>
 * Util class for Strings
 * </p>
 *
 * @author zhuangyongj
 */
public class StrUtil {
    private static final String UTF8 = "UTF-8";
    private static final String ISO8859 = "ISO-8859-1";

    private StrUtil() {

    }

    /**
     * Ensure proper support for languages that don't use utf-8 (e.g., chinese)
     *
     * @param str
     * @return
     */
    public static String correctEncoding(String str) {
        try {
            // to UTF-8
            byte[] rawBytes = str.getBytes(UTF8);
            // to ISO-8859-1
            String isoStr = new String(rawBytes, ISO8859);
            // back to utf
            byte[] utf8Bytes = isoStr.getBytes(ISO8859);
            return new String(utf8Bytes, UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            System.exit(1);
        }
        throw new RuntimeException("correctEncoding failed");
    }
}
