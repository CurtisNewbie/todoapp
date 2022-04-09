package com.curtisnewbie.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.io.Serializable;

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
     * Should search on typing
     */
    @JsonProperty(required = false, defaultValue = "false")
    private boolean searchOnTypingEnabled;

    public Config() {

    }

    public Config(Environment environment) {
        this.language = environment.getLanguage().key;
        this.strikethroughEffectEnabled = environment.isStrikethroughEffectEnabled();
        this.searchOnTypingEnabled = environment.isSearchOnTypingEnabled();
        this.pattern = environment.getPattern();
    }

    public static Config getDefaultConfig() {
        Config c = new Config();
        c.setSearchOnTypingEnabled(false);
        c.setStrikethroughEffectEnabled(false);
        c.setLanguage(Language.DEFAULT.key);
        return c;
    }
}
