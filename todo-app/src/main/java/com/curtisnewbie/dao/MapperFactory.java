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
     */
    String getDatabaseAbsolutePath();

    /**
     * Get new {@link TodoJobMapper}
     * <p>
     * This method may block until the factory is fully initialized
     * </p>
     */
    Mono<TodoJobMapper> getNewTodoJobMapperAsync();

}
