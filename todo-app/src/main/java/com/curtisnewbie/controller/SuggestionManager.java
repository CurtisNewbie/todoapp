package com.curtisnewbie.controller;

import java.util.*;

/**
 * Suggestion Manager
 * <p>
 * Thread-safe
 * </p>
 *
 * @author yongj.zhuang
 * @see SuggestionType
 */
public final class SuggestionManager {

    private Set<SuggestionType> suggestionSet = EnumSet.noneOf(SuggestionType.class);

    /**
     * Should we make suggestion ?
     */
    public synchronized boolean shouldSuggest(SuggestionType type) {
        return suggestionSet.add(type); // only suggests once
    }


}
