package com.curtisnewbie.config;

import lombok.Data;

/**
 * Environment Configuration
 *
 * @author yongjie.zhuang
 */
@Data
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

    /**
     * Should search on typing
     */
    private boolean searchOnTypingEnabled;

    public Environment(String savePath, Language language, boolean strikethroughEffectEnabled, boolean searchOnTypingEnabled) {
        this.savePath = savePath;
        this.language = language;
        this.strikethroughEffectEnabled = strikethroughEffectEnabled;
        this.searchOnTypingEnabled = searchOnTypingEnabled;
    }

    public Environment(Config config) {
        this(config.getSavePath(),
                Language.parseLang(config.getLanguage()),
                config.getStrikethroughEffectEnabled(),
                config.isSearchOnTypingEnabled());
    }
}
