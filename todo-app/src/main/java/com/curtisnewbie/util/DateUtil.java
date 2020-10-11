package com.curtisnewbie.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * <p>
 * Util for Date
 * </p>
 *
 * @author yongjie.zhuang
 */
public class DateUtil {

    private DateUtil() {
    }

    private static final SimpleDateFormat shortDateFormatSlash = new SimpleDateFormat("dd/MM/YYYY");
    private static final SimpleDateFormat shortDateFormatDash = new SimpleDateFormat("dd-MM-YYYY");

    /**
     * Convert date to yyyy/MM/dd string
     *
     * @param d date
     * @return date string in yyyy/MM/dd
     */
    public static String toDateStrSlash(Date d) {
        return shortDateFormatSlash.format(d);
    }

    /**
     * Convert date to yyyy-MM-dd string
     *
     * @param d date
     * @return date string in yyyy-MM-dd
     */
    public static String toDateStrDash(Date d) {
        return shortDateFormatDash.format(d);
    }
}
