package com.curtisnewbie.util;

/**
 * A Pair of object
 *
 * @author yongjie.zhuang
 */
public class Pair<T, V> {

    private final T t;
    private final V v;

    public Pair(T left, V right) {
        this.t = left;
        this.v = right;
    }

    public T getLeft() {
        return t;
    }

    public V getRight() {
        return v;
    }

}
