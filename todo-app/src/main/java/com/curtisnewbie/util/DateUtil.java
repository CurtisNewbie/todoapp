package com.curtisnewbie.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * <p>
 * Util for Date
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class DateUtil {

    private DateUtil() {
    }

    private static final ThreadLocal<SimpleDateFormat> shortDateFormatSlashThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/YYYY"));
    private static final ThreadLocal<DateTimeFormatter> shortLDateFormatSlashThreadLocal = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("dd/MM/uuuu"));
    private static final ThreadLocal<DateTimeFormatter> shortCurrTimeFormatThreadLocal = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("H:m:s"));
    private static final ThreadLocal<SimpleDateFormat> shortDateFormatDashThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MM-YYYY"));
    private static final ThreadLocal<SimpleDateFormat> longDateFormatDashThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MM-YYYY-HH:mm:ss"));

    /**
     * Convert date to yyyy/MM/dd string
     *
     * @param d date
     * @return date string in yyyy/MM/dd
     */
    public static String toDateStrSlash(Date d) {
        return shortDateFormatSlashThreadLocal.get().format(d);
    }

    /**
     * Convert date to yyyy/MM/dd string
     *
     * @param d date
     * @return date string in yyyy/MM/dd
     */
    public static String toDateStrSlash(LocalDate d) {
        return d.format(shortLDateFormatSlashThreadLocal.get());
    }

    /**
     * Convert date to yyyy-MM-dd string
     *
     * @param d date
     * @return date string in yyyy-MM-dd
     */
    public static String toDateStrDash(Date d) {
        return shortDateFormatDashThreadLocal.get().format(d);
    }

    /**
     * Convert date to yyyy-MM-dd-hh:MM:ss string
     *
     * @param d date
     * @return date string in yyyy-MM-dd-hh:MM:ss
     */
    public static String toLongDateStrDash(Date d) {
        return longDateFormatDashThreadLocal.get().format(d);
    }

    /**
     * Return a short string for current time
     *
     * @return
     */
    public static String getNowTimeShortStr() {
        return LocalDateTime.now().format(shortCurrTimeFormatThreadLocal.get());
    }


    /**
     * Get start time of the date
     */
    public static long startTimeOf(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
    }
}
