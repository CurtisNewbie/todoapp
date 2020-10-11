package com.curtisnewbie.util;

/**
 * <p>
 * Class that allows registering callbacks
 * </p>
 *
 * @author yongjie.zhuang
 */
@FunctionalInterface
public interface OnClose {

    void close();
}
