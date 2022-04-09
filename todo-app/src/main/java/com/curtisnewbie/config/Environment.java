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
    private Language language;

    /**
     * Should finished task have strikethrough effect
     */
    private boolean strikethroughEffectEnabled;

    /**
     * Should search on typing
     */
    private boolean searchOnTypingEnabled;

    /**
     * Pattern used to export to-dos
     */
    private String pattern;

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
