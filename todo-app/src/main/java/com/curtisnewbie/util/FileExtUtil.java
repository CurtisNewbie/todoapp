package com.curtisnewbie.util;

import javafx.stage.*;

import java.util.*;

/**
 * File Extension Util
 *
 * @author yongj.zhuang
 */
public final class FileExtUtil {

    private FileExtUtil() {

    }

    public static FileChooser.ExtensionFilter txtExtFilter() {
        return new FileChooser.ExtensionFilter("text", Arrays.asList("*.txt"));
    }

    public static FileChooser.ExtensionFilter markdownExtFilter() {
        return new FileChooser.ExtensionFilter("markdown", Arrays.asList("*.md"));
    }

    public static FileChooser.ExtensionFilter jsonExtFilter() {
        return new FileChooser.ExtensionFilter("json", Arrays.asList("*.json"));
    }
}
