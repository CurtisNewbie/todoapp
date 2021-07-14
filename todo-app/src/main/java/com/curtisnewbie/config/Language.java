package com.curtisnewbie.config;

import java.util.Locale;

/**
 * <p>
 * Languages-related constants
 * </p>
 *
 * @author yongjie.zhuang
 */
public enum Language {

    /** Chinese */
    CHN("chn", Locale.CHINESE),

    /** English */
    ENG("eng", Locale.ENGLISH),

    /** Default */
    DEFAULT("eng", Locale.ENGLISH);

    public final String key;
    public final Locale locale;

    Language(String key, Locale locale) {
        this.key = key;
        this.locale = locale;
    }

    public static Language parseLang(String langStr) {
        if (langStr == null)
            langStr = Language.DEFAULT.key;
        boolean isChn = langStr.equals(Language.CHN.key);
        Language lang;
        if (isChn) {
            lang = Language.CHN;
        } else {
            lang = Language.ENG;
        }
        return lang;
    }
}
