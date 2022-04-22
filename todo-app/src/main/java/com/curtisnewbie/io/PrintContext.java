package com.curtisnewbie.io;

import com.curtisnewbie.config.*;
import lombok.*;

/**
 * Print Context
 * <p>
 * Not thread-safe
 * </p>
 *
 * @author yongj.zhuang
 */
@Data
@Builder
public class PrintContext {

    private final Environment environment;

    private final boolean isNumbered;

    private int counter;

    public int getAndIncr() {
        return ++counter;
    }

}
