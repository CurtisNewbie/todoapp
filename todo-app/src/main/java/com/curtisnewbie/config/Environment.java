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
     * Language to use
     */
    private volatile Language language;

    /**
     * Should finished task have strikethrough effect
     */
    private volatile boolean strikethroughEffectEnabled;

    /**
     * Should search on typing
     */
    private volatile boolean searchOnTypingEnabled;

    /**
     * Pattern used to export to-dos
     */
    private volatile String pattern;

    public Environment(Language language, boolean strikethroughEffectEnabled, boolean searchOnTypingEnabled, String pattern) {
        this.language = language;
        this.strikethroughEffectEnabled = strikethroughEffectEnabled;
        this.searchOnTypingEnabled = searchOnTypingEnabled;
        this.pattern = pattern;
    }

    public Environment(Config config) {
        this(Language.parseLang(config.getLanguage()),
                config.isStrikethroughEffectEnabled(),
                config.isSearchOnTypingEnabled(),
                config.getPattern());
    }
}
