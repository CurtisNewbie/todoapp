package com.curtisnewbie.util;

/**
 * Simple Time using {@link System#nanoTime()}
 *
 * @author yongjie.zhuang
 */
public final class CountdownTimer {

    private boolean isStarted = false;
    private boolean isStopped = false;
    private long startTime;
    private long endTime;

    public CountdownTimer() {
    }

    public void start() {
        if (isStarted)
            throw new IllegalStateException("Timer has started already");
        if (isStopped) {
            isStopped = false;
        }
        isStarted = true;
        startTime = System.nanoTime();
    }

    public void stop() {
        if (!isStarted)
            throw new IllegalStateException("Timer hasn't started yet");
        if (isStopped)
            throw new IllegalStateException("Timer is stopped already");
        isStopped = true;
        endTime = System.nanoTime();
    }

    public double getMilliSec() {
        if (!isStarted && !isStopped)
            throw new IllegalStateException("Timer is not started or not stopped");
        return (endTime - startTime) / 1_000_000;
    }


}
