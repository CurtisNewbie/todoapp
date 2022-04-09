package com.curtisnewbie.util;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * String interpolation util
 *
 * @author yongjie.zhuang
 */
public final class StrInterpolationUtil {

    private static final Pattern substiPattern = Pattern.compile("([^$]*\\$\\{)([^}]*)(}[^$]*)");

    /**
     * Interpolate string
     */
    public static String interpolate(final String text, final Map<String, String> param) {
        return interpolate(text, parseSubstitution(text), param);
    }

    /**
     * Interpolate string
     */
    public static String interpolate(final String text, final Set<String> substitutions, final Map<String, String> param) {
        String interpolated = text;
        for (String sub : substitutions) {
            final String p = format("${%s}", sub);
            interpolated = interpolated.replace(p, param.get(sub));
        }
        return interpolated;
    }

    /**
     * Parse 'substitution'
     */
    public static Set<String> parseSubstitution(String text) {
        Set<String> sub = new HashSet<>();
        if (text == null)
            return sub;
        final Matcher m = substiPattern.matcher(text);
        while (m.find()) {
            String s = m.group(2);
            s = s != null ? s.trim() : null;
            if (s != null && !s.isEmpty())
                sub.add(s);
        }
        return sub;
    }
}
