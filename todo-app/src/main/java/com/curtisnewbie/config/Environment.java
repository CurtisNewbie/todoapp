package com.curtisnewbie.config;

import com.curtisnewbie.controller.SuggestionType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * Immutable Environment Configuration
 *
 * @author yongjie.zhuang
 */
@AllArgsConstructor
public class Environment {

    /**
     * Language to use
     */
    @Getter
    private final Language language;

    /**
     * Should finished task have strikethrough effect
     */
    @Getter
    private final boolean strikethroughEffectEnabled;

    /**
     * Should search on typing
     */
    @Getter
    private final boolean searchOnTypingEnabled;

    /**
     * quick to-do bar displayed by default
     */
    @Getter
    private final boolean quickTodoBarDisplayed;

    /**
     * Pattern used to export to-dos
     */
    @Getter
    private final String pattern;

    /**
     * Config map for suggestion
     */
    private final Map<SuggestionType, Boolean> _suggestionsToggle;

    public Environment(Config config) {
        this(Language.parseLang(config.getLanguage()),
                config.isStrikethroughEffectEnabled(),
                config.isSearchOnTypingEnabled(),
                config.isQuickTodoBarDisplayed(),
                config.getPattern(),
                config.getSuggestionsToggle()
        );
    }

    public Environment setLanguage(Language language) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern, _suggestionsToggle);
    }

    public Environment setStrikethroughEffectEnabled(boolean strikethroughEffectEnabled) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern, _suggestionsToggle);
    }

    public Environment setSearchOnTypingEnabled(boolean searchOnTypingEnabled) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern, _suggestionsToggle);
    }

    public Environment setPattern(String pattern) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern, _suggestionsToggle);
    }

    public Environment setQuickTodoBarDisplayed(boolean quickTodoBarDisplayed) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern, _suggestionsToggle);
    }

    public Environment toggleSuggestionOff(SuggestionType key) {
        final Map<SuggestionType, Boolean> nst = new HashMap<>(_suggestionsToggle);
        nst.put(key, false);
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern, nst);
    }

    public boolean isSuggestionToggleOn(SuggestionType key) {
        if (!_suggestionsToggle.containsKey(key))
            return true;

        return _suggestionsToggle.get(key);
    }

    public Map<SuggestionType, Boolean> _getSuggestionsToggle() {
        return new HashMap<>(_suggestionsToggle);
    }
}
