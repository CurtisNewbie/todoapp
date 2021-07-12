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
    @Deprecated  // TODO: 21/03/2021 Not removing it for backward compatibility 
    private String savePath;

    /**
     * Language to use (optional for backward compatibility)
     */
    @JsonProperty(required = false, defaultValue = "eng")
    private String language;

    /**
     * Should finished task to have strikethrough effect
     */
    @JsonProperty(required = false, defaultValue = "false")
    private boolean strikethroughEffectEnabled;

    public Config() {

    }

    public Config(Environment environment) {
        this.savePath = environment.getSavePath();
        this.language = environment.getLanguage().key;
        this.strikethroughEffectEnabled = environment.isStrikethroughEffectEnabled();
    }

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

    public boolean getStrikethroughEffectEnabled() {
        return strikethroughEffectEnabled;
    }

    public void setStrikethroughEffectEnabled(boolean strikethroughEffectEnabled) {
        this.strikethroughEffectEnabled = strikethroughEffectEnabled;
    }

    @Override
    public String toString() {
        return "Config{" +
                "savePath='" + savePath + '\'' +
                ", language='" + language + '\'' +
                ", strikethroughEffectEnabled='" + strikethroughEffectEnabled + '\'' +
                '}';
    }
}
