package com.curtisnewbie.exception;

/**
 * <p>
 * Indicate that the file cannot be loaded
 * </p>
 *
 * @author zhuangyongj
 */
public class FailureToLoadException extends Exception {

    public FailureToLoadException(Exception e) {
        super(e);
    }
}
