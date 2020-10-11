package com.curtisnewbie.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

/**
 * <p>
 * Config entity serialised/deserialized from JSON
 * </p>
 *
 * @author yongjie.zhuang
 */
public class Config implements Serializable {

    /**
     * Path to where the to-do list is saved on disk
     */
    private String savePath;

    /**
     * Language to use (optional for backward compatibility)
     */
    @JsonProperty(required = false, defaultValue = "eng")
    private String language;

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "Config{" + "savePath='" + savePath + '\'' + '}';
    }
}
