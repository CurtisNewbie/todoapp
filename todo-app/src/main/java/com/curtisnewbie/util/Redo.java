package com.curtisnewbie.util;

import com.curtisnewbie.entity.TodoJob;

/**
 * <p>
 * Class that represents an action that can be redo
 * </p>
 *
 * @author yongjie.zhuang
 */
public final class Redo {

    private final RedoType type;
    private final TodoJob todoJob;

    public Redo(RedoType type, TodoJob job) {
        this.type = type;
        this.todoJob = job;
    }

    public RedoType getType() {
        return type;
    }

    public TodoJob getTodoJob() {
        return todoJob;
    }
}
