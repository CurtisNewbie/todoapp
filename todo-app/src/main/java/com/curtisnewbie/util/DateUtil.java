package com.curtisnewbie.util;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * <p>
 * Util for Date / time
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class DateUtil {

    private static final ThreadLocal<SimpleDateFormat> DDMMYYYY_SLASH_THREADLOCAL = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd/MM/YYYY"));
    private static final ThreadLocal<DateTimeFormatter> DDMMUUUU_SLASH_THREADLOCAL = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("dd/MM/uuuu"));
    private static final ThreadLocal<DateTimeFormatter> MMDDUUUU_SLASH_THREADLOCAL = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("MM/dd/uuuu"));
    private static final ThreadLocal<DateTimeFormatter> HMS_THREADLOCAL = ThreadLocal.withInitial(() -> DateTimeFormatter.ofPattern("H:m:s"));
    private static final ThreadLocal<SimpleDateFormat> DDMMYYYY_DASH_THREADLOCAL = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MM-YYYY"));
    private static final ThreadLocal<SimpleDateFormat> LONG_DATE_DASH_THREADLOCAL = ThreadLocal.withInitial(() -> new SimpleDateFormat("dd-MM-YYYY-HH:mm:ss"));

    private DateUtil() {
    }

    /**
     * Convert date to dd/MM/YYYY string
     *
     * @param d date
     * @return date string in dd/MM/YYYY
     */
    public static String toDDmmYYYYSlash(Date d) {
        return DDMMYYYY_SLASH_THREADLOCAL.get().format(d);
    }

    /**
     * Convert date to dd/MM/uuuu string
     *
     * @param d date
     * @return date string in dd/MM/uuuu
     */
    public static String toDDmmUUUUSlash(LocalDate d) {
        return d.format(DDMMUUUU_SLASH_THREADLOCAL.get());
    }

    /**
     * Convert date to MM/dd/uuuu string
     *
     * @param d date
     * @return date string in MM/dd/uuuu
     */
    public static String toMMddUUUUSlash(LocalDate d) {
        return d.format(MMDDUUUU_SLASH_THREADLOCAL.get());
    }

    /**
     * Convert date to dd-MM-YYYY string
     *
     * @param d date
     * @return date string in dd-MM-YYYY
     */
    public static String toYYYYmmDDDash(Date d) {
        return DDMMYYYY_DASH_THREADLOCAL.get().format(d);
    }

    /**
     * Convert date to yyyy-MM-dd-hh:MM:ss string
     *
     * @param d date
     * @return date string in yyyy-MM-dd-hh:MM:ss
     */
    public static String toLongDateStrDash(Date d) {
        return LONG_DATE_DASH_THREADLOCAL.get().format(d);
    }

    /**
     * Return a short string for current time (H:m:s)
     *
     * @return
     */
    public static String getNowTimeShortStr() {
        return LocalDateTime.now().format(HMS_THREADLOCAL.get());
    }

    /**
     * Get start time of the date
     */
    public static long startTimeOf(LocalDate ld) {
        return Date.from(ld.atStartOfDay(ZoneId.systemDefault()).toInstant()).getTime();
    }

    /**
     * Convert Date to LocalDate
     */
    public static LocalDate localDateOf(Date date) {
        if (date == null)
            return null;
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }
}
