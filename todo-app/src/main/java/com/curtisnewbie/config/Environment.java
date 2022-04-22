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
     * Should the finished task have strikethrough effect
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

    /**
     * Copy the name of To-do only
     */
    @Getter
    private final boolean copyNameOnly;

    /**
     * Hide special tag
     */
    @Getter
    private final boolean specialTagHidden;

    public Environment(Config config) {
        this(Language.parseLang(config.getLanguage()),
                config.isStrikethroughEffectEnabled(),
                config.isSearchOnTypingEnabled(),
                config.isQuickTodoBarDisplayed(),
                config.getPattern(),
                config.getSuggestionsToggle(),
                config.isCopyNameOnly(),
                config.isSpecialTagHidden()
        );
    }

    public Environment setLanguage(Language language) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                _suggestionsToggle, copyNameOnly, specialTagHidden);
    }

    public Environment setStrikethroughEffectEnabled(boolean strikethroughEffectEnabled) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                _suggestionsToggle, copyNameOnly, specialTagHidden);
    }

    public Environment setSearchOnTypingEnabled(boolean searchOnTypingEnabled) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                _suggestionsToggle, copyNameOnly, specialTagHidden);
    }

    public Environment setPattern(String pattern) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                _suggestionsToggle, copyNameOnly, specialTagHidden);
    }

    public Environment setQuickTodoBarDisplayed(boolean quickTodoBarDisplayed) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                _suggestionsToggle, copyNameOnly, specialTagHidden);
    }

    public Environment setCopyNameOnly(boolean copyNameOnly) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                _suggestionsToggle, copyNameOnly, specialTagHidden);
    }

    public Environment setSpecialTagHidden(boolean specialTagHidden) {
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                _suggestionsToggle, copyNameOnly, specialTagHidden);
    }

    public Environment toggleSuggestionOff(SuggestionType key) {
        final Map<SuggestionType, Boolean> nst = new HashMap<>(_suggestionsToggle);
        nst.put(key, false);
        return new Environment(language, strikethroughEffectEnabled, searchOnTypingEnabled, quickTodoBarDisplayed, pattern,
                nst, copyNameOnly, specialTagHidden);
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
