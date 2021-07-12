package com.curtisnewbie.config;

/**
 * <p>
 * Languages-related constants
 * </p>
 *
 * @author yongjie.zhuang
 */
public enum Language {

    /** Chinese */
    CHN("chn"),

    /** English */
    ENG("eng"),

    /** Default */
    DEFAULT("eng");

    public final String key;

    Language(String key) {
        this.key = key;
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
