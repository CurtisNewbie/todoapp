package com.curtisnewbie.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

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

    Queue<Redo> redoQueue = new ConcurrentLinkedQueue<>();

    /**
     * Add an redo
     */
    public void put(Redo redo) {
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
    public Redo get() {
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
