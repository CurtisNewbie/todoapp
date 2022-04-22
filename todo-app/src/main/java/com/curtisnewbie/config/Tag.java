package com.curtisnewbie.config;

import lombok.*;

/**
 * Special Tags
 *
 * @author yongj.zhuang
 */
@Getter
public enum Tag {

    /** Exclude tag, content after the tag will be excluded */
    EXCL("</EXCL>");

    private final String value;
    private final int len;

    Tag(String value) {
        this.value = value;
        this.len = this.value.length();
    }

    /** Strip off text after the tag */
    public String strip(String c) {
        final int i = c.indexOf(value);
        if (i > 0)
            c = c.substring(0, i - 1);

        return c.trim();
    }

    /** Hide the tag, replace it with spaces */
    public String escape(String c) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++)
            sb.append(" ");
        return c.replace(value, sb.toString());
    }
}
