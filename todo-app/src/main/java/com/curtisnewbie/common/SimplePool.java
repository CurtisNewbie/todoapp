package com.curtisnewbie.common;

import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Supplier;

/**
 * Simple Object Pool
 *
 * @author yongj.zhuang
 */
@Slf4j
public class SimplePool<T> {

    private int peek = 0;
    private final String name;
    private final Queue<T> pool = new LinkedList<>();
    private final Supplier<T> newT;

    public SimplePool(String name, int initialCapacity, Supplier<T> newT) {
        this.name = name;
        this.newT = newT;
        if (initialCapacity < 1) initialCapacity = 0;
        while (initialCapacity-- > 0) {
            pool.add(newT.get());
            ++peek;
        }
    }

    public SimplePool(int initialCapacity, Supplier<T> newT) {
        this("", initialCapacity, newT);
    }

    public T borrowT() {
        T t = null;
        synchronized (this) {
            t = pool.poll();
            if (log.isDebugEnabled())
                log.debug("{} - Pool Size: {}, peek: {}", name, pool.size(), peek);
        }
        if (t == null) t = newT.get();

        return t;
    }

    public void returnT(T t) {
        if (t instanceof Cleanable) {
            ((Cleanable) t).clean();
        }
        synchronized (this) {
            pool.add(t);
            if (peek < pool.size()) peek = pool.size();
            if (log.isDebugEnabled())
                log.debug("{} - Pool Size: {}, peek: {}", name, pool.size(), peek);
        }
    }
}
