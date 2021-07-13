package com.curtisnewbie.io;

/**
 * An object as a printer of other object
 *
 * @param <T> object target object
 * @author yongjie.zhuang
 */
public interface ObjectPrinter<T> {

    /**
     * Print object
     *
     * @param t object
     */
    String printObject(T t);
}