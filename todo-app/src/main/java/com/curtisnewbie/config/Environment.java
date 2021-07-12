package com.curtisnewbie.config;

/**
 * Environment Configuration
 *
 * @author yongjie.zhuang
 */
public class Environment {

    /**
     * Path to where the to-do list is saved on disk
     */
    @Deprecated  // TODO: 21/03/2021 Not removing it for backward compatibility 
    private final String savePath;

    /**
     * Language to use
     */
    private Language language;

    /**
     * Should finished task have strikethrough effect
     */
    private boolean strikethroughEffectEnabled;

    public Environment(String savePath, Language language, boolean strikethroughEffectEnabled) {
        this.savePath = savePath;
        this.language = language;
        this.strikethroughEffectEnabled = strikethroughEffectEnabled;
    }

    public Environment(Config config) {
        this(config.getSavePath(), Language.parseLang(config.getLanguage()), config.getStrikethroughEffectEnabled());
    }

    public String getSavePath() {
        return savePath;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public boolean isStrikethroughEffectEnabled() {
        return strikethroughEffectEnabled;
    }

    public void setStrikethroughEffectEnabled(boolean strikethroughEffectEnabled) {
        this.strikethroughEffectEnabled = strikethroughEffectEnabled;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "savePath='" + savePath + '\'' +
                ", language=" + language +
                ", strikethroughEffectEnabled=" + strikethroughEffectEnabled +
                '}';
    }
}
