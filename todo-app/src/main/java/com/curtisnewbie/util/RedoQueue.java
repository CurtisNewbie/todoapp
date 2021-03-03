package com.curtisnewbie.util;

import java.util.LinkedList;
import java.util.Queue;

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
public final class RedoQueue {

    /**
     * Desirable maximum size that this queue should respect, this is strictly followed.
     */
    private static final int MAX_SIZE = 50;

    private final Queue<Redo> redoQueue = new LinkedList<>();

    /**
     * Add an redo (and remove last one if it's current size exceeds its desirable size)
     */
    public void offer(Redo redo) {
        if (redoQueue.size() > MAX_SIZE)
            poll();
        redoQueue.offer(redo);
    }

    /**
     * <p>
     * Get (as well as remove) an redo.
     * </p>
     *
     * @return Redo action
     */
    public Redo poll() {
        return redoQueue.poll();
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
