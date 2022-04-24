package com.curtisnewbie.util;

import javafx.stage.*;

import java.util.*;

import static java.util.Collections.*;

/**
 * File Extension Util
 *
 * @author yongj.zhuang
 */
public final class FileExtUtil {

    /**
     * These objects are immutable, thus thread-safe
     */
    private static final FileChooser.ExtensionFilter textExtFilter = new FileChooser.ExtensionFilter("text", singletonList("*.txt"));
    private static final FileChooser.ExtensionFilter markdownExtFilter = new FileChooser.ExtensionFilter("markdown", singletonList("*.md"));

    private FileExtUtil() {

    }

    public static FileChooser.ExtensionFilter txtExtFilter() {
        return textExtFilter;
    }

    public static FileChooser.ExtensionFilter markdownExtFilter() {
        return markdownExtFilter;
    }

    public static FileChooser.ExtensionFilter jsonExtFilter() {
        return new FileChooser.ExtensionFilter("json", singletonList("*.json"));
    }
}
