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

    private static final SimpleDateFormat shortDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Convert date to yyyy-MM-dd string
     *
     * @param d date
     * @return date string in yyyy-MM-dd
     */
    public static String toDateStr(Date d) {
        return shortDateFormat.format(d);
    }
}
