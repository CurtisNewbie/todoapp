package com.curtisnewbie.config;

import com.curtisnewbie.controller.SuggestionType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>
 * Config entity serialised/deserialized from JSON
 * </p>
 *
 * @author yongjie.zhuang
 */
@Data
public class Config implements Serializable {

    /**
     * Language to use (optional for backward compatibility)
     */
    @JsonProperty(required = false, defaultValue = "eng")
    private String language;

    /**
     * pattern used to export to-dos (optional for backward compatibility)
     */
    @JsonProperty(required = false, defaultValue = "")
    private String pattern;

    /**
     * Should finished task to have strikethrough effect
     */
    @JsonProperty(required = false, defaultValue = "false")
    private boolean strikethroughEffectEnabled;

    /**
     * quick to-do bar displayed by default
     */
    @JsonProperty(required = false, defaultValue = "false")
    private boolean quickTodoBarDisplayed;

    /**
     * Should search on typing
     */
    @JsonProperty(required = false, defaultValue = "false")
    private boolean searchOnTypingEnabled;

    /**
     * Copy the name of To-do only
     */
    @JsonProperty(required = false, defaultValue = "true")
    private boolean copyNameOnly;

    /**
     * Config map for suggestion
     */
    @JsonProperty(required = false)
    private Map<SuggestionType, Boolean> suggestionsToggle = new HashMap<>();

    /**
     * Hide special tag
     */
    @JsonProperty(required = false, defaultValue = "true")
    private boolean specialTagHidden;

    /**
     * Enable functionalities for the special tags
     */
    @JsonProperty(required = false, defaultValue = "true")
    private boolean specialTagEnabled;

    public Config() {

    }

    public Config(Environment environment) {
        this.language = environment.getLanguage().key;
        this.strikethroughEffectEnabled = environment.isStrikethroughEffectEnabled();
        this.searchOnTypingEnabled = environment.isSearchOnTypingEnabled();
        this.pattern = environment.getPattern();
        this.quickTodoBarDisplayed = environment.isQuickTodoBarDisplayed();
        this.suggestionsToggle = new HashMap<>(environment._getSuggestionsToggle());
        this.copyNameOnly = environment.isCopyNameOnly();
        this.specialTagHidden = environment.isSpecialTagHidden();
        this.specialTagEnabled = environment.isSpecialTagEnabled();
    }

    public static Config getDefaultConfig() {
        Config c = new Config();
        c.setSearchOnTypingEnabled(false);
        c.setStrikethroughEffectEnabled(false);
        c.setQuickTodoBarDisplayed(false);
        c.setLanguage(Language.DEFAULT.key);
        c.setCopyNameOnly(true);
        c.setSpecialTagHidden(true);
        c.setSpecialTagEnabled(true);
        return c;
    }
}
