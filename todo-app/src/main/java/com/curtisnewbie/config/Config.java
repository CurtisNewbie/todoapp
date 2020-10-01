package com.curtisnewbie.config;

import java.io.Serializable;

/**
 * <p>
 * Config entity serialised/deserialized from JSON
 * </p>
 *
 * @author zhuangyongj
 */
public class Config implements Serializable {

    /**
     * Path to where the to-do list is saved on disk
     */
    private String savePath;

    public String getSavePath() {
        return savePath;
    }

    public void setSavePath(String savePath) {
        this.savePath = savePath;
    }

    @Override
    public String toString() {
        return "Config{" + "savePath='" + savePath + '\'' + '}';
    }
}
