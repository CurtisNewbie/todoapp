package com.curtisnewbie.io;

import com.curtisnewbie.config.*;
import com.curtisnewbie.dao.TodoJob;
import com.curtisnewbie.util.StrInterpolationUtil;
import com.curtisnewbie.util.StrUtil;

import java.util.HashMap;
import java.util.Map;

import static com.curtisnewbie.config.PropertyConstants.*;
import static com.curtisnewbie.util.DateUtil.toDDmmUUUUSlash;
import static com.curtisnewbie.util.StrInterpolationUtil.*;

/**
 * ObjectPrinter for {@link TodoJob}
 * </br>
 * Supports the following arguments (used in pattern)
 * <ul>
 *   <li>expectedEndDate</li>
 *   <li>actualEndDate</li>
 *   <li>status</li>
 *   <li>content</li>
 * </ul>
 *
 * @author yongjie.zhuang
 */
public class TodoJobObjectPrinter implements ObjectPrinter<TodoJob> {

    public static String EXPECTED_END_DATE_KEY = "expectedEndDate";
    public static String ACTUAL_END_DATE_KEY = "actualEndDate";
    public static String STATUS_KEY = "status";
    public static String CONTENT_KEY = "content";

    private static final int ENG_WIDTH = 13;
    private static final int CN_WIDTH = 5;

    @Override
    public String printObject(TodoJob todoJob, String pattern, PrintContext context) {
        final Environment environment = context.getEnvironment();
        final PropertiesLoader propertiesLoader = PropertiesLoader.getInstance();
        final String status = todoJob.isDone() ? propertiesLoader.getLocalizedProperty(TEXT_DONE_KEY) : propertiesLoader.getLocalizedProperty(TEXT_IN_PROGRESS_KEY);
        final String expectedEndDate = toDDmmUUUUSlash(todoJob.getExpectedEndDate());
        final String actualEndDate = todoJob.getActualEndDate() != null ? toDDmmUUUUSlash(todoJob.getActualEndDate()) : "__/__/____";
        final String content = formatContent(todoJob.getName(), environment.isSpecialTagEnabled());

        // no pattern specified, use the default one
        if (pattern == null || StrUtil.isEmpty(pattern)) {
            return prefixNumberIfNeeded(context, defaultPattern(status, actualEndDate, expectedEndDate, content, environment));
        }

        // do string interpolation based on the given pattern
        Map<String, String> params = new HashMap<>();
        params.put(EXPECTED_END_DATE_KEY, expectedEndDate);
        params.put(ACTUAL_END_DATE_KEY, actualEndDate);
        params.put(STATUS_KEY, status);
        params.put(CONTENT_KEY, content);
        return prefixNumberIfNeeded(context, interpolate(pattern, params));
    }

    private static String prefixNumberIfNeeded(PrintContext context, String content) {
        if (!context.isNumbered())
            return content;

        return String.format("%s. %s", context.getAndIncr(), content);
    }

    private static String formatContent(String content, boolean isTagStripped) {
        if (isTagStripped)
            content = Tag.EXCL.strip(content);
        return content.replaceAll("\\n", "\n  ");
    }

    /** default formatting */
    private String defaultPattern(String status, String actualEndDate, String expectedEndDate, String content, Environment environment) {
        final PropertiesLoader propertiesLoader = PropertiesLoader.getInstance();
        final String expectedText = propertiesLoader.getLocalizedProperty(TEXT_EXPECTED_END_DATE_KEY);
        final String actualText = propertiesLoader.getLocalizedProperty(TEXT_ACTUAL_END_DATE_KEY);
        int width = environment.getLanguage().equals(Language.ENG) ? ENG_WIDTH : CN_WIDTH;
        return String.format("%-" + width + "s %s: %s - %s: %s \n\t%s",
                "[" + status + "]",
                expectedText,
                expectedEndDate,
                actualText,
                actualEndDate,
                content);
    }
}
