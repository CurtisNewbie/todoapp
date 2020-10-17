package com.curtisnewbie.callback;

/**
 * <p>
 * Class that allows registering callbacks to react to application closure
 * </p>
 *
 * @author yongjie.zhuang
 */
@FunctionalInterface
public interface OnClose {

    void close();
}
