package com.curtisnewbie.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Immutable Environment Configuration
 *
 * @author yongjie.zhuang
 */
@Getter
@AllArgsConstructor
public class Environment {

    /**
     * Language to use
     */
    private final Language language;

    /**
     * Should finished task have strikethrough effect
     */
    private final boolean strikethroughEffectEnabled;

    /**
     * Should search on typing
     */
    private final boolean searchOnTypingEnabled;

    /**
     * Pattern used to export to-dos
     */
    private final String pattern;

    public Environment(Config config) {
        this(Language.parseLang(config.getLanguage()),
                config.isStrikethroughEffectEnabled(),
                config.isSearchOnTypingEnabled(),
                config.getPattern());
    }

    public Environment setLanguage(Language language) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, pattern);
    }

    public Environment setStrikethroughEffectEnabled(boolean strikethroughEffectEnabled) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, pattern);
    }

    public Environment setSearchOnTypingEnabled(boolean searchOnTypingEnabled) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, pattern);
    }

    public Environment setPattern(String pattern) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, pattern);
    }
}
