package com.curtisnewbie.dao;

import reactor.core.publisher.Mono;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.*;

/**
 * @author yongjie.zhuang
 */
public interface TodoJobMapper extends Mapper {

    CompletableFuture<List<TodoJob>> findByPageAsync(String name, int page);

    CompletableFuture<List<TodoJob>> findBetweenDatesAsync(String name, LocalDate startDate, LocalDate endDate);

    /**
     * If there is no 'earliest data', now is returned
     */
    CompletableFuture<LocalDate> findEarliestDateAsync();

    /**
     * If there is no 'latest data', now is returned
     */
    CompletableFuture<LocalDate> findLatestDateAsync();

    /**
     * Update record
     *
     * @return is record updated or not
     */
    CompletableFuture<Boolean> updateByIdAsync(TodoJob todoJob);

    /**
     * Delete record
     *
     * @return is record deleted or not
     */
    CompletableFuture<Boolean> deleteByIdAsync(int id);

    /**
     * Insert record
     *
     * @return primary key or null
     */
    CompletableFuture<Integer> insertAsync(TodoJob todoJob);
}
