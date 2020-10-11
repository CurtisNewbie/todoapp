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
}
