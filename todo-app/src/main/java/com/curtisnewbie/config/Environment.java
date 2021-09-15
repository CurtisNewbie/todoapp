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

    public Environment(Language language, boolean strikethroughEffectEnabled, boolean searchOnTypingEnabled) {
        this.language = language;
        this.strikethroughEffectEnabled = strikethroughEffectEnabled;
        this.searchOnTypingEnabled = searchOnTypingEnabled;
    }

    public Environment(Config config) {
        this(Language.parseLang(config.getLanguage()),
                config.isStrikethroughEffectEnabled(),
                config.isSearchOnTypingEnabled());
    }
}
