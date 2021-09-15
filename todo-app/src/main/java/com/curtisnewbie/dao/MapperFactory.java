package com.curtisnewbie.dao;

import reactor.core.publisher.Mono;

/**
 * Abstract factory of mapper
 *
 * @author yongjie.zhuang
 */
public interface MapperFactory {

    /**
     * Get absolute path to database file
     *
     * @return
     */
    String getDatabaseAbsolutePath();

    /**
     * Get new {@link TodoJobMapper}
     */
    TodoJobMapper getNewTodoJobMapper();

    /**
     * Get new {@link TodoJobMapper}
     */
    Mono<TodoJobMapper> getNewTodoJobMapperAsync();
}
