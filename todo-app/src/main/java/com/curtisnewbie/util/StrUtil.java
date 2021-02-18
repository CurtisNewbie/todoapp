package com.curtisnewbie.util;

/**
 * <p>
 * Util class for Strings
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class StrUtil {

    private StrUtil() {
    }

    /**
     * Check whether one is empty, by which it means that a string is empty when it is: NULL, blank or of 0 length.
     *
     * @return TRUE if the string is NULL, blank or of 0 length, else FALSE
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
}
