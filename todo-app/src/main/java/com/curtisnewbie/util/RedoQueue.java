package com.curtisnewbie.util;

import java.util.LinkedList;
import java.util.Queue;

/**
 * <p>
 * A class that internally contains a list of "redo" action. It internally contains a queue, and it's Thread-Safe.
 * </p>
 * <p>
 * The kind of redo action is typically represented by {@link RedoType}
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
    public synchronized void offer(Redo redo) {
        if (redoQueue.size() > MAX_SIZE)
            poll();
        redoQueue.offer(redo);
    }

    /**
     * <p>
     * Get (as well as remove) an redo.
     * </p>
     * <p>
     * If it's empty, it will simply return NULL to simplify some of the synchronisation issue. Thus, do not rely on
     * {@link #size()} to judge whether the queue is empty.
     * </p>
     *
     * @return Redo action
     */
    public synchronized Redo poll() {
        return redoQueue.poll();
    }

    /**
     * Return size (this is not synchronized, i.e., it's not reliable)
     *
     * @return size
     */
    public int size() {
        return redoQueue.size();
    }
}
