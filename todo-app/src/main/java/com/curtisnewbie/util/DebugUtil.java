package com.curtisnewbie.util;

import java.util.Properties;

/**
 * @author yongj.zhuang
 */
public final class DebugUtil {

    private static final String DEBUG = "debug";

    /**
     * This tool is only used for debugging, so this will always be false
     */
    public static final boolean isDebug;

    static {
        // java -Ddebug=true -jar ....
        final Properties properties = System.getProperties();
        isDebug = properties.containsKey(DEBUG) && Boolean.parseBoolean(properties.get(DEBUG).toString());
    }
}
