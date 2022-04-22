package com.curtisnewbie.io;

import com.curtisnewbie.config.*;

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
     * @param t       object
     * @param pattern pattern
     * @param context context
     */
    String printObject(T t, String pattern, PrintContext context);
}
