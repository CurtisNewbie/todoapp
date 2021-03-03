package com.curtisnewbie.util;

import java.util.Deque;
import java.util.LinkedList;

/**
 * <p>
 * A class that internally contains a list of "redo" action. It's not thread-safe.
 * </p>
 * <p>
 * The kind of redo action is represented by {@link RedoType}
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class RedoStack {

    /**
     * Desirable maximum size that this queue should respect, this is strictly followed.
     */
    private static final int MAX_SIZE = 50;

    private final Deque<Redo> redoQueue = new LinkedList<>();

    /**
     * Add an redo (and remove earliest one if it's current size exceeds its desirable size)
     */
    public void push(Redo redo) {
        if (redoQueue.size() > MAX_SIZE)
            redoQueue.pollFirst();
        redoQueue.offerLast(redo);
    }

    /**
     * <p>
     * Get (as well as remove) an redo.
     * </p>
     *
     * @return Redo action
     */
    public Redo pop() {
        return redoQueue.pollLast();
    }

    /**
     * Return size
     *
     * @return size
     */
    public int size() {
        return redoQueue.size();
    }
}
