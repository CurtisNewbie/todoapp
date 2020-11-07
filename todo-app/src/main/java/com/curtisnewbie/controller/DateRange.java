package com.curtisnewbie.controller;

import java.time.LocalDate;

/**
 * Range of dates (Start and End)
 *
 * @author yongjie.zhuang
 */
public class DateRange {

    private final long start;
    private final long end;

    public DateRange(long start, long end) {
        this.start = start;
        this.end = end;
    }

    public long getStart() {
        return start;
    }

    public long getEnd() {
        return end;
    }
}
