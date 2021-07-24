package com.curtisnewbie.util;

import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * A class that internally contains a number of "redo" action.
 * <p>
 * It's internally backed by a {@link java.util.concurrent.ConcurrentLinkedDeque} and thus it's thread-safe.
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class RedoStack {

    private final Deque<Redo> redoDeque = new ConcurrentLinkedDeque<>();

    /**
     * Add an redo (and remove earliest one if it's current size exceeds its desirable size)
     */
    public void push(Redo redo) {
        Objects.requireNonNull(redo);
        redoDeque.offerLast(redo);
    }

    /**
     * <p>
     * Get (as well as remove) an redo.
     * </p>
     *
     * @return Redo action (may be null)
     */
    public Redo pop() {
        return redoDeque.pollLast();
    }
}
